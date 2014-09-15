package de.fh.aachen.bpmn.rest.impl.api.cmd;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import de.fh.aachen.bpmn.rest.ConsumeHttpCmd;
import de.fh.aachen.bpmn.rest.impl.api.ActivitiRestClient.CONTENT_TYPES;
import de.fh.aachen.bpmn.rest.impl.api.ConsumeRequest;
import de.fh.aachen.bpmn.rest.impl.util.StringUtils;

public abstract class HttpCmdImpl implements ConsumeHttpCmd {

	CloseableHttpClient mHttpclient;
	HttpClientBuilder mHttpBuilder = HttpClients.custom();
	HttpClientContext mContext = HttpClientContext.create();
	URL mUrl = null;

	protected void initializeHttpClient(ConsumeRequest request) {
		if (request.isAuthRequired()) {
			initAuthentication(request.getUrl(), request.getAuthName(),
					request.getAuthPw());
		}
		initHeader(request.getPredefinedContentType(), request.getContentType());
	}

	private void initHeader(CONTENT_TYPES contentType, String contentTypeString) {
		List<BasicHeader> headers = new ArrayList<BasicHeader>();
		if (contentType.equals(CONTENT_TYPES.JSON)) {
			headers.add(new BasicHeader("Content-Type", "application/json"));
		} else if (contentType.equals(CONTENT_TYPES.PLAIN)) {
			headers.add(new BasicHeader("Content-Type", "text/plain"));
		} else if (contentType.equals(CONTENT_TYPES.NON_PREDEFINED)) {
			if (StringUtils.isNotEmptyOrNull(contentTypeString)) {
				headers.add(new BasicHeader("Content-Type", contentTypeString));
			}
		}
		mHttpBuilder = mHttpBuilder.setDefaultHeaders(headers);
	}

	private void initAuthentication(String url, String authName, String authPw) {
		try {
			mUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(mUrl.getHost(), mUrl.getPort() != -1 ? mUrl
						.getPort() : mUrl.getDefaultPort()),
				new UsernamePasswordCredentials(authName, authPw));
		mHttpBuilder = mHttpBuilder
				.setDefaultCredentialsProvider(credsProvider);
	}
}
