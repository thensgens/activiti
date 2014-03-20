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
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.repository.DeploymentResponse;
import org.activiti.rest.service.api.runtime.task.TaskResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * @author Torben Hensgens
 */
public class TaskResource extends SecuredResource {

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

        // TODO: differentiate between regular user task and REST publish tasks (!)
        Task requestedTask = ActivitiUtil.getTaskService().createTaskQuery().processDefinitionKey(processName)
                .processInstanceId(instanceName).taskDefinitionKey(taskName).singleResult();
        if (requestedTask == null) {
            throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskName + "'.", Task.class);
        }

        return getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory()
                .createTaskResponse(this, requestedTask);
    }

//    @Put
//    public DeploymentResponse updateTask(Representation entity) {
//        if (!authenticate()) {
//            return null;
//        }
//    }
}
