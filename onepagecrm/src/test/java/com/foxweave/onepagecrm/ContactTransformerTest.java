package com.foxweave.onepagecrm;

import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 */
public class ContactTransformerTest {

    @Test
    public void test_has_data() throws Exception {
        ContactPostInputTransformer inputTransformer = new ContactPostInputTransformer();
        ContactPreOutputTransformer outputTransformer = new ContactPreOutputTransformer();

        JSONObject payload = new JSONObject(SAMPLE);

        inputTransformer.transform(payload);
        Assert.assertEquals("jbloggs@bigcompany.co.uk", payload.getString("work_email"));
        Assert.assertEquals("jbloggs@home.com", payload.getString("home_email"));
        Assert.assertEquals("+1 23 978234", payload.getString("work_phone"));
        Assert.assertEquals("+1 23 123123", payload.getString("mobile_phone"));
        Assert.assertFalse(payload.has("emails"));
        Assert.assertFalse(payload.has("phones"));

        outputTransformer.transform(payload);
        Assert.assertEquals("work|jbloggs@bigcompany.co.uk,home|jbloggs@home.com", payload.getString("emails"));
        Assert.assertEquals("work|+1 23 978234,mobile|+1 23 123123", payload.getString("phones"));
        Assert.assertFalse(payload.has("work_email"));
        Assert.assertFalse(payload.has("home_email"));
        Assert.assertFalse(payload.has("work_phone"));
        Assert.assertFalse(payload.has("mobile_phone"));
    }

    @Test
    public void test_hasnt_data() throws Exception {
        ContactPostInputTransformer inputTransformer = new ContactPostInputTransformer();
        ContactPreOutputTransformer outputTransformer = new ContactPreOutputTransformer();

        JSONObject payload = new JSONObject();

        inputTransformer.transform(payload);
        Assert.assertFalse(payload.has("work_email"));
        Assert.assertFalse(payload.has("home_email"));
        Assert.assertFalse(payload.has("work_phone"));
        Assert.assertFalse(payload.has("mobile_phone"));

        outputTransformer.transform(payload);
        Assert.assertFalse(payload.has("emails"));
        Assert.assertFalse(payload.has("phones"));
    }

    private static final String SAMPLE =
            "{\n" +
            "  \"emails\": [\n" +
            "    {\n" +
            "      \"address\": \"jbloggs@bigcompany.co.uk\",\n" +
            "      \"type\": \"work\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"address\": \"jbloggs@home.com\",\n" +
            "      \"type\": \"home\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"phones\": [\n" +
            "    {\n" +
            "      \"number\": \"+1 23 123123\",\n" +
            "      \"type\": \"mobile\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": \"+1 23 978234\",\n" +
            "      \"type\": \"work\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
}
