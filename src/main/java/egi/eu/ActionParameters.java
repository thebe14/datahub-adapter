package egi.eu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import egi.eu.model.PublishPolicy;
import idsa.connector.Connector;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.Multi;

import onedata.onezone.OneZone;
import onedata.oneprovider.OneProvider;
import onedata.oneprovider.model.File;
import onedata.oneprovider.model.Folder;


/**
 * The parameters of an action to perform (e.g. publish).
 * Allows implementing actions as instances of Function<JobInfo, Response>.
 *
 */
public class ActionParameters {

    public Connector connector;
    public String connectorId;

    public String catalogId;
    public String resourceId;
    public String representationId;
    public String artifactId;
    public String ruleId;
    public String contractId;

    public OneZone onezone;

    public OneProvider provider;
    public String providerId;
    public String providerBaseUrl;

    public String spaceName;
    public String spaceId;

    public Folder.Item item;
    public String path;
    public String type;
    public Map<String, String> metadata;

    public int fileTokenValidityDays;
    public String publisher;
    public String sovereign;
    public String language;
    public String license;
    public List<String> keywords;
    public PublishPolicy policy;

    public Response response;


    /**
     * Constructor
     */
    public ActionParameters() {
        this.type = File.TYPE_FOLDER;
        this.response = Response.ok().build();
        this.fileTokenValidityDays = 7;
    }

    /**
     * Constructor
     */
    public ActionParameters(String spaceId) {
        this.spaceId = spaceId;
        this.type = File.TYPE_FOLDER;
        this.response = Response.ok().build();
        this.fileTokenValidityDays = 7;
    }

    /**
     * Copy constructor
     */
    public ActionParameters(ActionParameters ap) {
        this.connector = ap.connector;
        this.connectorId = ap.connectorId;
        this.catalogId = ap.catalogId;
        this.resourceId = ap.resourceId;
        this.representationId = ap.representationId;
        this.artifactId = ap.artifactId;
        this.ruleId = ap.ruleId;
        this.contractId = ap.contractId;
        this.onezone = ap.onezone;
        this.provider = ap.provider;
        this.providerId = ap.providerId;
        this.providerBaseUrl = ap.providerBaseUrl;
        this.spaceName = ap.spaceName;
        this.spaceId = ap.spaceId;
        this.item = new Folder.Item(ap.item.name, ap.item.fileId);
        this.path = ap.path;
        this.type = ap.type;
        this.metadata = ap.metadata;
        this.response = ap.response;
        this.fileTokenValidityDays = ap.fileTokenValidityDays;
        this.publisher = ap.publisher;
        this.sovereign = ap.sovereign;
        this.language = ap.language;
        this.license = ap.license;
        this.policy = ap.policy;
        this.keywords = (null != ap.keywords) ? new ArrayList<String>() : null;
        if(null != this.keywords)
            this.keywords.addAll(ap.keywords);
    }

    /**
     * Check if the embedded API Response is a success
     */
    public boolean succeeded() {
        return Family.familyOf(this.response.getStatus()) == Family.SUCCESSFUL;
    }

    /**
     * Check if the embedded API Response is a failure
     */
    public boolean failed() {
        return Family.familyOf(this.response.getStatus()) != Family.SUCCESSFUL;
    }

    /**
     * Get failure Uni
     */
    public static <T> Uni<T> failedUni() {
        return Uni.createFrom().failure(new RuntimeException());
    }

    /**
     * Get failure Multi
     */
    public static <T> Multi<T> failedMulti() {
        return Multi.createFrom().failure(new RuntimeException());
    }
}
