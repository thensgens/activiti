package de.fh.aachen.bpmn.rest.impl.api;

import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.CONTENT_TYPES;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.HTTP_VERBS;

public class ConsumeRequest {

	private HTTP_VERBS mHttpVerb;
	private String mUrl;
	private CONTENT_TYPES mPredefinedContentType;
	private String mContentType;
	private String mMessageBody;
	private boolean mAuthRequired;
	private String mAuthName;
	private String mAuthPw;

	public ConsumeRequest(HTTP_VERBS mHttpVerb, String mUrl,
			CONTENT_TYPES mContentType, String mMessageBody,
			boolean mAuthRequired, String mAuthName, String mAuthPw) {
		super();
		this.mHttpVerb = mHttpVerb;
		this.mUrl = mUrl;
		this.mPredefinedContentType = mContentType;
		this.mMessageBody = mMessageBody;
		this.mAuthRequired = mAuthRequired;
		this.mAuthName = mAuthName;
		this.mAuthPw = mAuthPw;
	}

	public ConsumeRequest(HTTP_VERBS mHttpVerb, String mUrl,
			String contentType, String mMessageBody, boolean mAuthRequired,
			String mAuthName, String mAuthPw) {
		super();
		this.mHttpVerb = mHttpVerb;
		this.mUrl = mUrl;
		this.mContentType = contentType;
		this.mMessageBody = mMessageBody;
		this.mAuthRequired = mAuthRequired;
		this.mAuthName = mAuthName;
		this.mAuthPw = mAuthPw;
	}

	public HTTP_VERBS getHttpVerb() {
		return mHttpVerb;
	}

	public void setHttpVerb(HTTP_VERBS mHttpVerb) {
		this.mHttpVerb = mHttpVerb;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getMessageBody() {
		return mMessageBody;
	}

	public void setMessageBody(String mMessageBody) {
		this.mMessageBody = mMessageBody;
	}

	public boolean isAuthRequired() {
		return mAuthRequired;
	}

	public void setAuthRequired(boolean mAuthRequired) {
		this.mAuthRequired = mAuthRequired;
	}

	public String getAuthName() {
		return mAuthName;
	}

	public void setAuthName(String mAuthName) {
		this.mAuthName = mAuthName;
	}

	public String getAuthPw() {
		return mAuthPw;
	}

	public void setAuthPw(String mAuthPw) {
		this.mAuthPw = mAuthPw;
	}

	public CONTENT_TYPES getPredefinedContentType() {
		return mPredefinedContentType;
	}

	public void setPredefinedContentType(CONTENT_TYPES mContentType) {
		this.mPredefinedContentType = mContentType;
	}

	public String getContentType() {
		return mContentType;
	}

	public void setContentType(String mContentType) {
		this.mContentType = mContentType;
	}
}
