package de.fh.aachen.bpmn.designer;

import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.servicetask.annotation.Help;
import org.activiti.designer.integration.servicetask.annotation.Property;
import org.activiti.designer.integration.servicetask.annotation.PropertyItems;
import org.activiti.designer.integration.servicetask.annotation.Runtime;

@Runtime(javaDelegateClass = "de.fh.aachen.bpmn.rest.delegates.RestPublishJavaDelegate")
@Help(displayHelpShort = "REST Publish Task", displayHelpLong = "REST Publish Task.")
public class RestPublishTask extends AbstractCustomServiceTask {

	@Property(type = PropertyType.TEXT, displayName = "Foo: ", required = true)
	private String defaultValue;
	
	@Override
	public String contributeToPaletteDrawer() {
		return "BPMN For REST Extensions";
	}

	@Override
	public String getName() {
		return "REST Publish Task";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/coins.png";
	}
}