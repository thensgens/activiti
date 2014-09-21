package org.activiti.rest.service.api.runtime.task;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.bpmn.behavior.RestPublishTaskActivityBehavior;
import org.activiti.engine.task.Task;
import org.activiti.rest.common.api.ActivitiUtil;

/**
 * Created by Torben Hensgens
 */
public class TaskBaseRestResource extends TaskBaseResource {

    @Override
    protected Task getTaskFromRequest() {
        String processDefinitionKey = getAttribute("process");
        String processInstanceId = getAttribute("instance");
        String taskKey = getAttribute("task");
        Task task = null;

        if (taskKey.startsWith(RestPublishTaskActivityBehavior.RestPublishConstants.COMMON_PREFIX)) {
            if (taskKey == null) {
                throw new ActivitiIllegalArgumentException("The task key cannot be null");
            }

            task = ActivitiUtil.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey)
                    .processInstanceId(processInstanceId).taskDefinitionKey(taskKey).singleResult();
            if (task == null) {
                throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskKey + "'.", Task.class);
            }
        } else {
            throw new ActivitiException("REST publish tasks have to start with the common prefix 'rest_'.");
        }
        return task;
    }

    @Override
    protected HistoricTaskInstance getHistoricTaskFromRequest() {
        String processDefinitionKey = getAttribute("process");
        String processInstanceId = getAttribute("instance");
        String taskKey = getAttribute("task");

        if (taskKey == null) {
            throw new ActivitiIllegalArgumentException("The task key cannot be null");
        }

        HistoricTaskInstance task = ActivitiUtil.getHistoryService().createHistoricTaskInstanceQuery()
                .processDefinitionKey(processDefinitionKey).processInstanceId(processInstanceId)
                .taskDefinitionKey(taskKey).singleResult();
        if (task == null) {
            throw new ActivitiObjectNotFoundException("Could not find a task with id '" +
                    taskKey + "'.", Task.class);
        }
        return task;
    }
}
