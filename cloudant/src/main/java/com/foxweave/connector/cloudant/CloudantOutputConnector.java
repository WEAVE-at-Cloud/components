/*
 * Copyright (C) 2012 FoxWeave, Ireland.
 *
 * All rights reserved.
 */
package com.foxweave.connector.cloudant;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.foxweave.exception.FoxWeaveException;
import com.foxweave.exception.ComponentConfigurationException;
import com.foxweave.internal.util.CharsetUtils;
import com.foxweave.internal.util.StreamUtils;
import com.foxweave.pipeline.component.OutputConnector;
import com.foxweave.pipeline.component.listener.ExchangeLifecycleListener;
import com.foxweave.pipeline.exchange.Exchange;
import com.foxweave.pipeline.exchange.Message;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudantOutputConnector extends AbstractCloudantConnector implements ExchangeLifecycleListener, OutputConnector {

    private static final Logger logger = LoggerFactory.getLogger(CloudantOutputConnector.class);
    private static final String CLOUDANT_DEFAULT_MAX_BATCH_SIZE = "CLOUDANT_DEFAULT_MAX_BATCH_SIZE";

    private static int DEFAULT_MAX_BATCH_SIZE = 1024 * 200;

    static {
        String userConfiguredDefaultMaxBatchSize = System.getProperty(CLOUDANT_DEFAULT_MAX_BATCH_SIZE);
        if (userConfiguredDefaultMaxBatchSize != null) {
            try {
                DEFAULT_MAX_BATCH_SIZE = Integer.parseInt(userConfiguredDefaultMaxBatchSize.trim());
            } catch (Exception e) {
                logger.error("Error parsing '" + CLOUDANT_DEFAULT_MAX_BATCH_SIZE + "' System property value '" + userConfiguredDefaultMaxBatchSize + "'.  Must be a valid number.  Defaulting to " + DEFAULT_MAX_BATCH_SIZE);
            }
        }
    }

    public static final String MAX_BATCH_SIZE = "maxBatchSize";

    private File batchFile;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private AtomicInteger numDocsInBatch = new AtomicInteger(0);

    public File getBatchFile() {
        return batchFile;
    }

    @Override
    public void setConfiguration(final JSONObject config) throws ComponentConfigurationException {
        super.setConfiguration(config);
        maxBatchSize = config.optInt(MAX_BATCH_SIZE, DEFAULT_MAX_BATCH_SIZE);
    }

    @Override
    protected String getRESTResource() {
        return "/_bulk_docs";
    }

    @Override
    public void start() throws Exception {
        batchFile = new File(getPipelineContext().getTempDir(), "cloudant/" + getComponentId() + "-batch.json");
        if (!batchFile.getParentFile().exists()) {
            batchFile.getParentFile().mkdirs();
        }
        if (batchFile.exists()) {
            sendBatch();
        }
    }

    @Override
    public void start(Exchange exchange) throws Exception {
    }

    @Override
    public void end(Exchange exchange) {
        if (batchFile.exists()) {
            try {
                sendBatch();
            } catch (Exception e) {
                logError(e);
            }
        }
    }

    @Override
    public synchronized Message send(final Message message) throws Exception {
        final JSONObject json = message.getPayload(JSONObject.class);

        if (json.has("_id")) {
            json.remove("_id");
        }
        if (json.has("_rev")) {
            json.remove("_rev");
        }

        if (batchFile.exists() && batchFile.length() >= maxBatchSize) {
            sendBatch();
        }

        FileOutputStream batchFileOS = new FileOutputStream(batchFile, true);
        try {
            OutputStreamWriter batchFileWriter = new OutputStreamWriter(batchFileOS, CharsetUtils.UTF8);
            try {
                if (batchFile.length() > 0) {
                    batchFileWriter.write(",");
                }
                batchFileWriter.write(json.toString());
                batchFileWriter.flush();
                numDocsInBatch.incrementAndGet();
            } finally {
                StreamUtils.safeClose(batchFileWriter);
            }
        } finally {
            StreamUtils.safeClose(batchFileOS);
        }

        return message;
    }

    private void sendBatch() throws FoxWeaveException, URIException {
        if (!batchFile.exists()) {
            return;
        }

        PostMethod postMethod = new PostMethod();
        try {
            FileRequestEntity requestEntity = new FileRequestEntity(batchFile, "application/json") {
                @Override
                public long getContentLength() {
                    return super.getContentLength() + "{\"docs\":[]}".getBytes(CharsetUtils.UTF8).length;
                }
                @Override
                public void writeRequest(OutputStream out) throws IOException {
                    out.write("{\"docs\":[".getBytes(CharsetUtils.UTF8));
                    super.writeRequest(out);
                    out.write("]}".getBytes(CharsetUtils.UTF8));
                    out.flush();
                }
            };

            if (numDocsInBatch.get() != 0) {
                logger.debug("Sending {} documents to Cloudant ({}).", numDocsInBatch.get(), requestURI);
            } else {
                // Num docs unknown - probably a restart
                logger.debug("Sending documents to Cloudant ({}).", requestURI);
            }

            postMethod.setURI(requestURI);
            postMethod.setRequestHeader("Authorization", "Basic " + encodedAuthCredentials);
            postMethod.setRequestEntity(requestEntity);
            httpClient.executeMethod(postMethod);

            logger.debug("Sending batch of documents to ");
            if (postMethod.getStatusCode() >= 200 && postMethod.getStatusCode() < 300) {
                batchFile.delete();
            } else {
                throw new FoxWeaveException("Response from '" + requestURI.getURI() + "' was " + postMethod.getStatusCode() + " " + postMethod.getResponseBodyAsString() + ".");
            }
        } catch (Exception e) {
            throw new FoxWeaveException("Error while trying to post to '" + requestURI.getURI() + "'", e);
        } finally {
            postMethod.releaseConnection();
        }
    }
}
