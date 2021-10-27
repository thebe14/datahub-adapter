package onedata.onezone;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


/**
 * Exception class for OneZone API calls
 */
public class OneZoneException extends WebApplicationException {

    private String responseBody;

    public OneZoneException() {
        super();
    }

    public OneZoneException(Response response, String responseBody) {
        super(response);
        this.responseBody = responseBody;
    }

    String responseBody() { return responseBody; }
}
