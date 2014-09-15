package de.fh.aachen.bpmn.rest.impl.delegates;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/* 
 * A delegate class (JavaDelegate) is mandatory for validation checks. 
 */
public class RestPublishJavaDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
	}
}