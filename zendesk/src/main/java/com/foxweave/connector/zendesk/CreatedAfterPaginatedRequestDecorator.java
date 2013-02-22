package com.foxweave.connector.zendesk;

import com.foxweave.connector.http.op.DefaultRequestDecorator;
import com.foxweave.connector.http.op.Operation;
import com.foxweave.connector.http.op.RequestDecorator;
import com.foxweave.exception.FoxWeaveException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;

import java.util.Map;

/**
 *
 */
public class CreatedAfterPaginatedRequestDecorator extends DefaultRequestDecorator {

    @Override
    public RequestDecorator setURI(HttpMethodBase httpMethod, Map<String, Object> contextObj) throws FoxWeaveException {
        Map<String, Object> lastMessage = (Map<String, Object>) contextObj.get("lastMessage");

        if (lastMessage != null) {
            Map<String, Object> lastEvals = (Map<String, Object>) contextObj.get("lastEvals");
            String lastNextPage = null;

            if (lastEvals != null) {
                Object nextPageVal = lastEvals.get("nextPage");
                if (nextPageVal != null) {
                    lastNextPage = nextPageVal.toString();
                }
            }

            if (lastNextPage != null) {
                try {
                    URI uri = new URI(lastNextPage, true);
                    if (uri.getScheme() != null && uri.getHost() != null) {
                        httpMethod.setURI(uri);
                        return this;
                    }
                } catch (URIException e) {
                    // Can't parse the nextPage URL... ignore it...
                }
            }

            // OK, no nextPage... try using the last page URI...
            Map<Object, Object> componentScopedCache = (Map<Object, Object>) contextObj.get(Operation.COMPONENT_SCOPED_CACHE);
            String lastMessageCreatedDate = (String) lastMessage.get("created_at");

            // Trim off the time part.  Note that Zendesk only lets you narrow query resultsets by
            // day i.e. it ignores the time part, which is a PITA, but maybe it's the only way they
            // can reliably paginate...
            lastMessageCreatedDate = lastMessageCreatedDate.substring(0, lastMessageCreatedDate.indexOf("T"));

            // If the last message processed has the same date as the date on the last invocation url,
            // then use that invocation url as the page url, otherwise fall back to building the url in the
            // standard way i.e. calling super.setURI().
            String lastURI = (String) componentScopedCache.get("lastURI");
            String lastPageQueryDate = getLastQueryDate(lastURI);
            if (lastMessageCreatedDate.equals(lastPageQueryDate)) {
                try {
                    httpMethod.setURI(new URI(lastURI, true));
                } catch (URIException e) {
                    throw new FoxWeaveException("Error constructing request URI using nextPage URL '" + lastNextPage + "'.", e);
                }
                return this;
            }
        }

        return fallbackToSuper(httpMethod, contextObj);
    }

    protected RequestDecorator fallbackToSuper(HttpMethodBase httpMethod, Map<String, Object> contextObj) throws FoxWeaveException {
        return super.setURI(httpMethod, contextObj);
    }

    public static String getLastQueryDate(String lastPageURI) throws FoxWeaveException {
        try {
            URI uri = new URI(lastPageURI, true);
            String query = uri.getQuery();
            String createdAfterDate = null;

            int createdAtIndex = query.indexOf("created>");
            if (createdAtIndex != -1) {
                createdAfterDate = query.substring(createdAtIndex);
                createdAfterDate = createdAfterDate.substring("created>".length(), createdAfterDate.indexOf("T"));
            }

            return createdAfterDate;
        } catch (URIException e) {
            throw new FoxWeaveException("Error parsing lastPageURI '" + lastPageURI + "'.", e);
        }
    }
}
