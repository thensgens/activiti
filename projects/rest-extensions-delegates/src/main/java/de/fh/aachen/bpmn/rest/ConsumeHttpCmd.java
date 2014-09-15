package de.fh.aachen.bpmn.rest;

import java.io.IOException;

import de.fh.aachen.bpmn.rest.impl.api.ConsumeRequest;

public interface ConsumeHttpCmd {

	/**
	 * This method returns the result of an HTTP request. The return value can
	 * be null.
	 * 
	 * @param request
	 *            The request object contains all relevant information for the
	 *            HTTP request
	 * @return Wrapped HTTP response (depending on the content-type). Can be
	 *         null.
	 * @throws IOException
	 */
	public ConsumerTaskResult execute(ConsumeRequest request)
			throws IOException;
}
