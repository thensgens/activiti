package de.fh.aachen.bpmn.rest.impl.api;

import java.io.IOException;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;
import de.fh.aachen.bpmn.rest.impl.api.cmd.HttpDeleteCmd;
import de.fh.aachen.bpmn.rest.impl.api.cmd.HttpGetCmd;
import de.fh.aachen.bpmn.rest.impl.api.cmd.HttpHeadCmd;
import de.fh.aachen.bpmn.rest.impl.api.cmd.HttpPostCmd;
import de.fh.aachen.bpmn.rest.impl.api.cmd.HttpPutCmd;

public class ActivitiRestClient {

	public ConsumerTaskResult executeRequest(ConsumeRequest request)
			throws IOException {

		ConsumerTaskResult taskResult = null;
		if (request.getHttpVerb().equals(HTTP_VERBS.GET)) {
			taskResult = new HttpGetCmd().execute(request);
		} else if (request.getHttpVerb().equals(HTTP_VERBS.POST)) {
			taskResult = new HttpPostCmd().execute(request);
		} else if (request.getHttpVerb().equals(HTTP_VERBS.PUT)) {
			taskResult = new HttpPutCmd().execute(request);
		} else if (request.getHttpVerb().equals(HTTP_VERBS.DELETE)) {
			taskResult = new HttpDeleteCmd().execute(request);
		} else if (request.getHttpVerb().equals(HTTP_VERBS.HEAD)) {
			taskResult = new HttpHeadCmd().execute(request);
		}
		return taskResult;
	}

	public enum HTTP_VERBS {
		GET, POST, PUT, DELETE, HEAD
	}

	public enum CONTENT_TYPES {
		JSON, PLAIN, HTML, NON_PREDEFINED
	}
}
