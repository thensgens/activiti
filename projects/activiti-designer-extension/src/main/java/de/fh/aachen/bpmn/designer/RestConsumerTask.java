package de.fh.aachen.bpmn.designer;

import org.activiti.designer.integration.servicetask.AbstractCustomServiceTask;
import org.activiti.designer.integration.servicetask.PropertyType;
import org.activiti.designer.integration.servicetask.annotation.Help;
import org.activiti.designer.integration.servicetask.annotation.Property;
import org.activiti.designer.integration.servicetask.annotation.PropertyItems;
import org.activiti.designer.integration.servicetask.annotation.Runtime;

/**
 * Defines the custom REST Consumer task.
 * 
 * @author Torben Hensgens
 * @version 1
 * @since 1.0.0
 */
@Runtime(javaDelegateClass = "de.fh.aachen.bpmn.rest.impl.delegates.RestConsumerJavaDelegate")
@Help(displayHelpShort = "REST Consumer Task", displayHelpLong = "New REST Consumer Task.")
public class RestConsumerTask extends AbstractCustomServiceTask {

	private final String HTTP_METHOD_GET = "GET";
	private final String HTTP_METHOD_POST = "POST";
	private final String HTTP_METHOD_PUT = "PUT";
	private final String HTTP_METHOD_DELETE = "DELETE";
	private final String HTTP_METHOD_HEAD = "HEAD";

	@Property(type = PropertyType.COMBOBOX_CHOICE, displayName = "Method: ", required = true)
	@Help(displayHelpShort = "HTTP Method", displayHelpLong = "The specified HTTP method.")
	@PropertyItems({ HTTP_METHOD_GET, HTTP_METHOD_GET, HTTP_METHOD_POST,
			HTTP_METHOD_POST, HTTP_METHOD_PUT, HTTP_METHOD_PUT,
			HTTP_METHOD_DELETE, HTTP_METHOD_DELETE, HTTP_METHOD_HEAD,
			HTTP_METHOD_HEAD })
	private String httpVerb;

	@Property(type = PropertyType.TEXT, displayName = "URL: ", required = true)
	@Help(displayHelpShort = "URL", displayHelpLong = "The URL for the desired resource.")
	private String urlResource;

	@Property(type = PropertyType.TEXT, displayName = "Content-Type: ")
	@Help(displayHelpShort = "MIME Content-Type", displayHelpLong = "The specified Content-Type.")
	private String contentType;

	@Property(type = PropertyType.MULTILINE_TEXT, displayName = "Body: ")
	@Help(displayHelpShort = "Message body", displayHelpLong = "The method body.")
	private String messageBody;
	
	@Property(type = PropertyType.TEXT, displayName = "Result variable: ")
	@Help(displayHelpShort = "Result variable", displayHelpLong = "The result of this operation will be stored in this variable.")
	private String resultVariable;

	@Property(type = PropertyType.BOOLEAN_CHOICE, displayName = "Authentification required?", required = true)
	@Help(displayHelpShort = "Authentification", displayHelpLong = "Specify whether or not (Basic HTTP) authentification is required for this service.")
	private String authRequired;

	@Property(type = PropertyType.TEXT, displayName = "Name: ")
	@Help(displayHelpShort = "Name", displayHelpLong = "Specified name for the authentifcation.")
	private String authName;

	@Property(type = PropertyType.TEXT, displayName = "Password: ")
	@Help(displayHelpShort = "Password", displayHelpLong = "Specified password for the authentifcation.")
	private String authPw;

	@Override
	public String contributeToPaletteDrawer() {
		return "BPMN For REST Extensions";
	}

	@Override
	public String getName() {
		return "REST Consumer Task";
	}

	@Override
	public String getSmallIconPath() {
		return "icons/coins.png";
	}
}