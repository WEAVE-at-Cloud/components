package com.foxweave.zohocrm;

import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.Test;

public class ZohoPayloadTransformerTest {
    @Test
    public void test_has_data() throws Exception {


        ZohoPayloadTransformer inputTransformer = new ZohoPayloadTransformer();

        JSONObject payload = new JSONObject(SAMPLE);

        payload = inputTransformer.transform(payload);
        Assert.assertEquals("836925000000068001", payload.getString("LEADID"));
        Assert.assertEquals("836925000000061001", payload.getString("SMOWNERID"));
        Assert.assertEquals("Evan Habersham", payload.getString("Lead_Owner"));
        Assert.assertEquals("MickeyMouseClub", payload.getString("Company"));
        Assert.assertEquals("Mouse", payload.getString("Last_Name"));


    }

    @Test
    public void test_hasnt_data() throws Exception {
        ZohoPayloadTransformer inputTransformer = new ZohoPayloadTransformer();

        JSONObject payload = new JSONObject(SAMPLE);

        inputTransformer.transform(payload);
        Assert.assertFalse(payload.has("Lead_Owner"));
        Assert.assertFalse(payload.has("Company"));
        Assert.assertFalse(payload.has("Last_Name"));


    }


    private static final String SAMPLE =

            "  		{\n" +
                    " 			\"no\": \"1\",\n" +
                    " 			 \"FL\": [\n" +
                    "    {\n" +
                    "      \"content\": \"836925000000068001\",\n" +
                    "      \"val\": \"LEADID\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"836925000000061001\",\n" +
                    "      \"val\": \"SMOWNERID\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Evan Habersham\",\n" +
                    "      \"val\": \"Lead Owner\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"MickeyMouseClub\",\n" +
                    "      \"val\": \"Company\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Mickey\",\n" +
                    "      \"val\": \"First Name\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Mouse\",\n" +
                    "      \"val\": \"Last Name\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"CEO\",\n" +
                    "      \"val\": \"Designation\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"mouse@disney.com\",\n" +
                    "      \"val\": \"Email\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"111-111-1111\",\n" +
                    "      \"val\": \"Phone\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"44444444444\",\n" +
                    "      \"val\": \"Fax\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"222-222-2222\",\n" +
                    "      \"val\": \"Mobile\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"www.mickeymouseclub.com\",\n" +
                    "      \"val\": \"Website\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Cold Call\",\n" +
                    "      \"val\": \"Lead Source\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Attempted to Contact\",\n" +
                    "      \"val\": \"Lead Status\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Optical Networking\",\n" +
                    "      \"val\": \"Industry\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"500000\",\n" +
                    "      \"val\": \"No of Employees\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"75000\",\n" +
                    "      \"val\": \"Annual Revenue\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Active\",\n" +
                    "      \"val\": \"Rating\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"836925000000061001\",\n" +
                    "      \"val\": \"SMCREATORID\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Evan Habersham\",\n" +
                    "      \"val\": \"Created By\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"836925000000061001\",\n" +
                    "      \"val\": \"MODIFIEDBY\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Evan Habersham\",\n" +
                    "      \"val\": \"Modified By\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"2013-04-09 09:48:27\",\n" +
                    "      \"val\": \"Created Time\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"2013-04-11 07:26:37\",\n" +
                    "      \"val\": \"Modified By\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"1 House of Mouse\",\n" +
                    "      \"val\": \"Street\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"FL\",\n" +
                    "      \"val\": \"State\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"11111\",\n" +
                    "      \"val\": \"Zip Code\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"USA\",\n" +
                    "      \"val\": \"Country\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Test Client\",\n" +
                    "      \"val\": \"Description\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"mrmouseatmmclub\",\n" +
                    "      \"val\": \"Skype ID\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"true\",\n" +
                    "      \"val\": \"Email Opt Out\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"Mr.\",\n" +
                    "      \"val\": \"Salutation\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"wallawall@gmail.com\",\n" +
                    "      \"val\": \"Secondary Email\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"2013-04-11 07:26:45\",\n" +
                    "      \"val\": \"Last Activity Time\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"content\": \"MrMouse\",\n" +
                    "      \"val\": \"Twitter\"\n" +
                    "  }\n" +
                    "  ]\n" +
                    "}\n";
}

