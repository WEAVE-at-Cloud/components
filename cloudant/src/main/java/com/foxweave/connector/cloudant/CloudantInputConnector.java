package com.foxweave.connector.cloudant;

import com.foxweave.exception.FoxWeaveException;
import com.foxweave.io.StreamUtils;
import com.foxweave.json.streaming.JSONObjectCallback;
import com.foxweave.json.streaming.JSONStreamer;
import com.foxweave.pipeline.component.PollContext;
import com.foxweave.pipeline.component.PollingInputConnector;
import com.foxweave.pipeline.exchange.EntityState;
import com.foxweave.pipeline.exchange.Exchange;
import com.foxweave.pipeline.exchange.ExchangeFactory;
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

public class CloudantInputConnector extends AbstractCloudantConnector implements PollingInputConnector {

    private static final Logger logger = LoggerFactory.getLogger(CloudantInputConnector.class);

    private static final String LAST_SEQ_CACHE_KEY = CloudantInputConnector.class.getName() + "#LAST_SEQ_CACHE_KEY";

    private ExchangeFactory exchangeFactory;
    private PollContext pollContext;

    @Override
    protected String getRESTResource() {
        return "/_changes";
    }

    @Override
    public void setExchangeFactory(ExchangeFactory exchangeFactory) {
        this.exchangeFactory = exchangeFactory;
    }

    @Override
    public void setPollContext(PollContext pollContext) {
        this.pollContext = pollContext;
    }

    @Override
    public void poll() throws Exception {
        String lastSeqId = getLastSequenceId();

        if (lastSeqId == null) {
            if (getPipelineContext().isSync()) {
                initializeLastSyncSeqId();
                return;
            } else {
                lastSeqId = "0";
            }
        }

        GetMethod method = new GetMethod(requestURI.toString());
        try {
            NameValuePair[] queryParams = new NameValuePair[3];
            queryParams[0] = new NameValuePair("include_docs", "true");
            queryParams[1] = new NameValuePair("descending", "false");
            queryParams[2] = new NameValuePair("since", lastSeqId);

            method.setQueryString(queryParams);
            method.setRequestHeader("Authorization", "Basic " + encodedAuthCredentials);
            if (httpClient.executeMethod(method) == 200) {
                InputStream dataStream = method.getResponseBodyAsStream();

                if (dataStream != null) {
                    try {
                        String charEnc = method.getResponseCharSet();

                        if (charEnc == null) {
                            charEnc = "UTF-8";
                        }

                        InputStreamReader dataStreamReader = new InputStreamReader(dataStream, charEnc);
                        try {
                            CloudantDocHandler callback = new CloudantDocHandler();
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
            }
        } finally {
            method.releaseConnection();
        }
    }

    private void initializeLastSyncSeqId() throws IOException, JSONException {
        GetMethod method = new GetMethod(requestURI.toString());
        NameValuePair[] queryParams = new NameValuePair[3];

        queryParams[0] = new NameValuePair("include_docs", "true");
        queryParams[1] = new NameValuePair("descending", "true");
        queryParams[2] = new NameValuePair("limit", "1");

        method.setQueryString(queryParams);
        method.setRequestHeader("Authorization", "Basic " + encodedAuthCredentials);
        if (httpClient.executeMethod(method) == 200) {
            JSONObject responseJSON = new JSONObject(method.getResponseBodyAsString());
            storeLastSequenceId(responseJSON.getString("last_seq"));
        }
    }

    private void storeLastSequenceId(String seq) {
        getPipelineContext().getPipelineScopedCache().put(LAST_SEQ_CACHE_KEY, seq);
    }

    private String getLastSequenceId() {
        return (String) getPipelineContext().getPipelineScopedCache().get(LAST_SEQ_CACHE_KEY);
    }

    private class CloudantDocHandler implements JSONObjectCallback {

        private Exchange exchange;

        @Override
        public boolean onJSONObject(JSONObject jsonObject) {
            JSONObject doc = jsonObject.optJSONObject("doc");
            if (doc == null) {
                logger.warn("Cloudant '_changes' document without a 'doc' element?  Potential JSONStreamer issue!");
                return pollContext.okayToContinue();
            }

            String _rev = doc.optString("_rev", null);
            if (_rev == null) {
                logger.warn("Cloudant '_changes' document without a 'doc' element?  Potential JSONStreamer issue!");
                return pollContext.okayToContinue();
            }

            // For now, we're only processing new documents i.e. not supporting updates yet...
            if (_rev.charAt(0) != '1') {
                return pollContext.okayToContinue();
            }

            if (exchange == null) {
                exchange = exchangeFactory.newExchange();
                exchange.start();
            }

            Message message = exchange.newMessage();
            message.setPayload(doc);

            exchange.send(message);
            if (exchange.getState() != EntityState.OK) {
                return false;
            }

            storeLastSequenceId(jsonObject.optString("seq"));

            return pollContext.okayToContinue();
        }

        private void endExchange() {
            if (exchange != null) {
                exchange.end();
            }
        }

        @Override
        public void onException(Exception e) throws Exception {
            throw new FoxWeaveException("Error processing Cloudant '_changes' stream.", e);
        }
    }
}
