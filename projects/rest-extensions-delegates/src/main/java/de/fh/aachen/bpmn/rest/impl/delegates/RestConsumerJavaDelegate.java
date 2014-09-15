package de.fh.aachen.bpmn.rest.impl.delegates;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient;
import de.fh.aachen.bpmn.rest.impl.api.ConsumeRequest;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.CONTENT_TYPES;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.HTTP_VERBS;

public class RestConsumerJavaDelegate implements JavaDelegate {

	private Expression httpVerb, urlResource, contentType, messageBody,
			authRequired, authName, authPw;

	private Expression resultVariable;

	private static Map<String, HTTP_VERBS> mVerbMapping;

	// predefined contenttype mapping (including predefined consume results);
	private static Map<String, CONTENT_TYPES> mContentTypeMapping;

	static {
		mVerbMapping = new HashMap<String, HTTP_VERBS>();
		mContentTypeMapping = new HashMap<String, CONTENT_TYPES>();

		mVerbMapping.put("GET", HTTP_VERBS.GET);
		mVerbMapping.put("POST", HTTP_VERBS.POST);
		mVerbMapping.put("PUT", HTTP_VERBS.PUT);
		mVerbMapping.put("DELETE", HTTP_VERBS.DELETE);
		mVerbMapping.put("HEAD", HTTP_VERBS.HEAD);
		mContentTypeMapping.put("application/json", CONTENT_TYPES.JSON);
		mContentTypeMapping.put("text/plain", CONTENT_TYPES.PLAIN);
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		String method = httpVerb.getValue(execution).toString();
		String urlRes = urlResource.getValue(execution).toString();
		String contentTypeRes = contentType.getValue(execution).toString();
		String body = "";
		if (messageBody != null && messageBody.getValue(execution) != null) {
			body = messageBody.getValue(execution).toString();
		}
		boolean authReq = authRequired.getValue(execution).toString()
				.equals("true") ? true : false;
		String name = "";
		if (authName != null && authName.getValue(execution) != null) {
			name = authName.getValue(execution).toString();
		}
		String pw = "";
		if (authPw != null && authPw.getValue(execution) != null) {
			pw = authPw.getValue(execution).toString();
		}

		ConsumeRequest request = null;
		// build the consume request object (contains all necessary information
		// for the request)
		// use the predefined mapping if there is one defined; else simply use
		// the user input
		if (mContentTypeMapping.containsKey(contentTypeRes)) {
			request = new ConsumeRequest(mVerbMapping.get(method), urlRes,
					mContentTypeMapping.get(contentTypeRes), body, authReq,
					name, pw);
		} else {
			request = new ConsumeRequest(mVerbMapping.get(method), urlRes,
					contentTypeRes, body, authReq, name, pw);
			request.setPredefinedContentType(CONTENT_TYPES.NON_PREDEFINED);
		}
		ActivitiRestClient restClient = new ActivitiRestClient();
		ConsumerTaskResult consumeResult = restClient.executeRequest(request);

		// Setting the variable's value in the corresponding
		// context/execution if there was a 'result variable' specified
		if (resultVariable.getValue(execution) != null) {
			execution.setVariable(
					resultVariable.getValue(execution).toString(),
					consumeResult.getValue());
		}
	}
}
