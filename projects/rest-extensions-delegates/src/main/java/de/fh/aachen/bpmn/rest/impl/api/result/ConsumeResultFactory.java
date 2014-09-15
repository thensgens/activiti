package de.fh.aachen.bpmn.rest.impl.api.result;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.CONTENT_TYPES;

public class ConsumeResultFactory {

	/**
	 * This method returns the correct task result type depending on the
	 * specified content-type. Null will be returned if the specified
	 * content-type cannot be processed.
	 * 
	 * @param type
	 *            Content-Type
	 * @param body
	 *            Response body
	 * @return Wrapped result object which depends on the content-type. Can be
	 *         null.
	 */
	public static ConsumerTaskResult createResult(CONTENT_TYPES type,
			String contentTypeString, String body) {
		ConsumerTaskResult result = null;
		if (body != null) {
			if (type.equals(CONTENT_TYPES.JSON)) {
				result = new JsonConsumeResult(body);
			} else if (type.equals(CONTENT_TYPES.PLAIN)) {
				result = new PlainConsumeResult(body);
			} else if (type.equals(CONTENT_TYPES.HTML)) {
				result = new HtmlConsumeResult(body);
			} else {
				result = new UnsupportedConsumeResult(body, contentTypeString);
			}
		}
		return result;
	}

}
