package org.activiti.validation.validator.impl;

import java.util.List;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.bpmn.model.Interface;
import org.activiti.bpmn.model.Operation;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.validation.ValidationError;
import org.activiti.validation.validator.Problems;
import org.apache.commons.lang3.StringUtils;

/**
 * @author jbarrez
 */
public class ServiceTaskValidator extends ExternalInvocationTaskValidator {

    @Override
    protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {
        List<ServiceTask> serviceTasks = process.findFlowElementsOfType(ServiceTask.class);
        for (ServiceTask serviceTask : serviceTasks) {
            verifyImplementation(process, serviceTask, errors);
            verifyType(process, serviceTask, errors);
            verifyResultVariableName(process, serviceTask, errors);
            verifyWebservice(bpmnModel, process, serviceTask, errors);
        }
    }

    protected void verifyImplementation(Process process, ServiceTask serviceTask, List<ValidationError> errors) {
        System.out.println("[Validating service task. Verify Implementation: " + serviceTask.getImplementationType() + "]");

        if (!"foo".equalsIgnoreCase(serviceTask.getImplementationType()) &&
                !ImplementationType.IMPLEMENTATION_TYPE_CLASS.equalsIgnoreCase(serviceTask.getImplementationType())
                && !ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())
                && !ImplementationType.IMPLEMENTATION_TYPE_EXPRESSION.equalsIgnoreCase(serviceTask.getImplementationType())
                && !ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(serviceTask.getImplementationType())
                && StringUtils.isEmpty(serviceTask.getType())) {
            addError(errors, Problems.SERVICE_TASK_MISSING_IMPLEMENTATION, process, serviceTask,
                    "One of the attributes 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask.");
        }
    }

    protected void verifyType(Process process, ServiceTask serviceTask, List<ValidationError> errors) {
        if (StringUtils.isNotEmpty(serviceTask.getType())) {

            if (!serviceTask.getType().equalsIgnoreCase("mail")
                    && !serviceTask.getType().equalsIgnoreCase("mule")
                    && !serviceTask.getType().equalsIgnoreCase("camel")
                    && !(serviceTask.getType().equalsIgnoreCase("shell"))) {
                addError(errors, Problems.SERVICE_TASK_INVALID_TYPE, process, serviceTask, "Invalid or unsupported service task type");
            }

            if (serviceTask.getType().equalsIgnoreCase("mail")) {
                validateFieldDeclarationsForEmail(process, serviceTask, serviceTask.getFieldExtensions(), errors);
            } else if (serviceTask.getType().equalsIgnoreCase("shell")) {
                validateFieldDeclarationsForShell(process, serviceTask, serviceTask.getFieldExtensions(), errors);
            }

        }
    }

    protected void verifyResultVariableName(Process process, ServiceTask serviceTask, List<ValidationError> errors) {
        if (StringUtils.isNotEmpty(serviceTask.getResultVariableName())
                && (ImplementationType.IMPLEMENTATION_TYPE_CLASS.equals(serviceTask.getImplementationType()) ||
                ImplementationType.IMPLEMENTATION_TYPE_DELEGATEEXPRESSION.equals(serviceTask.getImplementationType()))) {
            addError(errors, Problems.SERVICE_TASK_RESULT_VAR_NAME_WITH_DELEGATE,
                    process, serviceTask, "'resultVariableName' not supported for service tasks using 'class' or 'delegateExpression");
        }
    }

    protected void verifyWebservice(BpmnModel bpmnModel, Process process, ServiceTask serviceTask, List<ValidationError> errors) {
        if (ImplementationType.IMPLEMENTATION_TYPE_WEBSERVICE.equalsIgnoreCase(serviceTask.getImplementationType()) &&
                StringUtils.isNotEmpty(serviceTask.getOperationRef())) {

            boolean operationFound = false;
            if (bpmnModel.getInterfaces() != null && bpmnModel.getInterfaces().size() > 0) {
                for (Interface bpmnInterface : bpmnModel.getInterfaces()) {
                    if (bpmnInterface.getOperations() != null && bpmnInterface.getOperations().size() > 0) {
                        for (Operation operation : bpmnInterface.getOperations()) {
                            if (operation.getId() != null && operation.getId().equals(serviceTask.getOperationRef())) {
                                operationFound = true;
                            }
                        }
                    }
                }
            }

            if (!operationFound) {
                addError(errors, Problems.SERVICE_TASK_WEBSERVICE_INVALID_OPERATION_REF, process, serviceTask, "Invalid operation reference");
            }

        }
    }

}
