/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.rest.service.api.publish;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.impl.bpmn.behavior.RestPublishTaskActivityBehavior;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.runtime.task.TaskActionRequest;
import org.activiti.rest.service.api.runtime.task.TaskBaseRestResource;
import org.activiti.rest.service.api.runtime.task.TaskResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Torben Hensgens
 */
public class TaskResource extends TaskBaseRestResource {

    protected static final String DEPRECATED_API_DEPLOYMENT_SEGMENT = "deployment";

    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

    private static final HashMap<String, Task> mTaskMap;

    static {
        allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
        allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
        allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
        allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
        mTaskMap = new HashMap<String, Task>();
    }

    @Get
    public TaskResponse retrieveTaskInformation() {
        if (!authenticate()) {
            return null;
        }

        String processInstance = getAttribute("instance");
        String taskName = getAttribute("task");
        String taskMapKey = processInstance + "/" + taskName;

        if (taskName == null) {
            throw new ActivitiIllegalArgumentException("The task identifier cannot be null");
        }

        if (taskName.startsWith(RestPublishTaskActivityBehavior.RestPublishConstants.COMMON_PREFIX)) {
            Task requestedTask = getTaskFromRequest();
            if (requestedTask == null && isProcessInstanceActive()) {
                // the task is not in the engine anymore but the related process instance is still active,
                // but it might be in our task cache which is valid as long as the related process
                // instance has not been deleted
                if (mTaskMap.containsKey(taskMapKey)) {
                    requestedTask = mTaskMap.get(taskMapKey);
                } else {
                    throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskName + "'.", Task.class);
                }
            }
            // the task is still null, thus the process instance has been shutdown
            if (requestedTask == null) {
                throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskName + "'" +
                        " and/or the related process instance has been shutdown.", Task.class);
            }
            return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                    .createTaskResponse(this, requestedTask);
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            throw new ActivitiException("REST publish tasks have to start with the common prefix 'rest_'.");
        }
    }

    @Put
    public void updateTaskState(TaskActionRequest actionRequest) {
        if (!authenticate()) {
            return;
        }

        String processInstance = getAttribute("instance");
        String taskName = getAttribute("task");
        String taskMapKey = processInstance + "/" + taskName;

        if (taskName == null) {
            throw new ActivitiIllegalArgumentException("The task identifier cannot be null");
        }

        if (taskName.startsWith(RestPublishTaskActivityBehavior.RestPublishConstants.COMMON_PREFIX)) {
            if (actionRequest == null) {
                throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
                        "A request body was expected when executing a task action.",
                        null, null));
            }

            Task task = getTaskFromRequest();

            if (task != null) {
                // save the task for later retrieval (even if the task has been completed)
                if (!mTaskMap.containsKey(taskMapKey)) {
                    mTaskMap.put(taskMapKey, task);
                }

                if (TaskActionRequest.ACTION_COMPLETE.equals(actionRequest.getAction())) {
                    completeTask(task, actionRequest);
                } else if (TaskActionRequest.ACTION_CLAIM.equals(actionRequest.getAction())) {
                    claimTask(task, actionRequest);
                } else if (TaskActionRequest.ACTION_DELEGATE.equals(actionRequest.getAction())) {
                    delegateTask(task, actionRequest);
                } else if (TaskActionRequest.ACTION_RESOLVE.equals(actionRequest.getAction())) {
                    resolveTask(task, actionRequest);
                } else {
                    throw new ActivitiIllegalArgumentException("Invalid action: '" + actionRequest.getAction() + "'.");
                }
            }
        } else {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            throw new ActivitiException("REST publish tasks have to start with the common prefix 'rest_'.");
        }
    }

    protected void completeTask(Task task, TaskActionRequest actionRequest) {
        if (actionRequest.getVariables() != null) {
            Map<String, Object> variablesToSet = new HashMap<String, Object>();
            for (RestVariable var : actionRequest.getVariables()) {
                if (var.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required");
                }

                Object actualVariableValue = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                        .getVariableValue(var);

                variablesToSet.put(var.getName(), actualVariableValue);
            }

            ActivitiUtil.getTaskService().complete(task.getId(), variablesToSet);
        } else {
            ActivitiUtil.getTaskService().complete(task.getId());
        }
    }

    protected void resolveTask(Task task, TaskActionRequest actionRequest) {
        ActivitiUtil.getTaskService().resolveTask(task.getId());
    }

    protected void delegateTask(Task task, TaskActionRequest actionRequest) {
        if (actionRequest.getAssignee() == null) {
            throw new ActivitiIllegalArgumentException("An assignee is required when delegating a task.");
        }
        ActivitiUtil.getTaskService().delegateTask(task.getId(), actionRequest.getAssignee());
    }

    protected void claimTask(Task task, TaskActionRequest actionRequest) {
        // In case the task is already claimed, a ActivitiTaskAlreadyClaimedException is thown and converted to
        // a CONFLICT response by the StatusService
        ActivitiUtil.getTaskService().claim(task.getId(), actionRequest.getAssignee());
    }
}