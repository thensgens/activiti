package org.activiti.rest.service.api.runtime.task;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.history.HistoricTaskInstance;
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

        if (taskKey == null) {
            throw new ActivitiIllegalArgumentException("The task key cannot be null");
        }

        Task task = ActivitiUtil.getTaskService().createTaskQuery().processDefinitionKey(processDefinitionKey)
                .processInstanceId(processInstanceId).taskDefinitionKey(taskKey).singleResult();
        if (task == null) {
            throw new ActivitiObjectNotFoundException("Could not find a task with id '" + taskKey + "'.", Task.class);
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
