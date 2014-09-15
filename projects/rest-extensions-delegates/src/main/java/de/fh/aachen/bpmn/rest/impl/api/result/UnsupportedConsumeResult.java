package de.fh.aachen.bpmn.rest.impl.api.result;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;

public class UnsupportedConsumeResult implements ConsumerTaskResult {

	private String mUnsupportedContentType;
	private String mInput;

	public UnsupportedConsumeResult(String input, String contentType) {
		mInput = input;
		mUnsupportedContentType = contentType;
	}

	@Override
	public String getType() {
		return mUnsupportedContentType;
	}

	@Override
	public Object getValue() {
		return mInput;
	}
}