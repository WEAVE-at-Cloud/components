package com.foxweave.onepagecrm;

import com.foxweave.json.JSONUtil;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
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

        inputTransformer.setObjectName("contact");
        outputTransformer.setObjectName("contact");
        JSONObject contactsAlaOnepage = inputTransformer.transform(createContactsList().toString());
        System.out.println(contactsAlaOnepage.toString(4));
        JSONArray contactsArray = (JSONArray) JSONUtil.getValue(contactsAlaOnepage, "data", "contacts");

        for (int i = 0 ; i < contactsArray.length(); i++) {
            JSONObject contact = contactsArray.getJSONObject(i);
            Assert.assertEquals("jbloggs@bigcompany.co.uk", contact.getString("work_email"));
            Assert.assertEquals("jbloggs@home.com", contact.getString("home_email"));
            Assert.assertEquals("+1 23 978234", contact.getString("work_phone"));
            Assert.assertEquals("+1 23 123123", contact.getString("mobile_phone"));
            Assert.assertFalse(contact.has("emails"));
            Assert.assertFalse(contact.has("phones"));
        }

        JSONObject contactAlaOnepage = new JSONObject(outputTransformer.transform(contactsArray.getJSONObject(0)));
        Assert.assertEquals("work|jbloggs@bigcompany.co.uk,home|jbloggs@home.com", contactAlaOnepage.getString("emails"));
        Assert.assertEquals("work|+1 23 978234,mobile|+1 23 123123", contactAlaOnepage.getString("phones"));
        Assert.assertFalse(contactAlaOnepage.has("work_email"));
        Assert.assertFalse(contactAlaOnepage.has("home_email"));
        Assert.assertFalse(contactAlaOnepage.has("work_phone"));
        Assert.assertFalse(contactAlaOnepage.has("mobile_phone"));
    }

    @Test
    public void test_no_data() throws Exception {
        ContactPostInputTransformer inputTransformer = new ContactPostInputTransformer();
        ContactPreOutputTransformer outputTransformer = new ContactPreOutputTransformer();

        JSONObject payload = inputTransformer.transform("{}");

        outputTransformer.transform(payload);
    }

    private JSONObject createContactsList() throws JSONException {
        JSONObject list = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray contacts = new JSONArray();

        list.put("data", data);
        data.put("contacts", contacts);
        contacts.put(new JSONObject(SAMPLE_CONTACT));
        contacts.put(new JSONObject(SAMPLE_CONTACT));

        return list;
    }

    private static final String SAMPLE_CONTACT =
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
