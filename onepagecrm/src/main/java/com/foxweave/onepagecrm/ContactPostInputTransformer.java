package com.foxweave.onepagecrm;

import com.foxweave.json.JSONUtil;
import com.foxweave.pipeline.transform.PipelinePayloadTransformer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * OnePage CRM post-input transformer.
 * <p/>
 * Need to transform the phone numbers and email addresses from the structure
 * provided by the OnePage API and into the structure defined in the module
 * descriptor (foxweave-components.json).
 */
public class ContactPostInputTransformer<F, T> implements PipelinePayloadTransformer<String, JSONObject> {

    public static final Set<String> phoneTypes = new HashSet<String>(Arrays.asList("work", "home", "mobile"));
    public static final Set<String> emailTypes = new HashSet<String>(Arrays.asList("work", "home"));

    private String objectName;

    @Override
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public JSONObject transform(String contactsAlaOnePage) throws Exception {
        JSONObject contactsJsonResponse = new JSONObject(contactsAlaOnePage);
        JSONArray contactsArray = (JSONArray) JSONUtil.getValue(contactsJsonResponse, "data", objectName + "s");

        if (contactsArray != null) {
            int numContacts = contactsArray.length();
            for (int i = 0; i < numContacts; i++) {
                JSONObject contact = contactsArray.getJSONObject(i);
                transformPhones(contact);
                transformEmails(contact);
            }
        }

        return contactsJsonResponse;
    }

    private void transformPhones(JSONObject payload) {
        JSONArray phones = payload.optJSONArray("phones");
        if (phones != null) {
            payload.remove("phones");
            for (int i = 0; i < phones.length(); i++) {
                try {
                    JSONObject phone = phones.getJSONObject(i);
                    String type = phone.getString("type");

                    if (phoneTypes.contains(type)) {
                        payload.put(type + "_phone", phone.getString("number"));
                    }
                } catch (JSONException e) {
                }
            }
        }
    }

    private void transformEmails(JSONObject payload) {
        JSONArray emails = payload.optJSONArray("emails");
        if (emails != null) {
            payload.remove("emails");
            for (int i = 0; i < emails.length(); i++) {
                try {
                    JSONObject email = emails.getJSONObject(i);
                    String type = email.getString("type");

                    if (emailTypes.contains(type)) {
                        payload.put(type + "_email", email.getString("address"));
                    }
                } catch (JSONException e) {
                }
            }
        }
    }
}
