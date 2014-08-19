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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.impl.DeploymentQueryProperty;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.rest.common.api.ActivitiUtil;
import org.activiti.rest.common.api.SecuredResource;
import org.activiti.rest.service.api.RestResponseFactory;
import org.activiti.rest.service.api.engine.variable.RestVariable;
import org.activiti.rest.service.api.repository.ProcessDefinitionResponse;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceCreateRequest;
import org.activiti.rest.service.api.runtime.process.ProcessInstanceResponse;
import org.activiti.rest.service.application.ActivitiRestServicesApplication;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * @author Torben Hensgens
 */
public class ProcessResource extends SecuredResource {

    protected static final String DEPRECATED_API_DEPLOYMENT_SEGMENT = "deployment";

    private static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

    static {
        allowedSortProperties.put("id", DeploymentQueryProperty.DEPLOYMENT_ID);
        allowedSortProperties.put("name", DeploymentQueryProperty.DEPLOYMENT_NAME);
        allowedSortProperties.put("deployTime", DeploymentQueryProperty.DEPLOY_TIME);
        allowedSortProperties.put("tenantId", DeploymentQueryProperty.DEPLOYMENT_TENANT_ID);
    }

    @Get
    public List<ProcessDefinitionResponse> getProcessDefinitions() {
        ProcessDefinitionQuery processDefinitionQuery = ActivitiUtil.getRepositoryService().createProcessDefinitionQuery();
        String process = getAttribute("process");
        processDefinitionQuery.processDefinitionKey(process);

        List<ProcessDefinitionResponse> responseList = new ArrayList<ProcessDefinitionResponse>();
        for (ProcessDefinition definition : processDefinitionQuery.list()) {
            ProcessDefinitionResponse responseEntry = new ProcessDefinitionResponse();
            responseEntry.setId(definition.getId());
            responseEntry.setName(definition.getName());
            responseEntry.setKey(definition.getKey());
            responseEntry.setCategory(definition.getCategory());
            responseEntry.setDeploymentId(definition.getDeploymentId());
            responseEntry.setVersion(definition.getVersion());
            responseEntry.setDeploymentId(definition.getDeploymentId());
            responseEntry.setSuspended(definition.isSuspended());
            responseList.add(responseEntry);
        }

        // TODO: fallback to 'processDefinitionKeyLike' query if the 'processDefinitionKey' doesn't return anything
        // ...
        return responseList;
    }

    @Post
    public ProcessInstanceResponse createProcessInstance(ProcessInstanceCreateRequest request) {
        if (!authenticate()) {
            return null;
        }

        RestResponseFactory factory = getApplication(ActivitiRestServicesApplication.class).getRestResponseFactory();

        Map<String, Object> startVariables = null;
        if (request.getVariables() != null) {
            startVariables = new HashMap<String, Object>();
            for (RestVariable variable : request.getVariables()) {
                if (variable.getName() == null) {
                    throw new ActivitiIllegalArgumentException("Variable name is required.");
                }
                startVariables.put(variable.getName(), factory.getVariableValue(variable));
            }
        }

        // Actually start the instance based on process definition key (here: value for /{process})
        try {
            ProcessInstance instance = null;
            instance = ActivitiUtil.getRuntimeService().startProcessInstanceByKey(
                    getAttribute("process"), request.getBusinessKey(), startVariables);
            setStatus(Status.SUCCESS_CREATED);
            return factory.createProcessInstanceResponse(this, instance);
        } catch (ActivitiObjectNotFoundException e) {
            throw new ActivitiIllegalArgumentException(e.getMessage(), e);
        }
    }
}
