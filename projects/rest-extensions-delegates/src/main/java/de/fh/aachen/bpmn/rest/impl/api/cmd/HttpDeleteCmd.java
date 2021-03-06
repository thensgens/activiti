package de.fh.aachen.bpmn.rest.impl.api.cmd;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicResponseHandler;

import de.fh.aachen.bpmn.rest.ConsumerTaskResult;
import de.fh.aachen.bpmn.rest.impl.api.ConsumeRequest;
import de.fh.aachen.bpmn.rest.impl.api.result.ConsumeResultFactory;

public class HttpDeleteCmd extends HttpCmdImpl {

	@Override
	public ConsumerTaskResult execute(ConsumeRequest request)
			throws IOException {
		initializeHttpClient(request);
		mHttpclient = mHttpBuilder.build();
		String responseBody = null;
		try {
			HttpDelete httpDelete = new HttpDelete(request.getUrl());

			CloseableHttpResponse response = null;
			try {
				ResponseHandler<String> handler = new BasicResponseHandler();
				response = mHttpclient.execute(httpDelete, mContext);
				responseBody = handler.handleResponse(response);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (response != null) {
					response.close();
				}
			}
		} finally {
			mHttpclient.close();
		}

		return ConsumeResultFactory.createResult(
				request.getPredefinedContentType(), request.getContentType(),
				responseBody);
	}

}
