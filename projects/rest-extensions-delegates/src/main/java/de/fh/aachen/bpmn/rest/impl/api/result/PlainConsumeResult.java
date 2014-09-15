package de.fh.aachen.bpmn.rest.impl.api.result;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;

public class PlainConsumeResult implements ConsumerTaskResult {

	private String mPlainText;

	public PlainConsumeResult(String text) {
		mPlainText = text;
	}

	@Override
	public String getType() {
		return "text/plain";
	}

	@Override
	public Object getValue() {
		return mPlainText;
	}
}