/*
 * Copyright (C) 2012 FoxWeave, Ireland.
 *
 * All rights reserved.
 */
package com.foxweave.connector.cloudant;

import com.foxweave.codec.Base64Coder;
import com.foxweave.data.component.ConfigUtil;
import com.foxweave.pipeline.component.AbstractPipelineComponent;
import com.foxweave.pipeline.component.ComponentConfigurationException;
import com.foxweave.pipeline.lifecycle.Configurable;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.json.JSONObject;

public abstract class AbstractCloudantConnector extends AbstractPipelineComponent implements Configurable<JSONObject> {

    public static final String URL = "cloudant_server_url";
    public static final String DATABASE_NAME = "cloudant_database_name";
    public static final String USER_NAME = "accountName";
    public static final String PASSWORD = "password";

    protected HttpClient httpClient = new HttpClient();
    protected URI requestURI;
    protected String encodedAuthCredentials;

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

        String cloudantUrl = dbUrl + getRESTResource();
        try {
            this.requestURI = new URI(cloudantUrl, false);
        } catch (URIException e) {
            throw new ComponentConfigurationException("Unexpected exception.  Invalid Cloudant URL.", e);
        }

        String username = ConfigUtil.getAccountParam(USER_NAME, config);
        String password = ConfigUtil.getAccountParam(PASSWORD, config);
        encodedAuthCredentials = Base64Coder.encodeString(username + ":" + password);
    }

    protected abstract String getRESTResource();
}
