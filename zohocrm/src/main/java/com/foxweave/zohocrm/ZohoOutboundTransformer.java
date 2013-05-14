package com.foxweave.zohocrm;

import com.foxweave.pipeline.transform.PipelinePayloadTransformer;
import com.foxweave.xml.XMLSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ZohoOutboundTransformer<F, T> implements PipelinePayloadTransformer<JSONObject, String> {

    private static DocumentBuilder docBuilder;

    private XMLSerializer serializer = new XMLSerializer(true);

    private String objectName;

    @Override
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public String transform(JSONObject record) throws Exception {
        Document document = docBuilder.newDocument();
        Element root = document.createElement(objectName);

        document.appendChild(root);
        Element row = document.createElement("row");
        row.setAttribute("no", "1");
        root.appendChild(row);

        JSONArray fieldNames = record.names();
        for (int i = 0; i < fieldNames.length(); i++) {
            Element FL = document.createElement("FL");

            row.appendChild(FL);

            String fieldName = fieldNames.getString(i);
            String fieldVal = record.getString(fieldName);

            // Need to remove underscores in field names.  See Inbound transformer.
            fieldName = fieldName.replace("_", " ");

            // Zoho has what seems to be a strange naming convention for it's NVP entries.  The name of the field is
            // stored in a 'val' field, while the value is stored in a 'content' field...
            FL.setAttribute("val", fieldName);
            FL.setTextContent(fieldVal);
        }

        return serializer.serialize(document);
    }

    static {
        try {
            docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected exception creating document builder.", e);
        }
    }
}