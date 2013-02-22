package com.foxweave.connector.zendesk;

import com.foxweave.connector.http.transform.RequestResponseTransformer;
import com.foxweave.exception.FoxWeaveException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 *
 */
public class AlreadySyncdFilteringTransformer implements RequestResponseTransformer {

    @Override
    public void setObjectName(String objectName) {
    }

    @Override
    public String createRequestBody(Map<String, Object> contextObj) throws FoxWeaveException {
        throw new IllegalStateException("Unexpected call to createRequestBody() on a GET request.");
    }

    @Override
    public JSONObject transformResponseBody(Map<String, Object> contextObj, String response) throws FoxWeaveException {
        JSONObject responseJSON;

        try {
            responseJSON = new JSONObject(response);
        } catch (JSONException e) {
            throw new FoxWeaveException("Error parsing Zendesk response.", e);
        }

        Map<String, Object> lastMessage = (Map<String, Object>) contextObj.get("lastMessage");
        if (lastMessage != null) {
            Long lastId = Long.parseLong(lastMessage.get("id").toString());
            try {
                JSONArray resultsArrayIn = responseJSON.optJSONArray("results");

                if (resultsArrayIn != null) {
                    JSONArray resultsArrayOut = new JSONArray();

                    responseJSON.put("results", resultsArrayOut);

                    int numRecords = resultsArrayIn.length();
                    for (int i = 0; i < numRecords; i++) {
                        JSONObject record = resultsArrayIn.getJSONObject(i);
                        Long recId = Long.parseLong(record.get("id").toString());

                        if (recId > lastId) {
                            resultsArrayOut.put(record);
                        }
                    }

                    responseJSON.put("count", resultsArrayOut.length());
                }
            } catch (JSONException e) {
                throw new FoxWeaveException("Error parsing Zendesk response.", e);
            }
        }

        return responseJSON;
    }
}
