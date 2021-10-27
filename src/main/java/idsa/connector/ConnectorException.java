package idsa.connector;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


/**
 * Exception class for IDSA Connector API calls
 */
public class ConnectorException extends WebApplicationException {

    private String responseBody;

    public ConnectorException() {
        super();
    }

    public ConnectorException(Response response, String responseBody) {
        super(response);
        this.responseBody = responseBody;
    }

    String responseBody() { return responseBody; }
}
