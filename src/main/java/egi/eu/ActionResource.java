package egi.eu;

import idsa.connector.ConnectorConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.tuples.Tuple2;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import java.net.URL;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import idsa.connector.Connector;
import idsa.connector.model.*;
import onedata.onezone.OneZone;
import onedata.onezone.model.*;
import onedata.oneprovider.OneProvider;
import onedata.oneprovider.model.*;
import egi.eu.model.*;
import egi.eu.model.PublishPolicy.PolicyType;


@Path("/action")
@Produces(MediaType.APPLICATION_JSON)
public class ActionResource {

    private static final String SEPARATOR = "/";
    private static final String META_RESOURCE_PREFIX = "idsa:resource:";
    private static final String META_ARTIFACT_PREFIX = "idsa:artifact:";
    private static final String META_VALIDTO_PREFIX = "idsa:valid:";

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Expects UTC
    private static final Logger LOG = Logger.getLogger(ActionResource.class);

    @Inject
    ActionConfig actionConfig;

    @Inject
    ConnectorConfig connectorConfig;

    private Connector connector;
    private OneZone onezone;
    private Map<String, OneProvider> providers;

    /**
     * Prepare REST clients for DataHub and the IDSA connector.
     * @param params Will receive the REST clients
     * @return true on success, updates fields "onezone" and "connector" of params
     */
    @PostConstruct
    private boolean getRestClients(ActionParameters params) {

        // Check if the URLs are valid
        URL urlConnector, urlOneZone;
        try {
            urlConnector = new URL(connectorConfig.connectorBaseUrl());
            urlOneZone = new URL(actionConfig.zoneBaseUrl());
        }
        catch (MalformedURLException e) {
            LOG.error(e.getMessage());
            return false;
        }

        if(null == connector)
            // Create the connector client with the configured base URL
            connector = RestClientBuilder.newBuilder()
                    .baseUrl(urlConnector)
                    .build(Connector.class);

        params.connector = connector;

        // Create the OneZone client with the configured base URL
        if(null == onezone)
            onezone = RestClientBuilder.newBuilder()
                    .baseUrl(urlOneZone)
                    .build(OneZone.class);

        params.onezone = onezone;

        return true;
    }

    /**
     * Prepare REST client for the provider with specified base URL.
     * @param params Holds the details of the base URL (schema, domain, and root path)
     *               for a OneProvider service we need a REST client for
     * @return true on success, updates field provider of params
     */
    @PostConstruct
    private boolean getProviderClient(ActionParameters params) {

        // Check if the URLs are valid
        URL urlProvider;
        try {
            urlProvider = new URL(params.providerBaseUrl);
        }
        catch (MalformedURLException e) {
            LOG.error(e.getMessage());
            return false;
        }

        // Check if we already have a provider client with this base URL
        if(null == providers)
            providers = new HashMap<>();
        else if(providers.containsKey(params.providerBaseUrl)) {
            params.provider = providers.get(params.providerBaseUrl);
            return true;
        }

        // Create and return new provider client
        OneProvider provider = RestClientBuilder.newBuilder()
                .baseUrl(urlProvider)
                .build(OneProvider.class);

        providers.put(params.providerBaseUrl, provider);

        params.provider = providers.get(params.providerBaseUrl);
        return true;
    }

