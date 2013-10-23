/*
 * Copyright (C) 2012 FoxWeave, Ireland.
 *
 * All rights reserved.
 */
package com.foxweave.connector.cloudant;

import com.foxweave.pipeline.exchange.Exchange;
import com.foxweave.pipeline.exchange.Message;
import com.foxweave.test.FoxWeaveTestCase;
import com.foxweave.test.web.jetty.RequestResponse;
import com.foxweave.test.web.jetty.SimpleJSONServer;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;

public class CloudantOutputConnectorTest extends FoxWeaveTestCase {

    private SimpleJSONServer cloudantServer;

    @Before
    public void setup() {
        cloudantServer = new SimpleJSONServer();
    }

    @After
    public void tearDown() {
        cloudantServer.stop();
    }

    @Test
    public void test() throws Exception {
        // Setup the expected/required Cloudant server request response sequence...
        cloudantServer.addRequestResponse(
                new RequestResponse("Batch 1")
                        .setExpectedRequestBody(EXPECTED_BATCH_1)
                        .setStatusCode(HttpURLConnection.HTTP_CREATED)
        );
        cloudantServer.addRequestResponse(
                new RequestResponse("Batch 2")
                        .setExpectedRequestBody(EXPECTED_BATCH_2)
                        .setStatusCode(HttpURLConnection.HTTP_CREATED)
        );
        cloudantServer.addRequestResponse(
                new RequestResponse("Batch 3")
                        .setExpectedRequestBody(EXPECTED_BATCH_3)
                        .setStatusCode(HttpURLConnection.HTTP_CREATED)
        );

        CloudantOutputConnector connector = new CloudantOutputConnector();

        // Create the the connector config...
        JSONObject config = new JSONObject();
        JSONObject authAccount = new JSONObject();
        config.put(CloudantOutputConnector.URL, cloudantServer.getServerURL());
        config.put(CloudantOutputConnector.DATABASE_NAME, "testDB");
        config.put(CloudantOutputConnector.MAX_BATCH_SIZE, 512); // small batch size (in bytes)
        config.put("authAccount", authAccount);
        authAccount.put(CloudantOutputConnector.USER_NAME, "uname");
        authAccount.put(CloudantOutputConnector.PASSWORD, "pw");
        connector.setPipelineContext(getMockPipelineContext());
        connector.setConfiguration(config);

        // Initialize and start it...
        connector.initialize();
        connector.start();

        connector.getBatchFile().delete();

        // Start an exchange...
        Exchange exchange = newExchange();
        connector.start(exchange);

        // Pump through 12 records on the exchange. Should result in 3 batches
        // of records being sent to the cloudant server...
        for (int i = 0; i < 12; i++) {
            Message message = exchange.newMessage();
            JSONObject payload =  new JSONObject();

            payload.put("field1", "val-1-" + i);
            payload.put("field2", "val-2-" + i);
            payload.put("field3", "val-3-" + i);
            payload.put("field4", "val-4-" + i);
            payload.put("field5", "val-5-" + i);
            payload.put("field6", "val-6-" + i);
            message.setPayload(payload);

            connector.send(message);
        }

        connector.end(exchange);

        // Check that the requests went through as expected...
        cloudantServer.waitForAllRequestsToComplete();
        cloudantServer.assertAllRequestsCompleteOK();

        connector.stop();
        connector.destroy();
    }


    private static final String EXPECTED_BATCH_1 = "{\"docs\":[{\"field5\":\"val-5-0\",\"field4\":\"val-4-0\",\"field3\":\"val-3-0\",\"field2\":\"val-2-0\",\"field6\":\"val-6-0\",\"field1\":\"val-1-0\"},{\"field5\":\"val-5-1\",\"field4\":\"val-4-1\",\"field3\":\"val-3-1\",\"field2\":\"val-2-1\",\"field6\":\"val-6-1\",\"field1\":\"val-1-1\"},{\"field5\":\"val-5-2\",\"field4\":\"val-4-2\",\"field3\":\"val-3-2\",\"field2\":\"val-2-2\",\"field6\":\"val-6-2\",\"field1\":\"val-1-2\"},{\"field5\":\"val-5-3\",\"field4\":\"val-4-3\",\"field3\":\"val-3-3\",\"field2\":\"val-2-3\",\"field6\":\"val-6-3\",\"field1\":\"val-1-3\"},{\"field5\":\"val-5-4\",\"field4\":\"val-4-4\",\"field3\":\"val-3-4\",\"field2\":\"val-2-4\",\"field6\":\"val-6-4\",\"field1\":\"val-1-4\"}]}";
    private static final String EXPECTED_BATCH_2 = "{\"docs\":[{\"field5\":\"val-5-5\",\"field4\":\"val-4-5\",\"field3\":\"val-3-5\",\"field2\":\"val-2-5\",\"field6\":\"val-6-5\",\"field1\":\"val-1-5\"},{\"field5\":\"val-5-6\",\"field4\":\"val-4-6\",\"field3\":\"val-3-6\",\"field2\":\"val-2-6\",\"field6\":\"val-6-6\",\"field1\":\"val-1-6\"},{\"field5\":\"val-5-7\",\"field4\":\"val-4-7\",\"field3\":\"val-3-7\",\"field2\":\"val-2-7\",\"field6\":\"val-6-7\",\"field1\":\"val-1-7\"},{\"field5\":\"val-5-8\",\"field4\":\"val-4-8\",\"field3\":\"val-3-8\",\"field2\":\"val-2-8\",\"field6\":\"val-6-8\",\"field1\":\"val-1-8\"},{\"field5\":\"val-5-9\",\"field4\":\"val-4-9\",\"field3\":\"val-3-9\",\"field2\":\"val-2-9\",\"field6\":\"val-6-9\",\"field1\":\"val-1-9\"}]}";
    private static final String EXPECTED_BATCH_3 = "{\"docs\":[{\"field5\":\"val-5-10\",\"field4\":\"val-4-10\",\"field3\":\"val-3-10\",\"field2\":\"val-2-10\",\"field6\":\"val-6-10\",\"field1\":\"val-1-10\"},{\"field5\":\"val-5-11\",\"field4\":\"val-4-11\",\"field3\":\"val-3-11\",\"field2\":\"val-2-11\",\"field6\":\"val-6-11\",\"field1\":\"val-1-11\"}]}";
}
