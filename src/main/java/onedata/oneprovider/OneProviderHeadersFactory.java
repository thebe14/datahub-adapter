package onedata.oneprovider;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import egi.eu.ActionConfig;


@ApplicationScoped
public class OneProviderHeadersFactory implements ClientHeadersFactory {

    @Inject
    ActionConfig config;

    /**
     * Add authorization header to OneProvider REST API calls.
     * @param incomingHeaders The incoming HTTP headers
     * @param clientOutgoingHeaders The outgoing HTTP headers
     * @return Adjust HTTP headers collection
     */
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("X-Auth-Token", config.providerToken());
        return result;
    }
}
