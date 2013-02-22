package com.foxweave.connector.zendesk;

import com.foxweave.connector.http.op.Operation;
import com.foxweave.connector.http.op.RequestDecorator;
import com.foxweave.exception.FoxWeaveException;
import junit.framework.Assert;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CreatedAfterPaginatedRequestDecoratorTest {

    private HashMap<String, Object> contextObj;
    private HashMap<String, Object> lastMessage;
    private HashMap<String, Object> lastEvals;
    private Map<Object, Object> componentScopedCache;

    @Before
    public void initContextObj() {
        contextObj = new HashMap<String, Object>();
        lastMessage = new HashMap<String, Object>();
        lastEvals = new HashMap<String, Object>();
        componentScopedCache = new HashMap<Object, Object>();

        contextObj.put("lastMessage", lastMessage);
        contextObj.put("lastEvals", lastEvals);
        contextObj.put(Operation.COMPONENT_SCOPED_CACHE, componentScopedCache);
    }

    @Test
    public void test_hasNoLastMessage() throws FoxWeaveException, URIException {
        CreatedAfterPaginatedRequestDecorator decorator = new CreatedAfterPaginatedRequestDecorator() {
            @Override
            protected RequestDecorator fallbackToSuper(HttpMethodBase httpMethod, Map<String, Object> contextObj) throws FoxWeaveException {
                try {
                    httpMethod.setURI(new URI("http://fallbackUri/", true));
                } catch (URIException e) {
                }
                return this;
            }
        };
        GetMethod getMethod = new GetMethod();

        contextObj.remove("lastMessage");

        decorator.setURI(getMethod, contextObj);

        Assert.assertEquals("http://fallbackUri/", getMethod.getURI().toString());
    }

    @Test
    public void test_hasNextPage() throws FoxWeaveException, URIException {
        CreatedAfterPaginatedRequestDecorator decorator = new CreatedAfterPaginatedRequestDecorator();
        GetMethod getMethod = new GetMethod();

        lastEvals.put("nextPage", "https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc");

        decorator.setURI(getMethod, contextObj);

        Assert.assertEquals("https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc", getMethod.getURI().toString());
    }

    @Test
    public void test_lastPage_same_as_last_message() throws FoxWeaveException, URIException {
        CreatedAfterPaginatedRequestDecorator decorator = new CreatedAfterPaginatedRequestDecorator();
        GetMethod getMethod = new GetMethod();

        lastMessage.put("created_at", "2012-12-08T12:23:50Z");
        componentScopedCache.put("lastURI", "https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc");

        decorator.setURI(getMethod, contextObj);

        Assert.assertEquals("https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc", getMethod.getURI().toString());
    }

    @Test
    public void test_lastPage_not_same_as_last_message() throws FoxWeaveException, URIException {
        CreatedAfterPaginatedRequestDecorator decorator = new CreatedAfterPaginatedRequestDecorator() {
            @Override
            protected RequestDecorator fallbackToSuper(HttpMethodBase httpMethod, Map<String, Object> contextObj) throws FoxWeaveException {
                try {
                    httpMethod.setURI(new URI("http://fallbackUri/", true));
                } catch (URIException e) {
                }
                return this;
            }
        };
        GetMethod getMethod = new GetMethod();

        lastMessage.put("created_at", "2012-12-09T12:23:50Z");
        componentScopedCache.put("lastURI", "https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc");

        decorator.setURI(getMethod, contextObj);

        Assert.assertEquals("http://fallbackUri/", getMethod.getURI().toString());
    }

    @Test
    public void test_getLastQueryDate() throws FoxWeaveException {
        Assert.assertEquals("2012-12-08", CreatedAfterPaginatedRequestDecorator.getLastQueryDate("https://nac.zendesk.com/api/v2/search.json?page=2&per_page=1&query=type%3Aticket+created%3E2012-12-08T12%3A23%3A50Z&sort_by=created_at&sort_order=asc"));
    }
}
