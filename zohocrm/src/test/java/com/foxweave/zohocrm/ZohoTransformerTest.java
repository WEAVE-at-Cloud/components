package com.foxweave.zohocrm;

import net.javacrumbs.jsonunit.JsonAssert;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.json.JSONObject;
import org.junit.Test;

public class ZohoTransformerTest {
    
    @Test
    public void test_inbound_transform() throws Exception {
        ZohoInboundTransformer inboundTransformer = new ZohoInboundTransformer();

        inboundTransformer.setObjectName("Leads");
        JSONObject result = inboundTransformer.transform(SAMPLE_INPUT);

//        System.out.println(result.toString(4));
        JsonAssert.assertJsonEquals(SAMPLE_INPUT_TRANSFORMED, result.toString());
    }

    @Test
    public void test_outbound_transform() throws Exception {
        ZohoOutboundTransformer outboundTransformer = new ZohoOutboundTransformer();

        outboundTransformer.setObjectName("Leads");
        String result = outboundTransformer.transform(new JSONObject("{\"Last_Activity_Time\": \"2013-05-06 14:25:02\"}"));

//        System.out.println(result);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(SAMPLE_OUTPUT_TRANSFORMED, result);
    }

    private static final String SAMPLE_INPUT = "{\n" +
                    "    \"response\": {\n" +
                    "        \"result\": {\n" +
                    "            \"Leads\": {\n" +
                    "                \"row\": [\n" +
                    "                    {\n" +
                    "                        \"no\": \"1\",\n" +
                    "                        \"FL\": [\n" +
                    "                            {\n" +
                    "                                \"content\": \"636925000000061001\",\n" +
                    "                                \"val\": \"LEADID\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"736925000000061001\",\n" +
                    "                                \"val\": \"SMOWNERID\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"Evan Habersham\",\n" +
                    "                                \"val\": \"Lead Owner\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"MickeyMouseClub\",\n" +
                    "                                \"val\": \"Company\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"2013-05-06 14:25:02\",\n" +
                    "                                \"val\": \"Last Activity Time\"\n" +
                    "                            }\n" +
                    "                        ]\n" +
                    "                    },\n" +
                    "                    {\n" +
                    "                        \"no\": \"2\",\n" +
                    "                        \"FL\": [\n" +
                    "                            {\n" +
                    "                                \"content\": \"836925000000061001\",\n" +
                    "                                \"val\": \"LEADID\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"936925000000061001\",\n" +
                    "                                \"val\": \"SMOWNERID\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"Evan Habersham\",\n" +
                    "                                \"val\": \"Lead Owner\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"Volkswagen\",\n" +
                    "                                \"val\": \"Company\"\n" +
                    "                            },\n" +
                    "                            {\n" +
                    "                                \"content\": \"2013-05-06 14:25:02\",\n" +
                    "                                \"val\": \"Last Activity Time\"\n" +
                    "                            }\n" +
                    "                        ]\n" +
                    "                    }\n" +
                    "                ]\n" +
                    "            }\n" +
                    "        },\n" +
                    "        \"uri\": \"/crm/private/json/Leads/getMyRecords\"\n" +
                    "    }\n" +
                    "}";

    private static final String SAMPLE_INPUT_TRANSFORMED =
            "{\"rows\": [\n" +
                    "    {\n" +
                    "        \"Company\": \"MickeyMouseClub\",\n" +
                    "        \"LEADID\": \"636925000000061001\",\n" +
                    "        \"Last_Activity_Time\": \"2013-05-06 14:25:02\",\n" +
                    "        \"Lead_Owner\": \"Evan Habersham\",\n" +
                    "        \"SMOWNERID\": \"736925000000061001\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"Company\": \"Volkswagen\",\n" +
                    "        \"LEADID\": \"836925000000061001\",\n" +
                    "        \"Last_Activity_Time\": \"2013-05-06 14:25:02\",\n" +
                    "        \"Lead_Owner\": \"Evan Habersham\",\n" +
                    "        \"SMOWNERID\": \"936925000000061001\"\n" +
                    "    }\n" +
                    "]}";

    private static final String SAMPLE_OUTPUT_TRANSFORMED =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                    "<Leads>\n" +
                    "    <row no=\"1\">\n" +
                    "        <FL val=\"Last Activity Time\">2013-05-06 14:25:02</FL>\n" +
                    "    </row>\n" +
                    "</Leads>\n";
}

