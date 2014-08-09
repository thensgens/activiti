package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.FormProperty;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.ExpressionManager;
import org.activiti.engine.impl.form.DefaultTaskFormHandler;
import org.activiti.engine.impl.form.TaskFormHandler;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by thens on 8/8/14.
 */
public class FooActivityBehavior extends TaskActivityBehavior {

    private TaskDefinition mTaskDefinition;

    public FooActivityBehavior(TaskDefinition taskDefinition) {
        mTaskDefinition = taskDefinition;
    }

    public void execute(ActivityExecution execution) throws Exception {
        System.out.println("Hello from FooActivityBehavior's execute method..");

        TaskEntity task = TaskEntity.createAndInsert(execution);
        task.setExecution(execution);








//        task.setTaskDefinition(taskDefinition);
//
//        if (taskDefinition.getNameExpression() != null) {
//            String name = (String) taskDefinition.getNameExpression().getValue(execution);
//            task.setName(name);
//        }
//
//        if (taskDefinition.getDescriptionExpression() != null) {
//            String description = (String) taskDefinition.getDescriptionExpression().getValue(execution);
//            task.setDescription(description);
//        }
//
//        if (taskDefinition.getDueDateExpression() != null) {
//            Object dueDate = taskDefinition.getDueDateExpression().getValue(execution);
//            if (dueDate != null) {
//                if (dueDate instanceof Date) {
//                    task.setDueDate((Date) dueDate);
//                } else if (dueDate instanceof String) {
//                    BusinessCalendar businessCalendar = Context
//                            .getProcessEngineConfiguration()
//                            .getBusinessCalendarManager()
//                            .getBusinessCalendar(DueDateBusinessCalendar.NAME);
//                    task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
//                } else {
//                    throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " +
//                            taskDefinition.getDueDateExpression().getExpressionText());
//                }
//            }
//        }
//
//        if (taskDefinition.getPriorityExpression() != null) {
//            final Object priority = taskDefinition.getPriorityExpression().getValue(execution);
//            if (priority != null) {
//                if (priority instanceof String) {
//                    try {
//                        task.setPriority(Integer.valueOf((String) priority));
//                    } catch (NumberFormatException e) {
//                        throw new ActivitiIllegalArgumentException("Priority does not resolve to a number: " + priority, e);
//                    }
//                } else if (priority instanceof Number) {
//                    task.setPriority(((Number) priority).intValue());
//                } else {
//                    throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " +
//                            taskDefinition.getPriorityExpression().getExpressionText());
//                }
//            }
//        }
//
//        if (taskDefinition.getCategoryExpression() != null) {
//            final Object category = taskDefinition.getCategoryExpression().getValue(execution);
//            if (category != null) {
//                if (category instanceof String) {
//                    task.setCategory((String) category);
//                } else {
//                    throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " +
//                            taskDefinition.getCategoryExpression().getExpressionText());
//                }
//            }
//        }
//
//        handleAssignments(task, execution);
//
//        // All properties set, now firing 'create' event
//        task.fireEvent(TaskListener.EVENTNAME_CREATE);
//
    }

}