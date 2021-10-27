package idsa.connector;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import idsa.connector.model.*;

import java.util.List;


@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(value = ConnectorExceptionMapper.class)
@RegisterClientHeaders(ConnectorHeadersFactory.class)
public interface Connector {

    public static final String APPLICATION_HALJSON = "application/hal+json";

    @GET
    @Path("/configurations/active")
    @Produces(APPLICATION_HALJSON)
    Uni<Configuration> getActiveConfigurationAsync();

    @GET
    @Path("/catalogs/{catalogId}")
    Uni<Catalog> getCatalogAsync(@PathParam String catalogId);

    @POST
    @Path("/catalogs")
    Uni<Catalog> createCatalogAsync(CatalogDescription catalog);

    @POST
    @Path("/offers")
    Uni<Resource> createResourceAsync(ResourceDescription resource);

    @POST
    @Path("/representations")
    Uni<Representation> createRepresentationAsync(RepresentationBase representation);

    @POST
    @Path("/artifacts")
    Uni<Artifact> createArtifactAsync(ArtifactDescription artifact);

    @POST
    @Path("/contracts")
    Uni<Contract> createContractAsync(ContractDescription contract);

    @POST
    @Path("/rules")
    Uni<Rule> createRuleAsync(RuleDescription rule);

    @POST
    @Path("/representations/{representationId}/artifacts")
    @Produces(APPLICATION_HALJSON)
    Uni<Embedded<Artifact>> addArtifactsToRepresentationAsync(@PathParam String representationId, List<String> artifacts);

    @POST
    @Path("/offers/{resourceId}/representations")
    @Produces(APPLICATION_HALJSON)
    Uni<Embedded<Representation>> addRepresentationsToResourceAsync(@PathParam String resourceId, List<String> representations);

    @POST
    @Path("/contracts/{contractId}/rules")
    @Produces(APPLICATION_HALJSON)
    Uni<Embedded<Rule>> addRulesToContractAsync(@PathParam String contractId, List<String> rules);

    @POST
    @Path("/offers/{resourceId}/contracts")
    @Produces(APPLICATION_HALJSON)
    Uni<Embedded<Contract>> addContractsToResourceAsync(@PathParam String resourceId, List<String> contracts);

    @POST
    @Path("/catalogs/{catalogId}/offers")
    @Produces(APPLICATION_HALJSON)
    Uni<Embedded<Resource>> addResourcesToCatalogAsync(@PathParam String catalogId, List<String> resources);
}
