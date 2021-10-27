package egi.eu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.concurrent.atomic.AtomicLong;


/**
 * The successful result of an operation in the DataHub connector's REST API.
 *
 * This is the OpenApi entity that will be returned in case a DatHub connect API succeeds.
 *
 */
@Schema(name = "PublishResults")
public class ActionSuccess {

    @Schema(title="The published space")
    @JsonInclude(Include.NON_EMPTY)
    public String spaceId;

    @Schema(title="Number of files published for the first time")
    public long newFiles;

    @Schema(title="Number of files for which only the access token was updated")
    public long updatedFiles;

    @JsonIgnore
    public AtomicLong _newFiles;

    @JsonIgnore
    public AtomicLong _updatedFiles;

    @JsonIgnore
    public AtomicLong _subFolders;

    /**
     * Constructor
     *
     */
    public ActionSuccess() {
        this.newFiles = 0;
        this.updatedFiles = 0;
        this._newFiles = new AtomicLong(0);
        this._updatedFiles = new AtomicLong(0);
        this._subFolders = new AtomicLong(0);
    }

    /**
     * Copy summary to the fields we publish to the API
     */
    public void publishSummary() {
        this.newFiles = this._newFiles.get();
        this.updatedFiles = this._updatedFiles.get();
    }

    /**
     * Convert to Response that can be returned by a REST endpoint
     *
     */
    public Response toResponse() {
        return Response.ok(this).build();
    }

    /**
     * Convert to Response that can be returned by a REST endpoint
     *
     */
    public Response toResponse(Status status) {
        return Response.ok(this).status(status).build();
    }
}
