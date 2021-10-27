package idsa.connector;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Base64;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;


@ApplicationScoped
public class ConnectorHeadersFactory implements ClientHeadersFactory {

    @Inject
    ConnectorConfig config;


    /**
     * Build a basic authorization header.
     * See also https://en.wikipedia.org/wiki/Basic_access_authentication
     * @return Basic authorization header
     */
    private String getBasicAuthorization() {
        String userPass = config.username() + ":" + config.password();
        return "Basic " + Base64.getEncoder().encodeToString(userPass.getBytes());
    }

    /**
     * Add authorization header to IDSA Connector REST API calls.
     * @param incomingHeaders The incoming HTTP headers
     * @param clientOutgoingHeaders The outgoing HTTP headers
     * @return Adjust HTTP headers collection
     */
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("Authorization", getBasicAuthorization());
        return result;
    }
}
