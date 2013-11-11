package com.foxweave.connector.mixpanel;

import com.foxweave.test.FoxWeaveTestCase;
import com.foxweave.test.web.jetty.RequestResponse;
import com.foxweave.test.web.jetty.SimpleServer;
import com.mixpanel.mixpanelapi.MixpanelAPI;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MixPanelOutputConnectorTest extends FoxWeaveTestCase {

    private SimpleServer mockMixpanelServer;

    @Before
    public void setUp() throws Exception {
        mockMixpanelServer = new SimpleServer();
    }

    @After
    public void tearDown() throws Exception {
        mockMixpanelServer.stop();
    }

    @Test
    public void test_set_profile() throws Exception {
        MixPanelOutputConnector mixPanelOutputConnector = new MixPanelOutputConnector() {
            @Override
            protected MixpanelAPI getMixpanelAPI() {
                return new MixpanelAPI(null, mockMixpanelServer.getServerURL() + "/engage");
            }
        };
        mixPanelOutputConnector.setPipelineContext(getMockPipelineContext());

        mockMixpanelServer.addRequestResponse(new RequestResponse()
                .setExpectedRequestURI(mockMixpanelServer.getServerURL() + "/engage")
                .setExpectedRequestParam("data", "W3siJHRpbWUiOjEyNDU2MTM4ODUwMDAsIiRkaXN0aW5jdF9pZCI6IjUwNDc5YjI0NjcxYmYiLCIkc2V0Ijp7Ik5hbWUiOiJNYXJrIERvd25lcyIsIkVtYWlsIjoibWRAeC5jb20ifSwiJGlwIjoiMTI3LjAuMC4xIn1d")
                .setResponsePayload("1")
        );

        mixPanelOutputConnector.setUserProfile(new JSONObject(SAMPLE_SET_PROFILE));

        mockMixpanelServer.waitForAllRequestsToComplete();
        mockMixpanelServer.assertAllRequestsCompleteOK();
    }

    @Test
    public void test_event() throws Exception {
        MixPanelOutputConnector mixPanelOutputConnector = new MixPanelOutputConnector() {
            @Override
            protected MixpanelAPI getMixpanelAPI() {
                return new MixpanelAPI(mockMixpanelServer.getServerURL() + "/track", null);
            }
        };
        mixPanelOutputConnector.setPipelineContext(getMockPipelineContext());

        mockMixpanelServer.addRequestResponse(new RequestResponse()
                .setExpectedRequestURI(mockMixpanelServer.getServerURL() + "/track?ip=0")
                .setExpectedRequestParam("data", "W3siZXZlbnQiOiJnYW1lIiwicHJvcGVydGllcyI6eyJ0aW1lIjoxMjQ1NjEzODg1LCJkaXN0aW5jdF9pZCI6IjUwNDc5YjI0NjcxYmYiLCJ0b2tlbiI6ImUzYmM0MTAwMzMwYzM1NzIyNzQwZmI4YzZmNWFiZGRjIiwiYWN0aW9uIjoicGxheSIsIm1wX2xpYiI6ImpkayIsImlwIjoiMTIzLjEyMy4xMjMuMTIzIn19XQ==")
                .setResponsePayload("1")
        );

        mixPanelOutputConnector.logActivity(new JSONObject(SAMPLE_EVENT));

        mockMixpanelServer.waitForAllRequestsToComplete();
        mockMixpanelServer.assertAllRequestsCompleteOK();
    }

    @Test
    public void test_delete_profile() throws Exception {
        MixPanelOutputConnector mixPanelOutputConnector = new MixPanelOutputConnector() {
            @Override
            protected MixpanelAPI getMixpanelAPI() {
                return new MixpanelAPI(null, mockMixpanelServer.getServerURL() + "/engage");
            }
        };
        mixPanelOutputConnector.setPipelineContext(getMockPipelineContext());

        mockMixpanelServer.addRequestResponse(new RequestResponse()
                .setExpectedRequestURI(mockMixpanelServer.getServerURL() + "/engage")
                .setExpectedRequestParam("data", "W3siJHRpbWUiOjEyNDU2MTM4ODUwMDAsIiRkaXN0aW5jdF9pZCI6IjUwNDc5YjI0NjcxYmYiLCIkZGVsZXRlIjoiIn1d")
                .setResponsePayload("1")
        );

        mixPanelOutputConnector.deleteUserProfile(new JSONObject(SAMPLE_DELETE_PROFILE));

        mockMixpanelServer.waitForAllRequestsToComplete();
        mockMixpanelServer.assertAllRequestsCompleteOK();
    }

    public static final String SAMPLE_SET_PROFILE = "{   \n" +
            "    \"distinct_id\": \"50479b24671bf\", \n" +
            "    \"ip\": \"127.0.0.1\", \n" +
            "    \"time\": 1245613885000, \n" +
            "    \"properties\": {\n" +
            "        \"Name\": \"Mark Downes\", \n" +
            "        \"Email\": \"md@x.com\"        \n" +
            "    }\n" +
            "}\n";

    public static final String SAMPLE_EVENT = "{   \n" +
            "    \"event\": \"game\", \n" +
            "    \"properties\": {\n" +
            "        \"distinct_id\": \"50479b24671bf\", \n" +
            "        \"ip\": \"123.123.123.123\", \n" +
            "        \"token\": \"e3bc4100330c35722740fb8c6f5abddc\", \n" +
            "        \"time\": 1245613885000, \n" +
            "        \"action\": \"play\"        \n" +
            "    }\n" +
            "}\n";

    public static final String SAMPLE_DELETE_PROFILE = "{   \n" +
            "    \"distinct_id\": \"50479b24671bf\", \n" +
            "    \"time\": 1245613885000\n" +
            "}\n";
}
