package com.foxweave.connector.cloudant.query;

import com.foxweave.connector.cloudant.CloudantInputConnector;
import com.foxweave.connector.cloudant.InputQueryHandler;
import com.foxweave.exception.FoxWeaveException;
import com.foxweave.io.StreamUtils;
import com.foxweave.json.streaming.JSONObjectCallback;
import com.foxweave.json.streaming.JSONStreamer;
import com.foxweave.pipeline.exchange.EntityState;
import com.foxweave.pipeline.exchange.Exchange;
import com.foxweave.pipeline.exchange.Message;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ViewQueryHandler implements InputQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(ViewQueryHandler.class);

    private static final String LAST_KEY_CACHE_KEY = CloudantInputConnector.class.getName() + "#LAST_KEY_CACHE_KEY";
    private static final String WEAVE_NO_DOCS = "$WEAVE-NO-DOCS$";

    private CloudantInputConnector cloudantInputConnector;

    @Override
    public void setCloudantInputConnector(CloudantInputConnector cloudantInputConnector) {
        this.cloudantInputConnector = cloudantInputConnector;
    }

    @Override
    public void poll() throws Exception {
        String lastKey = getLastKey();

        if (lastKey == null) {
            if (cloudantInputConnector.getPipelineContext().isSync()) {
                initializeStartKey();
                return;
            }
        }

        GetMethod method = new GetMethod(cloudantInputConnector.requestURI.toString());
        try {
            if (lastKey != null && !lastKey.equals(WEAVE_NO_DOCS)) {

                // FIXME: multiple docs can have the same key.  This dos not allow for that.

                NameValuePair[] queryParams = new NameValuePair[2];
                queryParams[0] = new NameValuePair("startkey", lastKey);
                queryParams[1] = new NameValuePair("skip", "1");

                method.setQueryString(queryParams);
            }

            method.setRequestHeader("Authorization", "Basic " + cloudantInputConnector.encodedAuthCredentials);
            if (cloudantInputConnector.httpClient.executeMethod(method) == 200) {
                InputStream dataStream = method.getResponseBodyAsStream();

                if (dataStream != null) {
                    try {
                        String charEnc = method.getResponseCharSet();

                        if (charEnc == null) {
                            charEnc = "UTF-8";
                        }

                        InputStreamReader dataStreamReader = new InputStreamReader(dataStream, charEnc);
                        try {
                            ViewCallback callback = new ViewCallback();
                            try {
                                JSONStreamer jsonStreamer = new JSONStreamer(callback, "rows");

                                jsonStreamer.stream(dataStreamReader);
                            } finally {
                                callback.endExchange();
                            }
                        } finally {
                            StreamUtils.safeClose(dataStreamReader);
                        }
                    } finally {
                        StreamUtils.safeClose(dataStream);
                    }
                }
            }
        } finally {
            method.releaseConnection();
        }
    }

    private void initializeStartKey() throws IOException, JSONException {
        GetMethod method = new GetMethod(cloudantInputConnector.requestURI.toString());
        NameValuePair[] queryParams = new NameValuePair[2];

        queryParams[0] = new NameValuePair("limit", "1");
        queryParams[1] = new NameValuePair("descending", "true");

        method.setQueryString(queryParams);
        method.setRequestHeader("Authorization", "Basic " + cloudantInputConnector.encodedAuthCredentials);
        if (cloudantInputConnector.httpClient.executeMethod(method) == 200) {
            JSONObject responseJSON = new JSONObject(method.getResponseBodyAsString());

            long total_rows = responseJSON.getLong("total_rows");
            if (total_rows == 0) {
                storeLastStartKey(WEAVE_NO_DOCS);
            } else {
                Object key = responseJSON.getJSONArray("rows").getJSONObject(0).get("key");
                storeLastStartKey(key.toString());
            }
        }
    }

    private void storeLastStartKey(String seq) {
        cloudantInputConnector.getPipelineContext().getPipelineScopedCache().put(LAST_KEY_CACHE_KEY, seq);
    }

    private String getLastKey() {
        return (String) cloudantInputConnector.getPipelineContext().getPipelineScopedCache().get(LAST_KEY_CACHE_KEY);
    }

    private class ViewCallback implements JSONObjectCallback {

        private Exchange exchange;

        @Override
        public boolean onJSONObject(JSONObject jsonObject) {
            JSONObject doc = jsonObject.optJSONObject("value");
            if (doc == null) {
                logger.warn("Cloudant view document without a 'value' element?  Potential JSONStreamer issue!");
                return cloudantInputConnector.pollContext.okayToContinue();
            }

            if (exchange == null) {
                exchange = cloudantInputConnector.exchangeFactory.newExchange();
                exchange.start();
            }

            Message message = exchange.newMessage();
            message.setPayload(doc);

            exchange.send(message);
            if (exchange.getState() != EntityState.OK) {
                return false;
            }

            storeLastStartKey(jsonObject.optString("key"));

            return cloudantInputConnector.pollContext.okayToContinue();
        }

        private void endExchange() {
            if (exchange != null) {
                exchange.end();
            } else {
                logger.debug("Nothing to sync from '{}'.", cloudantInputConnector.requestURI);
            }
        }

        @Override
        public void onException(Exception e) throws Exception {
            throw new FoxWeaveException("Error processing Cloudant view stream: " + cloudantInputConnector.getRESTResource(), e);
        }
    }
}