    /**
     * Updates (the access token of) a file already published to an IDSA connector.
     * @param params Holds the id of the file to publish, the provider from where to get info
     *               about the file, the connector where to publish it, and the catalog in which
     *               to create a resource for this file. It also expects the current medatadata
     *               of the file in field "metadata".
     * @return ActionParameters with the field "response" having status code RESET_CONTENT if the
     * file was updated, or wraps an ActionError entity on failure.
     * The field "metadata" is updated in on success.
     */
    private static Uni<ActionParameters> updateFileImpl(ActionParameters params) {

        LOG.infof("Updating file %s/%s (%s)",
                params.path.length() > 1 ? params.path : "",
                params.item.name,
                params.item.fileId);

        try {
            CompletableFuture<Response> response = new CompletableFuture<>();

            Uni<String> start = Uni.createFrom().item(params.item.fileId);
            start
                // Get a restricted t
                .onItem().transformToUni(fid -> params.provider.getFileMetadataAsync(fid))
                .onItem().transformToUni(fmd -> {
                    // Got file metadata
                    params.metadata = fmd;

                    // Check if this file is already published
                    for(var e : fmd.entrySet()) {
                        LOG.debugf("Metadata: %s -> %s", e.getKey(), e.getValue());
                    }

                    if(fmd.containsKey(META_RESOURCE_PREFIX + params.connectorId)) {
                        // File is already published to this connector, update it instead
                        //return;
                    }
                    return Uni.createFrom().item("");
                })
                .onItem().transformToUni(x -> {
                    return Uni.createFrom().item("");
                })
                .onItem().transformToUni(x -> {
                    return Uni.createFrom().item(Response.ok().build());
                })
                .onItem().transformToUni(finish -> {
                    // File metadata was updated
                    // Check previous operation
                    if(Family.SUCCESSFUL != Family.familyOf(finish.getStatus())) {
                        response.complete(finish);
                        return ActionParameters.failedUni();
                    }

                    // Success
                    LOG.infof("Updated file  %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    response.complete(Response.status(Status.RESET_CONTENT).build());
                    return Uni.createFrom().nullItem();
                })
                .onFailure().invoke(e -> {
                    LOG.errorf("Failed to update file %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    if(!response.isDone())
                        response.complete(new ActionError(e, Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("fileId", params.item.fileId),
                                Tuple2.of("fileName", params.item.name),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl)) )
                                .toResponse());
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("Cancelled updating file %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);
                })
                .subscribe().with(unused -> {});

            // Wait until file is updated (possibly with error)
            params.response = response.get();
        } catch (InterruptedException e) {
            // Cancelled
            params.response = new ActionError("updateFileImplInterrupted", Tuple2.of("fileId", params.item.fileId)).toResponse();
        } catch (ExecutionException e) {
            // Execution error
            params.response = new ActionError("updateFileImplExecutionError", Tuple2.of("fileId", params.item.fileId)).toResponse();
        }

        return Uni.createFrom().item(params);
    }

    /**
     * Publish a file to an IDSA connector.
     * @param params Holds the id of the file to publish, the provider from where to get info
     *               about the file, the connector where to publish it, and the catalog in which
     *               to create a resource for this file. It also expects the current medatadata
     *               of the file in field "metadata".
     * @return ActionParameters with the field "response" having status code OK on success or
     * wraps an ActionError entity on failure.
     * The field "metadata" is filled in on success.
     */
    private static Uni<ActionParameters> publishFileImpl(ActionParameters params) {

        LOG.infof("Publishing file %s/%s (%s)",
                params.path.length() > 1 ? params.path : "",
                params.item.name,
                params.item.fileId);

        try {
            CompletableFuture<Response> response = new CompletableFuture<>();

            Uni<String> start = Uni.createFrom().item(params.item.fileId);
            start
                .onItem().transformToUni(fid -> {
                    // Get access token restricted to this file
                    var validUntil = LocalDateTime.now().plusDays(params.fileTokenValidityDays);
                    TokenRequest tr = new TokenRequest(Arrays.asList(
                                                new TokenRequest.Caveat(),
                                                new TokenRequest.Caveat(fid),
                                                new TokenRequest.Caveat(validUntil)));

                    params.metadata.put(META_VALIDTO_PREFIX + params.connectorId,
                                        dateTimeFormat.format(Date.from(validUntil.toInstant(ZoneOffset.UTC))));

                    return params.onezone.getRestrictedAccessTokenAsync(tr);
                })
                .onItem().transformToUni(token -> {
                    // Got file access token
                    // Create artifact for this file
                    String fileContentUrl = String.format("%s/api/v3/oneprovider/data/%s/content",
                                                params.providerBaseUrl,
                                                params.item.fileId);
                    ArtifactDescription afd = new ArtifactDescription(params.item.name, fileContentUrl, token.token);

                    return params.connector.createArtifactAsync(afd);
                })
                .onItem().transformToUni(artifact -> {
                    // Got artifact
                    params.artifactId = artifact.extractId();
                    params.metadata.put(META_ARTIFACT_PREFIX + params.connectorId, params.artifactId);

                    // Create representation for this file
                    RepresentationDescription rpr = new RepresentationDescription(params.item.name, params.language);

                    return params.connector.createRepresentationAsync(rpr);
                })
                .onItem().transformToUni(representation -> {
                    // Got representation
                    params.representationId = representation.extractId();

                    // Add the artifact to the representation
                    return params.connector.addArtifactsToRepresentationAsync(params.representationId, Arrays.asList(params.artifactId));
                })
                .onItem().transformToUni(artifacts -> {
                    // Artifact was added to representation
                    // Create resource for this file
                    ResourceDescription res = new ResourceDescription(params.item.name, params.path);
                    res.publisher = params.publisher;
                    res.sovereign = params.sovereign;
                    res.language = params.language;
                    res.license = params.license;
                    res.keywords = (null != params.keywords) ? new ArrayList<String>() : null;
                    if(null != res.keywords)
                        res.keywords.addAll(params.keywords);

                    return params.connector.createResourceAsync(res);
                })
                .onItem().transformToUni(resource -> {
                    // Got resource
                    params.resourceId = resource.extractId();
                    params.metadata.put(META_RESOURCE_PREFIX + params.connectorId, params.resourceId);

                    // Add the representation to the resource
                    return params.connector.addRepresentationsToResourceAsync(params.resourceId, Arrays.asList(params.representationId));
                })
                .onItem().transformToUni(representations -> {
                    // Representation was added to resource
                    // Create contract for resource
                    ContractDescription rc = new ContractDescription(params.item.name, params.path);

                    return params.connector.createContractAsync(rc);
                })
                .onItem().transformToUni(contract -> {
                    // Got contract
                    params.contractId = contract.extractId();

                    // Create usage policy for resource
                    String title = "Usage policy";
                    Policy policy = null;
                    switch(params.policy.type) {
                        case PROHIBIT:
                            title = "Prohibit use";
                            policy = Policy.ProhibitUse(title);
                            break;

                        case COUNTED:
                            title = String.format("Allow use %d times", params.policy.useCount);
                            policy = Policy.CountedUse(params.policy.useCount ,title);
                            break;

                        case INTERVAL:
                            title = String.format("Allow use from %s to %s",
                                    dateTimeFormat.format(params.policy.useFrom),
                                    dateTimeFormat.format(params.policy.useUntil));
                            policy = Policy.IntervalUse(params.policy.useFrom, params.policy.useUntil, title);
                            break;

                        case INTERVAL_DELETE:
                            title = String.format("Allow use from %s to %s and delete at %s",
                                    dateTimeFormat.format(params.policy.useFrom),
                                    dateTimeFormat.format(params.policy.useUntil),
                                    dateTimeFormat.format(params.policy.deleteAt));
                            policy = Policy.IntervalUseDeleteAt(params.policy.useFrom, params.policy.useUntil, params.policy.deleteAt, title);
                            break;

                        case DURATION:
                            title = String.format("Allow use for %s", params.policy.useFor);
                            policy = Policy.DurationUse(params.policy.useFor, title);
                            break;

                        case NOTIFY:
                            title = "Allow use with notification";
                            policy = Policy.NotifyUse(params.policy.notifyMessage, params.policy.notifyLink, title);
                            break;

                        case LOG:
                            title = "Allow use with logging";
                            policy = Policy.LogUse(title);
                            break;

                        default:
                            title = "Allow use";
                            policy = Policy.FreeUse(title);
                            break;
                    }

                    RuleDescription rule = null;
                    try {
                        rule = new RuleDescription(title, policy);
                    } catch(JsonProcessingException e) {
                        LOG.error(e);
                        response.complete(new ActionError("policyToJsonString", Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("fileId", params.item.fileId),
                                Tuple2.of("fileName", params.item.name)) )
                                .toResponse(Status.EXPECTATION_FAILED));
                        return Uni.createFrom().failure(new RuntimeException());
                    }

                    // Create rule using the policy
                    return params.connector.createRuleAsync(rule);
                })
                .onItem().transformToUni(rule -> {
                    // Got rule
                    params.ruleId = rule.extractId();

                    // Add the rule to the contract
                    return params.connector.addRulesToContractAsync(params.contractId, Arrays.asList(params.ruleId));
                })
                .onItem().transformToUni(rules -> {
                    // Rule was added to contract
                    // Add the contract to the resource
                    return params.connector.addContractsToResourceAsync(params.resourceId, Arrays.asList(params.contractId));
                })
                .onItem().transformToUni(contracts -> {
                    // Contract was added to resource
                    // Add the resource to the catalog
                    return params.connector.addResourcesToCatalogAsync(params.catalogId, Arrays.asList(params.resourceId));
                })
                .onItem().transformToUni(resources -> {
                    // Resource was added to catalog
                    // This concludes publishing the file to the connector
                    // Update file metadata
                    return params.provider.setFileMetadataAsync(params.item.fileId, params.metadata);
                })
                .onItem().transformToUni(finish -> {
                    // File metadata was updated
                    // Check previous operation
                    if(Family.SUCCESSFUL != Family.familyOf(finish.getStatus())) {
                        response.complete(finish);
                        return ActionParameters.failedUni();
                    }

                    // Success
                    LOG.infof("Published file  %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    response.complete(Response.status(Status.OK).build());
                    return Uni.createFrom().item("");
                })
                .onFailure().invoke(e -> {
                    LOG.error(e);
                    LOG.errorf("Failed to publish file %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    if(!response.isDone())
                        response.complete(new ActionError(e, Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("fileId", params.item.fileId),
                                Tuple2.of("fileName", params.item.name),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl)) )
                                .toResponse());
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("Cancelled publishing file %s/%s (%s)",
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);
                })
                .subscribe().with(unused -> {});

            // Wait until file is published (possibly with error)
            params.response = response.get();
        } catch (InterruptedException e) {
            // Cancelled
            params.response = new ActionError("publishFileInterrupted", Tuple2.of("fileId", params.item.fileId)).toResponse();
        } catch (ExecutionException e) {
            // Execution error
            params.response = new ActionError("publishFileExecutionError", Tuple2.of("fileId", params.item.fileId)).toResponse();
        }

        return Uni.createFrom().item(params);
    }

