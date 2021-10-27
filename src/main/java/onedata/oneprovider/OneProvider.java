package onedata.oneprovider;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import io.smallrye.mutiny.Uni;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import onedata.oneprovider.model.*;


@Path("/api/v3/oneprovider")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(value = OneProviderExceptionMapper.class)
@RegisterClientHeaders(OneProviderHeadersFactory.class)
public interface OneProvider {

    @GET
    @Path("/spaces/{spaceId}")
    Uni<SpaceRoot> getSpaceAsync(@PathParam String spaceId);

    @GET
    @Path("/data/{fileId}/children")
    Uni<Folder> listFolderContentAsync(@PathParam String fileId);

    @GET
    @Path("/data/{fileId}")
    Uni<File> getFileAsync(@PathParam String fileId);

    @GET
    @Path("/data/{fileId}/metadata/xattrs")
    Uni<Map<String, String>> getFileMetadataAsync(@PathParam String fileId);

    @PUT
    @Path("/data/{fileId}/metadata/xattrs")
    @Consumes(MediaType.APPLICATION_JSON)
    Uni<Response> setFileMetadataAsync(@PathParam String fileId, Map<String, String> metadata);
}
