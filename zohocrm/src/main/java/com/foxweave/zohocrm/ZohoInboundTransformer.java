package com.foxweave.zohocrm;

import com.foxweave.json.JSONUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import com.foxweave.pipeline.transform.PipelinePayloadTransformer;

public class ZohoInboundTransformer<F, T> implements PipelinePayloadTransformer<String, JSONObject> {

    private String objectName;

    @Override
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public JSONObject transform(String rawPayload) throws Exception {
        JSONObject untransformedPayload = new JSONObject(rawPayload);
        JSONObject transformedPayload = new JSONObject();
        JSONArray rowsIn = (JSONArray) JSONUtil.getValue(untransformedPayload, "response", "result", objectName, "row");

        if (rowsIn != null) {
            JSONArray rowsOut = new JSONArray();
            transformedPayload.put("rows", rowsOut);

            for (int i = 0; i < rowsIn.length(); i++) {
                JSONObject rowIn = rowsIn.getJSONObject(i);
                JSONObject rowOut = transformRow(rowIn);

                rowsOut.put(rowOut);
            }
        }

        return transformedPayload;
    }

    private JSONObject transformRow(JSONObject rowIn) throws JSONException {
        JSONObject rowOut = new JSONObject();

        //Start traversing the 'FL' array
        JSONArray FL = rowIn.getJSONArray("FL");
        for (int i = 0; i < FL.length(); i++) {
            JSONObject fieldObject = FL.getJSONObject(i);
            String fieldName = fieldObject.getString("val");
            Object fieldValue = fieldObject.get("content");

            fieldName = fieldName.replace(" ", "_");

            rowOut.put(fieldName, fieldValue);
        }

        return rowOut;
    }
}