    /**
     * Publish a file to an IDSA connector.
     * @param params Holds the id of the file to publish, the provider from where to get info
     *               about the file, the connector where to publish it, and the catalog in which
     *               to create a resource for this file
     * @return ActionParameters with the field "response" having status code OK if the file was published
     * for the first time, RESET_CONTENT if the file was updated, or wraps an ActionError entity on failure.
     * The field "metadata" is filled in on success.
     */
    private Function<ActionParameters, Uni<ActionParameters>> publishFile = (params) -> {

        LOG.infof("Checking if already published file %s/%s (%s)",
                params.path.length() > 1 ? params.path : "",
                params.item.name,
                params.item.fileId);

        try {
            CompletableFuture<Response> response = new CompletableFuture<>();

            Uni<String> start = Uni.createFrom().item(params.item.fileId);
            start
                // Get file metadata
                .onItem().transformToUni(fid -> params.provider.getFileMetadataAsync(fid))
                .onItem().transformToUni(fmd -> {
                    // Got file metadata
                    params.metadata = fmd;

                    // Check if this file is already published
                    for(var e : fmd.entrySet()) {
                        LOG.debugf("Metadata: %s -> %s", e.getKey(), e.getValue());
                    }

                    // TODO Uncomment the condition below after this bug is resolved:
                    // https://github.com/International-Data-Spaces-Association/DataspaceConnector/issues/670
                    if(false && fmd.containsKey(META_RESOURCE_PREFIX + params.connectorId))
                        // File is already published to this connector, update it instead
                        return ActionResource.updateFileImpl(params);

                    return ActionResource.publishFileImpl(params);
                })
                .onItem().transformToUni(publishedParams -> {
                    // Check previous operation
                    if(Family.SUCCESSFUL != Family.familyOf(publishedParams.response.getStatus())) {
                        response.complete(publishedParams.response);
                        return ActionParameters.failedUni();
                    }

                    // Success
                    response.complete(publishedParams.response);
                    return Uni.createFrom().nullItem();
                })
                .onFailure().invoke(e -> {
                    if(!response.isDone())
                        response.complete(new ActionError(e, Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("fileId", params.item.fileId),
                                Tuple2.of("fileName", params.item.name),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl)) )
                                .toResponse());
                })
                .subscribe().with(unused -> {});

