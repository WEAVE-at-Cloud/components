package com.foxweave.connector.cloudant;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface InputQueryHandler {

    void setCloudantInputConnector(CloudantInputConnector cloudantInputConnector);
    void poll() throws Exception;
}
