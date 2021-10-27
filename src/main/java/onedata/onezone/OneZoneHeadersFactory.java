package onedata.onezone;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import egi.eu.ActionConfig;


@ApplicationScoped
public class OneZoneHeadersFactory implements ClientHeadersFactory {

    @Inject
    ActionConfig config;


    /**
     * Add the auth header to calls to the OneZone REST API.
     * @param incomingHeaders The incoming HTTP headers
     * @param clientOutgoingHeaders The outgoing HTTP headers
     * @return Adjust HTTP headers collection
     */
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("X-Auth-Token", config.zoneToken());
        return result;
    }
}