            // Wait until file is published (possibly with error)
            params.response = response.get();
        } catch (InterruptedException e) {
            // Cancelled
            params.response = new ActionError("publishFileInterrupted", Tuple2.of("fileId", params.item.fileId)).toResponse();
        } catch (ExecutionException e) {
            // Execution error
            params.response = new ActionError("publishFileExecutionError", Tuple2.of("fileId", params.item.fileId)).toResponse();
        }

        return Uni.createFrom().item(params);
    };

    /***
     * Handle files from a folder (and all subfolders).
     * @param params Holds the details of the folder to handle, the details of space where the folder belongs,
     *               the details of the provider from where to get info about files, and the connector to publish to
     * @param fileHandler is the handler to be called for each file in the folder
     * @param opName the name of the operation to be performed on each file in the space
     * @return ActionParameters with the field "response" wrapping an ActionSuccess or an ActionError entity
     */
    private static Uni<ActionParameters> handleFolder(ActionParameters params,
                                                      Function<ActionParameters, Uni<ActionParameters>> fileHandler,
                                                      String opName) {

        LOG.infof("%sing content of folder %s/%s (%s)",
                opName,
                params.path.length() > 1 ? params.path : "",
                params.item.name,
                params.item.fileId);

        try {
            ActionSuccess result = new ActionSuccess();
            List<Folder.Item> folders = new ArrayList<>();
            CompletableFuture<ActionParameters> ap = new CompletableFuture<>();

            Uni<String> start = Uni.createFrom().item(params.item.fileId);
            start
                // List content of the folder
                .onItem().transformToUni(fid -> params.provider.listFolderContentAsync(fid))
                .onItem().transformToMulti(dir -> {
                    // Got listing of folder items
                    // Turn the list of items into a stream
                    return Multi.createFrom().iterable(dir.children);
                })
                .onItem().transformToUniAndConcatenate(item -> {
                    // Got a folder item
                    // Get details about folder item
                    return params.provider.getFileAsync(item.fileId);
                })
                .onItem().transformToUniAndConcatenate(file -> {
                    // Got item details
                    ActionParameters fileParams = new ActionParameters(params);
                    fileParams.item = new Folder.Item(file.name, file.fileId);
                    fileParams.path = ((1 == params.path.length()) ? params.path : (params.path + SEPARATOR)) + params.item.name;
                    fileParams.type = file.type;

                    // Check the type of the item
                    if(file.type.equals(File.TYPE_FOLDER))
                        // This is a subfolder, recurse into it
                        return handleFolder(fileParams, fileHandler, opName);
                    else if(file.size > 0)
                        // This is a file, handle it
                        return fileHandler.apply(fileParams);

                    // We will ignore empty files
                    LOG.infof("Ignoring empty file %s (%s)", file.name, file.fileId);
                    fileParams.type = File.TYPE_IGNORE;
                    return Uni.createFrom().item(fileParams);
                })
                .onItem().transformToUniAndConcatenate(handledParams -> {
                    // Folder item was handled
                    // Check previous operation
                    if(Family.SUCCESSFUL != Family.familyOf(handledParams.response.getStatus())) {
                        ap.complete(handledParams);
                        return ActionParameters.failedUni();
                    }

                    if(handledParams.type.equals(File.TYPE_FOLDER)) {
                        // Subfolder was handled
                        result._subFolders.incrementAndGet();

                        // Count files handled in this subfolder
                        if(handledParams.response.getEntity().getClass().equals(ActionSuccess.class)) {
                            ActionSuccess as = (ActionSuccess)handledParams.response.getEntity();
                            result._newFiles.addAndGet(as._newFiles.get());
                            result._updatedFiles.addAndGet(as._updatedFiles.get());
                        }
                    }
                    else if(handledParams.type.equals(File.TYPE_FILE)) {
                        // File was handled, count it
                        if(Status.OK == Status.fromStatusCode(handledParams.response.getStatus()))
                            result._newFiles.incrementAndGet();
                        else
                            result._updatedFiles.incrementAndGet();
                    }

                    // Success for this item
                    return Uni.createFrom().nullItem();
                })
                .onCompletion().invoke(() -> {
                    // Success for all items
                    LOG.errorf("%sing complete for folder %s/%s (%s)",
                            opName,
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    result.publishSummary();
                    result.spaceId = params.spaceId;

                    params.response = result.toResponse();
                    ap.complete(params);
                })
                .onFailure().invoke(e -> {
                    LOG.errorf("%sing failed for folder %s/%s (%s)",
                            opName,
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);

                    if(!ap.isDone()) {
                        ActionParameters errorParams = new ActionParameters(params);
                        errorParams.response = new ActionError(e, Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("folderId", params.item.fileId),
                                Tuple2.of("folderName", params.item.name),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl)) )
                                .toResponse();
                        ap.complete(errorParams);
                    }
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("%sing cancelled for folder %s/%s (%s)",
                            opName,
                            params.path.length() > 1 ? params.path : "",
                            params.item.name,
                            params.item.fileId);
                })
                .subscribe().with(unused -> {});

            // Wait until folder is handled (possibly with error)
            ActionParameters handledParams = ap.get();
            return Uni.createFrom().item(handledParams);
        } catch (InterruptedException e) {
            // Cancelled
            params.response = new ActionError("handleFolderInterrupted", Tuple2.of("folderId", params.item.fileId)).toResponse();
        } catch (ExecutionException e) {
            // Execution error
            params.response = new ActionError("handleFolderExecutionError", Tuple2.of("folderId", params.item.fileId)).toResponse();
        }

        return Uni.createFrom().item(params);
    }

    /**
     * Make sure a catalog exists in the IDSA connector for our space.
     * @param params Holds the details of the space to handle, the details of the provider
     *               from where to get info about files, and the connector to publish to
     * @param create Is true to create a catalog, false to check if the one saved to
     *               the root folder's metadata still exists. When asked to create the
     *               catalog and the space was published in the past, but the catalog no
     *               longer exists, it creates a new catalog and updates the metadata
     *               of the root folder.
     * @return ActionParameters with the field "response" having status code OK if the catalog
     * already exists, CREATED if it was just created, or wraps an ActionError entity on failure.
     * The field "catalogId" is filled in on success.
     */
    private static Uni<ActionParameters> validateCatalog(ActionParameters params, boolean create) {

        LOG.infof("%s catalog of space %s (%s)",
                create ? "Creating" : "Checking",
                params.spaceName,
                params.spaceId);

        try {
            CompletableFuture<Response> response = new CompletableFuture<>();
            Uni<Void> start = Uni.createFrom().voidItem();
            start
                // Get connector configuration
                .onItem().transformToUni(unused -> params.connector.getActiveConfigurationAsync())
                .onItem().transformToUni(cfg -> {
                    // Got connector configuration
                    params.connectorId = cfg.connectorId;

                    // Get root folder metadata
                    return params.provider.getFileMetadataAsync(params.item.fileId);
                })
                .onItem().transformToUni(fmd -> {
                    // Got root folder metadata
                    params.metadata = fmd;
                    LOG.infof("Space root folder is %s", params.item.fileId);
                    for (var e : fmd.entrySet()) {
                        LOG.infof("Metadata: %s -> %s", e.getKey(), e.getValue());
                    }

                    // Check if root folder is already published
                    CompletableFuture<Optional<Catalog>> opCat = new CompletableFuture<>();

                    if(fmd.containsKey(META_RESOURCE_PREFIX + params.connectorId)) {
                        // Root folder is already published to this connector,
                        // validate catalog still exists
                        LOG.infof("Space %s already published to connector %s",
                                params.spaceName,
                                params.connectorId);

                        params.catalogId = fmd.get(META_RESOURCE_PREFIX + params.connectorId);
                        params.connector.getCatalogAsync(params.catalogId)
                            .onFailure().invoke(catf -> {
                                // Catalog does not exist
                                params.response = new ActionError(catf, Arrays.asList(
                                        Tuple2.of("spaceName", params.spaceName),
                                        Tuple2.of("catalogId", params.catalogId)))
                                        .toResponse();
                                params.catalogId = null;

                                if(Status.NOT_FOUND == Status.fromStatusCode(params.response.getStatus())) {
                                    LOG.infof("Catalog for space %s no longer exists", params.spaceName);
                                    params.response = Response.ok().status(Status.CREATED).build();
                                    opCat.complete(Optional.empty());
                                } else
                                    opCat.complete(null);
                            })
                            .subscribe().with(cat -> {
                                // Catalog exists
                                LOG.infof("Catalog for space %s exists and is %s",
                                        params.spaceName,
                                        params.catalogId);

                                params.catalogId = cat.extractId();
                                opCat.complete(Optional.of(cat));
                            }); // Subscribed to catalog info
                    } else
                        opCat.complete(Optional.empty());

                    // Wait until catalog check is ready
                    Optional<Catalog> catalog = null;
                    try {
                        catalog = opCat.get();
                    }
                    catch(InterruptedException e) {
                        // Catalog check cancelled
                        params.response = new ActionError("catalogCheckInterrupted",
                                Tuple2.of("fileId", params.item.fileId)).toResponse();
                    } catch(ExecutionException e) {
                        // Catalog check execution error
                        params.response = new ActionError("catalogCheckExecutionError",
                                Tuple2.of("fileId", params.item.fileId)).toResponse();
                    }

                    return Uni.createFrom().item(catalog);
                })
                .onItem().transformToUni(opCat -> {
                    // Got a catalog, if there was one already for this space
                    // Check previous operation
                    if(params.failed()) {
                        response.complete(params.response);
                        return ActionParameters.failedUni();
                    }

                    if(null != opCat && opCat.isPresent())
                        // If we have a catalog, pass it to the next stage
                        return Uni.createFrom().item(opCat.get());

                    // Create new catalog for this space
                    LOG.infof("Creating new catalog for space %s", params.spaceName);
                    return params.connector.createCatalogAsync(new CatalogDescription(params.spaceName));
                })
                .onItem().transformToUni(cat -> {
                    // Got the catalog for this space
                    params.catalogId = cat.extractId();

                    // Store the catalog id as metadata on the root folder
                    params.metadata.put(META_RESOURCE_PREFIX + params.connectorId, params.catalogId);
                    return params.provider.setFileMetadataAsync(params.item.fileId, params.metadata);
                })
                .onItem().transformToUni(finish -> {
                    // Root folder metadata was updated
                    // Check previous operation
                    if(Family.SUCCESSFUL != Family.familyOf(finish.getStatus())) {
                        response.complete(finish);
                        return ActionParameters.failedUni();
                    }

                    // Success
                    response.complete(finish);
                    return Uni.createFrom().nullItem();
                })
                .onFailure().invoke(e -> {
                    LOG.errorf("Failed to validate catalog for space %s", params.spaceId);
                    if(!response.isDone())
                        response.complete(new ActionError(e, Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("folderId", params.item.fileId),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl)) )
                                .toResponse());
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("Cancelled validation of catalog for space %s", params.spaceId);
                })
                .subscribe().with(unused -> {});

            // Wait until validation finishes (possibly with error)
            params.response = response.get();
            params.metadata.clear();
        } catch(InterruptedException e) {
            // Cancelled
            params.response = new ActionError("validateCatalogInterrupted", Tuple2.of("spaceId", params.spaceId)).toResponse();
        } catch(ExecutionException e) {
            // Execution error
            params.response = new ActionError("validateCatalogExecutionError", Tuple2.of("spaceId", params.spaceId)).toResponse();
        }

        return Uni.createFrom().item(params);
    }

    /**
     * Get the details of a space and create a REST client for the first OneProvider
     * that supports the space.
     * @param params Holds the "spaceId" of the space to get details for
     * @return ActionParameters with the response field wrapping an ActionError entity on failure.
     * The details of the space and provider are filled in on success.
     */
    private Uni<ActionParameters> getSpaceInfo(ActionParameters params) {

        LOG.infof("Get details of space %s", params.spaceId);

        try {
            CompletableFuture<Response> response = new CompletableFuture<>();
            Uni<String> start = Uni.createFrom().item(params.spaceId);
            start
                // Get the details of the space
                .onItem().transformToUni(sid -> params.onezone.getSpaceAsync(sid))
                .onItem().transformToUni(s -> {
                    // Got space details
                    LOG.infof("Space name is %s", s.name);
                    params.spaceName = s.name;

                    // Check if space is supported by at least one provider
                    if(null == s.providers || s.providers.isEmpty()) {
                        response.complete(new ActionError("spaceHasNoProviders",
                                Tuple2.of("spaceId", params.spaceId))
                                .toResponse(Status.EXPECTATION_FAILED));
                        return Uni.createFrom().failure(new RuntimeException());
                    }

                    // Get the details of first provider supporting the space
                    params.providerId = s.providers.keySet().iterator().next();
                    return params.onezone.getProviderAsync(params.providerId);
                })
                .onItem().transformToUni(p -> {
                    // Got provider details
                    params.providerBaseUrl = "https://" + p.domain;

                    // Create REST clients for this provider
                    if(!getProviderClient(params)) {
                        // Could not get REST client for provider
                        response.complete(new ActionError("invalidConfiguration", Arrays.asList(
                                Tuple2.of("spaceName", params.spaceName),
                                Tuple2.of("providerId", params.providerId),
                                Tuple2.of("providerBaseUrl", params.providerBaseUrl) ))
                                .toResponse(Status.EXPECTATION_FAILED));
                        return Uni.createFrom().failure(new RuntimeException());
                    }

                    // Get details of the space again, this time from the provider
                    return params.provider.getSpaceAsync(params.spaceId);
                })
                .onItem().transformToUni(sp -> {
                    // Got space details from provider
                    params.item = new Folder.Item(sp.name, sp.fileId);
                    params.path = SEPARATOR;

                    // Success
                    response.complete(Response.ok().build());
                    return Uni.createFrom().nullItem();
                })
                .onFailure().invoke(e -> {
                    LOG.errorf("Failed getting details of space %s", params.spaceId);
                    if(!response.isDone())
                        response.complete(new ActionError(e, Arrays.asList(
                            Tuple2.of("spaceId", params.spaceId),
                            Tuple2.of("spaceName", params.spaceName),
                            Tuple2.of("providerBaseUrl", params.providerBaseUrl) ))
                            .toResponse());
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("Cancelled getting details of space %s", params.spaceId);
                })
                .subscribe().with(unused -> {});

            // Wait until space details are collected (possibly with error)
            params.response = response.get();
        } catch(InterruptedException e) {
            // Cancelled
            params.response = new ActionError("getSpaceInfoInterrupted", Tuple2.of("spaceId", params.spaceId)).toResponse();
        } catch(ExecutionException e) {
            // Execution error
            params.response = new ActionError("getSpaceInfoExecutionError", Tuple2.of("spaceId", params.spaceId)).toResponse();
        }

        return Uni.createFrom().item(params);
    }

    /**
     * Publish all files from the specified space to the configured IDSA connector.
     * @param spaceId is the space to publish
     * @return API Response, wraps an ActionSuccess or an ActionError entity
     */
    @POST
    @Path("/publish/{spaceId}")
    @Operation(summary = "Publish DataHub space to an IDSA connector",
               description = "Publishes each file from a DataHub space to the configured IDSA connector.")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Success",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ActionSuccess.class))),
        @APIResponse(responseCode = "400", description="Invalid parameters",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ActionError.class))),
        @APIResponse(responseCode = "404", description="Space not found",
                content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = ActionError.class)))
    })
    public Uni<Response> publish(@Parameter(description = "The id of the space to publish", required = true)
                                 @PathParam String spaceId,
                                 @RequestBody(description = "Optional action parameters")
                                 PublishParams optionalParams) {

        LOG.infof("Publishing space %s", spaceId);

        try {
            ActionParameters ap = new ActionParameters(spaceId);
            if(null != optionalParams) {
                ap.publisher = optionalParams.publisher;
                ap.sovereign = optionalParams.sovereign;
                ap.language = optionalParams.language;
                ap.license = optionalParams.license;
                ap.keywords = (null != optionalParams.keywords) ? new ArrayList<String>() : null;
                if(null != optionalParams.keywords)
                    ap.keywords.addAll(optionalParams.keywords);

                ap.policy = (null != optionalParams.policy) ? optionalParams.policy : new PublishPolicy(PolicyType.FREE);
            }

            CompletableFuture<Response> response = new CompletableFuture<>();
            Uni<String> start = Uni.createFrom().item(spaceId);
            start
                .onItem().transformToUni(sid -> {
                    // Validate that we have all required policy details
                    switch(ap.policy.type) {
                        case COUNTED:
                            if(ap.policy.useCount <= 0) {
                                response.complete(new ActionError("invalidParameters",
                                        Tuple2.of("policy.useCount", "Field is required and must be greater than 0"))
                                        .toResponse(Status.BAD_REQUEST));
                                return Uni.createFrom().failure(new RuntimeException());
                            } break;
                        case INTERVAL:
                            if(null == ap.policy.useFrom || null == ap.policy.useUntil) {
                                var errorDetails = new ArrayList<Tuple2<String, String>>();
                                if(null == ap.policy.useFrom)
                                    errorDetails.add(Tuple2.of("policy.useFrom", "Field is required"));
                                if(null == ap.policy.useUntil)
                                    errorDetails.add(Tuple2.of("policy.useUntil", "Field is required"));

                                response.complete(new ActionError("invalidParameters", errorDetails)
                                        .toResponse(Status.BAD_REQUEST));
                                return Uni.createFrom().failure(new RuntimeException());
                            } break;
                        case INTERVAL_DELETE:
                            if(null == ap.policy.useFrom || null == ap.policy.useUntil) {
                                var errorDetails = new ArrayList<Tuple2<String, String>>();
                                if(null == ap.policy.useFrom)
                                    errorDetails.add(Tuple2.of("policy.useFrom", "Field is required"));
                                if(null == ap.policy.useUntil)
                                    errorDetails.add(Tuple2.of("policy.useUntil", "Field is required"));
                                if(null == ap.policy.deleteAt)
                                    errorDetails.add(Tuple2.of("policy.deleteAt", "Field is required"));

                                response.complete(new ActionError("invalidParameters", errorDetails)
                                        .toResponse(Status.BAD_REQUEST));
                                return Uni.createFrom().failure(new RuntimeException());
                            }
                        case DURATION:
                            if(null != ap.policy.useFor) {
                                response.complete(new ActionError("invalidParameters",
                                        Tuple2.of("policy.useFor", "Field is required"))
                                        .toResponse(Status.BAD_REQUEST));
                                return Uni.createFrom().failure(new RuntimeException());
                            } break;
                        case NOTIFY:
                            if(null == ap.policy.notifyLink || null == ap.policy.notifyMessage ||
                               ap.policy.notifyMessage.isBlank() || ap.policy.notifyLink.isBlank()) {
                                var errorDetails = new ArrayList<Tuple2<String, String>>();
                                if(null == ap.policy.notifyLink || ap.policy.notifyLink.isBlank())
                                    errorDetails.add(Tuple2.of("policy.notifyLink", "Field is required"));
                                if(null == ap.policy.notifyMessage || ap.policy.notifyMessage.isBlank())
                                    errorDetails.add(Tuple2.of("policy.notifyMessage", "Field is required"));

                                response.complete(new ActionError("invalidParameters", errorDetails)
                                        .toResponse(Status.BAD_REQUEST));
                                return Uni.createFrom().failure(new RuntimeException());
                            } break;
                    }

                    return Uni.createFrom().item(sid);
                })
                .onItem().transformToUni(sid -> {
                    // Create REST clients for DataHub and the IDSA connector
                    if(!getRestClients(ap)) {
                        // Could not get REST clients
                        response.complete(new ActionError("invalidConfiguration", Arrays.asList(
                                Tuple2.of("spaceName", ap.spaceName),
                                Tuple2.of("onezoneBaseUrl", actionConfig.zoneBaseUrl()),
                                Tuple2.of("connectorBaseUrl", connectorConfig.connectorBaseUrl()) ))
                                .toResponse(Status.EXPECTATION_FAILED));
                        return Uni.createFrom().failure(new RuntimeException());
                    }

                    return Uni.createFrom().item(sid);
                })
                .onItem().transformToUni(sid -> {
                    // Get space details and build REST client for a provider supporting the space
                    return getSpaceInfo(ap);
                })
                .onItem().transformToUni(params -> {
                    // Got space details
                    // Check previous operation
                    if (params.failed()) {
                        response.complete(params.response);
                        return ActionParameters.failedUni();
                    }

                    // Ensure we have a catalog for this space
                    return validateCatalog(params, true);
                })
                .onItem().transformToUni(params -> {
                    // Catalog for the space is ready
                    // Check previous operation
                    if (params.failed()) {
                        response.complete(params.response);
                        return ActionParameters.failedUni();
                    }

                    // Publish the root folder
                    params.fileTokenValidityDays = actionConfig.fileTokenValidityDays();
                    return ActionResource.handleFolder(params, publishFile, "Publish");
                })
                .onItem().transformToUni(params -> {
                    // Root folder published
                    // Check previous operation
                    if (params.failed()) {
                        response.complete(params.response);
                        return ActionParameters.failedUni();
                    }

                    // Success
                    response.complete(Response.ok(params.response.getEntity()).build());
                    return Uni.createFrom().nullItem();
                })
                .onFailure().invoke(e -> {
                    LOG.errorf("Failed to publish space %s", spaceId);
                    if (!response.isDone())
                        response.complete(new ActionError(e, Tuple2.of("spaceId", spaceId))
                                .toResponse());
                })
                .onCancellation().invoke(() -> {
                    LOG.infof("Cancelled publishing space %s", spaceId);
                })
                .subscribe().with(unused -> {});

            // Wait until space is published (possibly with error)
            Response r = response.get();
            return Uni.createFrom().item(r);
        } catch (InterruptedException e) {
            // Cancelled
            return Uni.createFrom().item(new ActionError("publishInterrupted", Tuple2.of("spaceId", spaceId)).toResponse());
        } catch (ExecutionException e) {
            // Execution error
            return Uni.createFrom().item(new ActionError("publishExecutionError", Tuple2.of("spaceId", spaceId)).toResponse());
        }
    }
}
