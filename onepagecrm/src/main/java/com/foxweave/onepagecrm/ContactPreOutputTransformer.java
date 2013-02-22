package com.foxweave.onepagecrm;

import com.foxweave.pipeline.transform.PipelinePayloadTransformer;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * OnePage CRM pre-output transformer.
 * <p/>
 * Need to transform the phone numbers and email addresses from the structure defined
 * in the module descriptor (foxweave-components.json) and into the structure
 * required by the OnePage API.
 */
public class ContactPreOutputTransformer implements PipelinePayloadTransformer {

    @Override
    public JSONObject transform(JSONObject payload) throws Exception {
        transformPhones(payload);
        transformEmails(payload);
        return payload;
    }

    private void transformPhones(JSONObject payload) {
        StringBuilder phones = new StringBuilder();

        for (String type : ContactPostInputTransformer.phoneTypes) {
            String fieldName = type + "_phone";
            String phoneNumber = payload.optString(fieldName, null);

            if (phoneNumber != null) {
                if (phones.length() > 0) {
                    phones.append(",");
                }
                phones.append(type + "|").append(phoneNumber);
                payload.remove(fieldName);
            }
        }

        if (phones.length() > 0) {
            try {
                payload.put("phones", phones.toString());
            } catch (JSONException e) {
            }
        }
    }

    private void transformEmails(JSONObject payload) {
        StringBuilder emails = new StringBuilder();

        for (String type : ContactPostInputTransformer.emailTypes) {
            String fieldName = type + "_email";
            String emailAddress = payload.optString(fieldName, null);

            if (emailAddress != null) {
                if (emails.length() > 0) {
                    emails.append(",");
                }
                emails.append(type + "|").append(emailAddress);
                payload.remove(fieldName);
            }
        }

        if (emails.length() > 0) {
            try {
                payload.put("emails", emails.toString());
            } catch (JSONException e) {
            }
        }
    }
}
