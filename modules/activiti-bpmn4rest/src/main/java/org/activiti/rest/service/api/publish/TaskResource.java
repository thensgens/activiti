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

    static {
        allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
        allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
        allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
        allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
    }

    @Get
    public TaskResponse retrieveTaskInformation() {
        if (!authenticate()) {
            return null;
        }

        String processName = getAttribute("process");
        String instanceName = getAttribute("instance");
        String taskName = getAttribute("task");

        if (taskName == null) {
            throw new ActivitiIllegalArgumentException("The task identifier cannot be null");
        }

        if (taskName.startsWith(RestPublishTaskActivityBehavior.RestPublishConstants.COMMON_PREFIX)) {
            Task requestedTask = ActivitiUtil.getTaskService().createTaskQuery().processDefinitionKey(processName)
                    .processInstanceId(instanceName).taskDefinitionKey(taskName).singleResult();
            if (requestedTask == null) {
                throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskName + "'.", Task.class);
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

        if (actionRequest == null) {
            throw new ResourceException(new Status(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE.getCode(),
                    "A request body was expected when executing a task action.",
                    null, null));
        }

        Task task = getTaskFromRequest();
        if (task != null) {
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
