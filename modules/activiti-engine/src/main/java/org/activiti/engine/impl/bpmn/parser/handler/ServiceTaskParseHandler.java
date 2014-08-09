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

import org.activiti.bpmn.constants.BpmnXMLConstants;
import org.activiti.bpmn.model.*;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


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


            // activiti:foo
        } else if ("foo".equalsIgnoreCase(serviceTask.getImplementationType())) {
            System.out.println(serviceTask.getImplementation());
            //activity.setActivityBehavior(bpmnParse.getActivityBehaviorFactory().createFooServiceTask(serviceTask, createTaskDefinition(bpmnParse, serviceTask)));

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

//    private TaskDefinition createTaskDefinition(BpmnParse bpmnParse, ServiceTask serviceTask) {
//        TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
//
//        // Create mocked form properties
//        List<FormProperty> mockedFormProps = new ArrayList<FormProperty>();
//        FormProperty mockProperty = new FormProperty();
//        mockProperty.setId("mock");
//        mockProperty.setName("Sample mock property");
//        mockProperty.setType("string");
//        mockProperty.setRequired(true);
//        mockProperty.setReadable(true);
//        mockProperty.setWriteable(true);
//        mockedFormProps.add(mockProperty);
//
//        // Set mocked form key (here: empty string for now)
//        String mockedFormKey = "";
//
//        // retrieve process definitions from the current scope
//        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) bpmnParse.getCurrentScope().getProcessDefinition();
//        taskFormHandler.parseConfiguration(mockedFormProps, mockedFormKey, bpmnParse.getDeployment(), processDefinition);
//        TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);
//        taskDefinition.setKey(serviceTask.getId());
//        processDefinition.getTaskDefinitions().put(serviceTask.getId(), taskDefinition);
//        ExpressionManager expressionManager = bpmnParse.getExpressionManager();
//
//
//
//
//        // ==================== //
//
//        taskFormHandler.parseConfiguration(userTask.getFormProperties(), userTask.getFormKey(), bpmnParse.getDeployment(), processDefinition);
//
//        TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);
//
//        taskDefinition.setKey(taskDefinitionKey);
//        processDefinition.getTaskDefinitions().put(taskDefinitionKey, taskDefinition);
//        ExpressionManager expressionManager = bpmnParse.getExpressionManager();
//
//        if (StringUtils.isNotEmpty(userTask.getName())) {
//            taskDefinition.setNameExpression(expressionManager.createExpression(userTask.getName()));
//        }
//
//        if (StringUtils.isNotEmpty(userTask.getDocumentation())) {
//            taskDefinition.setDescriptionExpression(expressionManager.createExpression(userTask.getDocumentation()));
//        }
//
//        if (StringUtils.isNotEmpty(userTask.getAssignee())) {
//            taskDefinition.setAssigneeExpression(expressionManager.createExpression(userTask.getAssignee()));
//        }
//        if (StringUtils.isNotEmpty(userTask.getOwner())) {
//            taskDefinition.setOwnerExpression(expressionManager.createExpression(userTask.getOwner()));
//        }
//        for (String candidateUser : userTask.getCandidateUsers()) {
//            taskDefinition.addCandidateUserIdExpression(expressionManager.createExpression(candidateUser));
//        }
//        for (String candidateGroup : userTask.getCandidateGroups()) {
//            taskDefinition.addCandidateGroupIdExpression(expressionManager.createExpression(candidateGroup));
//        }
//
//        // Activiti custom extension
//
//        // Task listeners
//        for (ActivitiListener taskListener : userTask.getTaskListeners()) {
//            taskDefinition.addTaskListener(taskListener.getEvent(), createTaskListener(bpmnParse, taskListener, userTask.getId()));
//        }
//
//        // Due date
//        if (StringUtils.isNotEmpty(userTask.getDueDate())) {
//            taskDefinition.setDueDateExpression(expressionManager.createExpression(userTask.getDueDate()));
//        }
//
//        // Category
//        if (StringUtils.isNotEmpty(userTask.getCategory())) {
//            taskDefinition.setCategoryExpression(expressionManager.createExpression(userTask.getCategory()));
//        }
//
//        // Priority
//        if (StringUtils.isNotEmpty(userTask.getPriority())) {
//            taskDefinition.setPriorityExpression(expressionManager.createExpression(userTask.getPriority()));
//        }
//    }
}

