package onedata.oneprovider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


/**
 * Exception class for OneProvider API calls
 */
public class OneProviderException extends WebApplicationException {

    private String responseBody;

    public OneProviderException() {
        super();
    }

    public OneProviderException(Response response, String responseBody) {
        super(response);
        this.responseBody = responseBody;
    }

    String responseBody() { return responseBody; }
}
