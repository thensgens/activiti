package org.activiti.custom;

import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.api.runtime.DummySerializable;

import java.util.HashMap;
import java.util.Map;

public class FirstBPMTest extends PluggableActivitiTestCase {

    @Deployment(resources = {"org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml"})
    public void testBaz() {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("basicType", new DummySerializable());
        runtimeService.startProcessInstanceByKey("oneTaskProcess", vars);
        Task task = taskService.createTaskQuery().includeProcessVariables().singleResult();
        assertNotNull(task.getProcessVariables());
    }
}