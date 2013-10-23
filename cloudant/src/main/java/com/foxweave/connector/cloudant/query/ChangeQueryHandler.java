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
import com.foxweave.util.ExchangeUtil;
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
public class ChangeQueryHandler implements InputQueryHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChangeQueryHandler.class);

    private static final String LAST_SEQ_CACHE_KEY = CloudantInputConnector.class.getName() + "#LAST_SEQ_CACHE_KEY";

    private CloudantInputConnector cloudantInputConnector;

    @Override
    public void setCloudantInputConnector(CloudantInputConnector cloudantInputConnector) {
        this.cloudantInputConnector = cloudantInputConnector;
    }

    @Override
    public void poll() throws Exception {
        String lastSeqId = getLastSequenceId();

        if (lastSeqId == null) {
            if (cloudantInputConnector.getPipelineContext().isSync()) {
                initializeLastSyncSeqId();
                return;
            } else {
                lastSeqId = "0";
            }
        }

        GetMethod method = new GetMethod(cloudantInputConnector.requestURI.toString());
        try {
            NameValuePair[] queryParams = new NameValuePair[3];
            queryParams[0] = new NameValuePair("include_docs", "true");
            queryParams[1] = new NameValuePair("descending", "false");
            queryParams[2] = new NameValuePair("since", lastSeqId);

            method.setQueryString(queryParams);
            method.setRequestHeader("Authorization", "Basic " + cloudantInputConnector.encodedAuthCredentials);
            if (logger.isDebugEnabled()) {
                logger.debug("Executing Cloudant _changes poll: {}", method.getURI());
            }
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
                            ChangesCallback callback = new ChangesCallback();
                            try {
                                JSONStreamer jsonStreamer = new JSONStreamer(callback, "results");

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
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cloudant _changes poll failed: {} - {}", method.getStatusCode(), method.getResponseBodyAsString());
                }
            }
        } finally {
            method.releaseConnection();
        }
    }

    private void initializeLastSyncSeqId() throws IOException, JSONException {
        GetMethod method = new GetMethod(cloudantInputConnector.requestURI.toString());
        NameValuePair[] queryParams = new NameValuePair[3];

        queryParams[0] = new NameValuePair("include_docs", "true");
        queryParams[1] = new NameValuePair("descending", "true");
        queryParams[2] = new NameValuePair("limit", "1");

        method.setQueryString(queryParams);
        method.setRequestHeader("Authorization", "Basic " + cloudantInputConnector.encodedAuthCredentials);
        if (cloudantInputConnector.httpClient.executeMethod(method) == 200) {
            JSONObject responseJSON = new JSONObject(method.getResponseBodyAsString());
            storeLastSequenceId(responseJSON.getString("last_seq"));
        }
    }

    private void storeLastSequenceId(String seq) {
        cloudantInputConnector.getPipelineContext().getPipelineScopedCache().put(LAST_SEQ_CACHE_KEY, seq);
    }

    private String getLastSequenceId() {
        return (String) cloudantInputConnector.getPipelineContext().getPipelineScopedCache().get(LAST_SEQ_CACHE_KEY);
    }

    private class ChangesCallback implements JSONObjectCallback {

        private Exchange exchange;

        @Override
        public boolean onJSONObject(JSONObject jsonObject) {
            JSONObject doc = jsonObject.optJSONObject("doc");
            if (doc == null) {
                logger.warn("Cloudant '_changes' document without a 'doc' element?  Potential JSONStreamer issue!");
                return cloudantInputConnector.pollContext.okayToContinue();
            }

            String _rev = doc.optString("_rev", null);
            if (_rev == null) {
                logger.warn("Cloudant '_changes' document without a '_rev' value?  Potential JSONStreamer issue!");
                return cloudantInputConnector.pollContext.okayToContinue();
            }

            // For now, we're only processing new documents i.e. not supporting updates yet...
            if (_rev.charAt(0) != '1') {
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

            storeLastSequenceId(jsonObject.optString("seq"));

            return cloudantInputConnector.pollContext.okayToContinue();
        }

        private void endExchange() {
            if (exchange != null) {
                logger.debug("Sync'd {} doc(s) from '{}'.", ExchangeUtil.getMessageCount(exchange), cloudantInputConnector.requestURI);
                exchange.end();
            } else {
                logger.debug("Nothing to sync from '{}'.", cloudantInputConnector.requestURI);
            }
        }

        @Override
        public void onException(Exception e) throws Exception {
            throw new FoxWeaveException("Error processing Cloudant '_changes' stream.", e);
        }
    }
}
