package onedata.onezone;

import onedata.onezone.model.*;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("/api/v3/onezone")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(value = OneZoneExceptionMapper.class)
@RegisterClientHeaders(OneZoneHeadersFactory.class)
public interface OneZone {

    @GET
    @Path("/user")
    Uni<User> getCurrentUserAsync();

    @GET
    @Path("/spaces/{spaceId}")
    Uni<Space> getSpaceAsync(@PathParam String spaceId);

    @GET
    @Path("/providers/{providerId}")
    Uni<Provider> getProviderAsync(@PathParam String providerId);

    @POST
    @Path("/user/tokens/temporary")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Token> getRestrictedAccessTokenAsync(TokenRequest request);
}
