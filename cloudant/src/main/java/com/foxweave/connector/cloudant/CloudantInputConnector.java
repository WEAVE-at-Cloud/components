package com.foxweave.connector.cloudant;

import com.foxweave.connector.cloudant.query.ChangeQueryHandler;
import com.foxweave.connector.cloudant.query.ViewQueryHandler;
import com.foxweave.exception.ComponentConfigurationException;
import com.foxweave.pipeline.component.PollContext;
import com.foxweave.pipeline.component.PollingInputConnector;
import com.foxweave.pipeline.exchange.ExchangeFactory;
import com.foxweave.pipeline.exchange.Message;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloudantInputConnector extends AbstractCloudantConnector implements PollingInputConnector {

    private static final Logger logger = LoggerFactory.getLogger(CloudantInputConnector.class);

    public ExchangeFactory exchangeFactory;
    public PollContext pollContext;
    private InputQueryHandler queryHandler;

    private enum QueryType {
        _changes,
        view
    }

    @Override
    public void setConfiguration(JSONObject config) throws ComponentConfigurationException {
        super.setConfiguration(config);

        if (getQueryType() == QueryType._changes) {
            queryHandler = new ChangeQueryHandler();
        } else {
            queryHandler = new ViewQueryHandler();
        }
        queryHandler.setCloudantInputConnector(this);

        logger.debug("Using InputQueryHandler '{}'.", queryHandler.getClass().getName());
    }

    @Override
    public String getRESTResource() throws ComponentConfigurationException {
        if (getQueryType() == QueryType._changes) {
            return "/_changes";
        } else {
            // It's a view.  Return the view path...
            try {
                return config.getString("viewPath");
            } catch (JSONException e) {
                throw new ComponentConfigurationException("Missing 'viewPath' configuration.");
            }
        }
    }

    private QueryType getQueryType() {
        String queryType = config.optString("queryType", QueryType._changes.name());
        if (queryType.equals(QueryType._changes.name())) {
            return QueryType._changes;
        } else {
            return QueryType.view;
        }
    }

    @Override
    public void setExchangeFactory(ExchangeFactory exchangeFactory) {
        this.exchangeFactory = exchangeFactory;
    }

    @Override
    public void replay(Message message) throws Exception {
    }

    @Override
    public void setPollContext(PollContext pollContext) {
        this.pollContext = pollContext;
    }

    @Override
    public void poll() throws Exception {
        queryHandler.poll();
    }
}
