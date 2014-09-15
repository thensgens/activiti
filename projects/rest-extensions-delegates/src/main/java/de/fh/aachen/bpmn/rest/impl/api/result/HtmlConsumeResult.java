package de.fh.aachen.bpmn.rest.impl.api.result;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;

public class HtmlConsumeResult implements ConsumerTaskResult {

	// simple representation for now
	private String mHtmlContent;

	public HtmlConsumeResult(String text) {
		mHtmlContent = text;
	}

	@Override
	public String getType() {
		return "text/html";
	}

	@Override
	public Object getValue() {
		return mHtmlContent;
	}

}
