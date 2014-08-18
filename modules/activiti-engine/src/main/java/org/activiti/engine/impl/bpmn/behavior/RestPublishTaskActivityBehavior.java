package org.activiti.engine.impl.bpmn.behavior;

import org.activiti.bpmn.model.ServiceTask;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.calendar.BusinessCalendar;
import org.activiti.engine.impl.calendar.DueDateBusinessCalendar;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.engine.impl.task.TaskDefinition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by thens on 8/9/14.
 */
public class RestPublishTaskActivityBehavior extends TaskActivityBehavior {

    protected ServiceTask mServiceTask;
    protected TaskDefinition mTaskDefinition;

    public RestPublishTaskActivityBehavior(ServiceTask serviceTask, TaskDefinition taskDefinition) {
        mServiceTask = serviceTask;
        mTaskDefinition = taskDefinition;
    }

    public void execute(ActivityExecution execution) throws Exception {
        TaskEntity task = TaskEntity.createAndInsert(execution);
        task.setExecution(execution);
        task.setTaskDefinition(mTaskDefinition);

        if (mTaskDefinition.getNameExpression() != null) {
            String name = (String) mTaskDefinition.getNameExpression().getValue(execution);
            task.setName(name);
        }

        if (mTaskDefinition.getDescriptionExpression() != null) {
            String description = (String) mTaskDefinition.getDescriptionExpression().getValue(execution);
            task.setDescription(description);
        }

        if (mTaskDefinition.getDueDateExpression() != null) {
            Object dueDate = mTaskDefinition.getDueDateExpression().getValue(execution);
            if (dueDate != null) {
                if (dueDate instanceof Date) {
                    task.setDueDate((Date) dueDate);
                } else if (dueDate instanceof String) {
                    BusinessCalendar businessCalendar = Context
                            .getProcessEngineConfiguration()
                            .getBusinessCalendarManager()
                            .getBusinessCalendar(DueDateBusinessCalendar.NAME);
                    task.setDueDate(businessCalendar.resolveDuedate((String) dueDate));
                } else {
                    throw new ActivitiIllegalArgumentException("Due date expression does not resolve to a Date or Date string: " +
                            mTaskDefinition.getDueDateExpression().getExpressionText());
                }
            }
        }

        if (mTaskDefinition.getPriorityExpression() != null) {
            final Object priority = mTaskDefinition.getPriorityExpression().getValue(execution);
            if (priority != null) {
                if (priority instanceof String) {
                    try {
                        task.setPriority(Integer.valueOf((String) priority));
                    } catch (NumberFormatException e) {
                        throw new ActivitiIllegalArgumentException("Priority does not resolve to a number: " + priority, e);
                    }
                } else if (priority instanceof Number) {
                    task.setPriority(((Number) priority).intValue());
                } else {
                    throw new ActivitiIllegalArgumentException("Priority expression does not resolve to a number: " +
                            mTaskDefinition.getPriorityExpression().getExpressionText());
                }
            }
        }

        if (mTaskDefinition.getCategoryExpression() != null) {
            final Object category = mTaskDefinition.getCategoryExpression().getValue(execution);
            if (category != null) {
                if (category instanceof String) {
                    task.setCategory((String) category);
                } else {
                    throw new ActivitiIllegalArgumentException("Category expression does not resolve to a string: " +
                            mTaskDefinition.getCategoryExpression().getExpressionText());
                }
            }
        }

        if (mTaskDefinition.getAssigneeExpression() != null) {
            task.setAssignee((String) mTaskDefinition.getAssigneeExpression().getValue(execution), true, false);
        }

        if (mTaskDefinition.getOwnerExpression() != null) {
            task.setOwner((String) mTaskDefinition.getOwnerExpression().getValue(execution));
        }

        if (!mTaskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
            for (Expression groupIdExpr : mTaskDefinition.getCandidateGroupIdExpressions()) {
                Object value = groupIdExpr.getValue(execution);
                if (value instanceof String) {
                    List<String> candiates = extractCandidates((String) value);
                    task.addCandidateGroups(candiates);
                } else if (value instanceof Collection) {
                    task.addCandidateGroups((Collection) value);
                } else {
                    throw new ActivitiIllegalArgumentException("Expression did not resolve to a string or collection of strings");
                }
            }
        }

        if (!mTaskDefinition.getCandidateUserIdExpressions().isEmpty()) {
            for (Expression userIdExpr : mTaskDefinition.getCandidateUserIdExpressions()) {
                Object value = userIdExpr.getValue(execution);
                if (value instanceof String) {
                    List<String> candiates = extractCandidates((String) value);
                    task.addCandidateUsers(candiates);
                } else if (value instanceof Collection) {
                    task.addCandidateUsers((Collection) value);
                } else {
                    throw new ActivitiException("Expression did not resolve to a string or collection of strings");
                }
            }
        }

        // All properties set, now firing 'create' event
        task.fireEvent(TaskListener.EVENTNAME_CREATE);
    }

    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        if (((ExecutionEntity) execution).getTasks().size() != 0)
            throw new ActivitiException("UserTask should not be signalled before complete");
        leave(execution);
    }

    protected List<String> extractCandidates(String str) {
        return Arrays.asList(str.split("[\\s]*,[\\s]*"));
    }
}