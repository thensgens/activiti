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
package org.activiti.engine.impl.bpmn.parser.handler;

import com.google.gson.*;
import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.*;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.behavior.WebServiceActivityBehavior;
import org.activiti.engine.impl.bpmn.data.AbstractDataAssociation;
import org.activiti.engine.impl.bpmn.data.IOSpecification;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author Joram Barrez
 */
public class ServiceTaskParseHandler extends AbstractExternalInvocationBpmnParseHandler<ServiceTask> {

    private static Logger logger = LoggerFactory.getLogger(ServiceTaskParseHandler.class);

    public Class<? extends BaseElement> getHandledType() {
        return ServiceTask.class;
    }

    protected void executeParse(BpmnParse bpmnParse, ServiceTask serviceTask) {
        ActivityImpl activity = createActivityOnCurrentScope(bpmnParse, serviceTask, BpmnXMLConstants.ELEMENT_TASK_SERVICE);
        activity.setAsync(serviceTask.isAsynchronous());
        activity.setExclusive(!serviceTask.isNotExclusive());

        // Email, Mule and Shell service tasks
        if (StringUtils.isNotEmpty(serviceTask.getType())) {

            if (serviceTask.getType().equalsIgnoreCase("mail")) {
                activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createMailActivityBehavior(serviceTask));

            } else if (serviceTask.getType().equalsIgnoreCase("mule")) {
                activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createMuleActivityBehavior(serviceTask, bpmnParse.getBpmnModel()));

            } else if (serviceTask.getType().equalsIgnoreCase("camel")) {
                activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createCamelActivityBehavior(serviceTask, bpmnParse.getBpmnModel()));

            } else if (serviceTask.getType().equalsIgnoreCase("shell")) {
                activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createShellActivityBehavior(serviceTask));

            } else {
                logger.warn("Invalid service task type: '" + serviceTask.getType() + "' " + " for service task " + serviceTask.getId());
            }

        }
        // Check if it is a custom REST publish task
        else if (serviceTask.getExtensionId().equals("de.fh.aachen.bpmn.designer.RestPublishTask")) {
            activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createRestPublishTaskActivityBehavior(serviceTask, createTaskDefinition(bpmnParse, serviceTask)));

            // activiti:class
        } else if (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(serviceTask.getImplementationType())) {
            activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createClassDelegateServiceTask(serviceTask));

            // activiti:delegateExpression
        } else if (ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
            activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createServiceTaskDelegateExpressionActivityBehavior(serviceTask));

            // activiti:expression
        } else if (ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())) {
            activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createServiceTaskExpressionActivityBehavior(serviceTask));

            // Webservice
        } else if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(serviceTask.getImplementationType()) &&
                StringUtils.isNotEmpty(serviceTask.getOperationRef())) {

            if (!bpmnParse.getOperations().containsKey(serviceTask.getOperationRef())) {
                logger.warn(serviceTask.getOperationRef() + " does not exist for service task " + serviceTask.getId());
            } else {

                WebServiceActivityBehavior webServiceActivityBehavior = bpmnParse.getActivityBehaviorFactory().createWebServiceActivityBehavior(serviceTask);
                webServiceActivityBehavior.setOperation(bpmnParse.getOperations().get(serviceTask.getOperationRef()));

                if (serviceTask.getIoSpecification() != null) {
                    IOSpecification ioSpecification = createIOSpecification(bpmnParse, serviceTask.getIoSpecification());
                    webServiceActivityBehavior.setIoSpecification(ioSpecification);
                }

                for (DataAssociation dataAssociationElement : serviceTask.getDataInputAssociations()) {
                    AbstractDataAssociation dataAssociation = createDataInputAssociation(bpmnParse, dataAssociationElement);
                    webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
                }

                for (DataAssociation dataAssociationElement : serviceTask.getDataOutputAssociations()) {
                    AbstractDataAssociation dataAssociation = createDataOutputAssociation(bpmnParse, dataAssociationElement);
                    webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
                }

                activity.setActivityBehavior(webServiceActivityBehavior);
            }
        } else {
            logger.warn("One of the attributes 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask " + serviceTask.getId());
        }

    }

    private TaskDefinition createTaskDefinition(BpmnParse bpmnParse, ServiceTask serviceTask) {
        TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();

        /** Hint:
         *  Relevant values for the field extension(s)
         *      getFieldName   : represents the name in the UI
         *      getStringValue : contains the actual value
         */

        String assigneeIdentifier = "assignee";
        String formKeyIdentifier = "formKey";
        String formPropIdentifier = "formProperties";
        String assignee = "";
        String formKey = "";
        String formPropsJson  = "";


        for (FieldExtension ext : serviceTask.getFieldExtensions()) {
            if (ext.getFieldName().equals(formPropIdentifier)) {
                formPropsJson = ext.getStringValue();
            } else if (ext.getFieldName().equals(assigneeIdentifier)) {
                assignee = ext.getStringValue();
            } else if (ext.getFieldName().equals(formKeyIdentifier)) {
                formKey = ext.getStringValue();
            }
        }

        // parse the form properties (input format is json) and populate the form property list
        List<FormProperty> formPropList = populateFormPropList(formPropsJson);


        // retrieve process definitions from the current scope
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) bpmnParse.getCurrentScope().getProcessDefinition();
        taskFormHandler.parseConfiguration(formPropList, formKey, bpmnParse.getDeployment(), processDefinition);
        TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);
        taskDefinition.setKey(serviceTask.getId());
        processDefinition.getTaskDefinitions().put(serviceTask.getId(), taskDefinition);
        ExpressionManager expressionManager = bpmnParse.getExpressionManager();

        // Mocking values for name, assignee etc.
        taskDefinition.setNameExpression(expressionManager.createExpression(serviceTask.getName()));
        taskDefinition.setAssigneeExpression(expressionManager.createExpression("kermit"));

        return taskDefinition;
    }

    private List<FormProperty> populateFormPropList(String formPropsInput) {
        JsonParser jsonParser = new JsonParser();
        List<FormProperty> resultList = new ArrayList<FormProperty>();

        try {
            JsonObject object = (JsonObject) jsonParser.parse(formPropsInput);
            Iterator<Map.Entry<String, JsonElement>> iter = object.entrySet().iterator();
            Map.Entry<String, JsonElement> firstEntry = iter.next();
            JsonArray propertyArray = (JsonArray) firstEntry.getValue();
            for (JsonElement jsonElement : propertyArray) {
                JsonObject arrayEntryObject = (JsonObject) jsonElement;
                FormProperty property = new FormProperty();
                property.setId(arrayEntryObject.getAsJsonPrimitive("id").getAsString());
                property.setName(arrayEntryObject.getAsJsonPrimitive("name").getAsString());
                property.setType(arrayEntryObject.getAsJsonPrimitive("type").getAsString());
                property.setRequired(arrayEntryObject.getAsJsonPrimitive("required").getAsBoolean());
                property.setReadable(arrayEntryObject.getAsJsonPrimitive("readable").getAsBoolean());
                property.setReadable(arrayEntryObject.getAsJsonPrimitive("writable").getAsBoolean());
                property.setVariable(arrayEntryObject.getAsJsonPrimitive("variableName").getAsString());
                property.setExpression(arrayEntryObject.getAsJsonPrimitive("expression").getAsString());
                property.setDefaultExpression(arrayEntryObject.getAsJsonPrimitive("defaultExpression").getAsString());

                resultList.add(property);
            }
        } catch (JsonSyntaxException e) {
            throw new ActivitiException(e.getMessage());
        } catch (Exception e) {
            throw new ActivitiException("Error during parsing of form properties.");
        }
        return resultList;
    }
}