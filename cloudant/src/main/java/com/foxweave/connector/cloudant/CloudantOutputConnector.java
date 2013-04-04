/*
 * Copyright (C) 2012 FoxWeave, Ireland.
 *
 * All rights reserved.
 */
package com.foxweave.connector.cloudant;

import java.io.*;

import com.foxweave.codec.Base64Coder;
import com.foxweave.data.component.ConfigUtil;
import com.foxweave.exception.FoxWeaveException;
import com.foxweave.io.CharsetUtils;
import com.foxweave.io.FileUtils;
import com.foxweave.io.StreamUtils;
import com.foxweave.pipeline.component.AbstractPipelineComponent;
import com.foxweave.pipeline.component.ComponentConfigurationException;
import com.foxweave.pipeline.component.OutputConnector;
import com.foxweave.pipeline.component.listener.ExchangeLifecycleListener;
import com.foxweave.pipeline.exchange.Exchange;
import com.foxweave.pipeline.exchange.Message;
import com.foxweave.pipeline.lifecycle.Configurable;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudantOutputConnector extends AbstractPipelineComponent implements ExchangeLifecycleListener, Configurable<JSONObject>, OutputConnector {

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

    public static final String URL = "cloudant_server_url";
    public static final String DATABASE_NAME = "cloudant_database_name";
    public static final String USER_NAME = "accountName";
    public static final String PASSWORD = "password";
    public static final String MAX_BATCH_SIZE = "maxBatchSize";

    private HttpClient httpClient;
    private URI requestURI;
    private String encodedAuthCredentials;
    private File batchFile;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;

    public File getBatchFile() {
        return batchFile;
    }

    @Override
    public void setConfiguration(final JSONObject config) throws ComponentConfigurationException {
        String serverURL = config.optString(URL);
        String dbName = config.optString(DATABASE_NAME);
        String dbUrl;

        if (serverURL.endsWith("/")) {
            dbUrl = serverURL + dbName;
        } else {
            dbUrl = serverURL + "/" + dbName;
        }
        try {
            this.requestURI = new URI(dbUrl + "/_bulk_docs", false);
        } catch (URIException e) {
            throw new ComponentConfigurationException("");
        }

        String username = ConfigUtil.getAccountParam(USER_NAME, config);
        String password = ConfigUtil.getAccountParam(PASSWORD, config);
        encodedAuthCredentials = Base64Coder.encodeString(username + ":" + password);

        maxBatchSize = config.optInt(MAX_BATCH_SIZE, DEFAULT_MAX_BATCH_SIZE);
    }

    @Override
    public void start() throws Exception {
        batchFile = new File(getPipelineContext().getTempDir(), "cloudant/" + getComponentId() + "-batch.json");
        if (!batchFile.getParentFile().exists()) {
            batchFile.getParentFile().mkdirs();
        }

        httpClient = new HttpClient();
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

        StringBuilder messageBuilder = new StringBuilder();
        try {
            messageBuilder.append("{\"docs\":[");
            messageBuilder.append(new String(FileUtils.read(batchFile), CharsetUtils.UTF8));
            messageBuilder.append("]}");
        } catch (IOException e) {
            e.printStackTrace();
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

            postMethod.setURI(requestURI);
            postMethod.setRequestHeader("Authorization", "Basic " + encodedAuthCredentials);
            postMethod.setRequestEntity(requestEntity);
            httpClient.executeMethod(postMethod);

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
