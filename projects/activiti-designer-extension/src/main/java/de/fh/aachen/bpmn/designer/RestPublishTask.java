package de.fh.aachen.bpmn.designer;

import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.servicetask.annotation.Help;
import org.activiti.designer.integration.servicetask.annotation.Property;
import org.activiti.designer.integration.servicetask.annotation.Runtime;

@Runtime(javaDelegateClass = "de.fh.aachen.bpmn.rest.delegates.RestPublishJavaDelegate")
@Help(displayHelpShort = "REST Publish Task", displayHelpLong = "REST Publish Task.")
public class RestPublishTask extends AbstractCustomServiceTask {

	@Property(type = PropertyType.TEXT, displayName = "Assignee: ", required = true)
	private String assignee;
	
	@Property(type = PropertyType.TEXT, displayName = "Form key: ", required = true)
	private String formKey;
	
	@Property(type = PropertyType.MULTILINE_TEXT, displayName = "Form properties: ", required = true, defaultValue = "{\n\t\"formProperties\" : [\n\t\t{\n\t\t\t\"id\" : \"foo\",\n\t\t\t\"name\" : \"Sample Form Value\",\n\t\t\t\"type\" : \"string\",\n\t\t\t\"required\" : true,\n\t\t\t\"readable\" : true,\n\t\t\t\"writable\" : true,\n\t\t\t\"variableName\" : \"\",\n\t\t\t\"expression\" : \"\",\n\t\t\t\"defaultExpression\" : \"baz\"\n\t\t}\n\t]\n}")
	private String formProperties;
	
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
		return "icons/rsz_resource_symbol.png";
	}
}