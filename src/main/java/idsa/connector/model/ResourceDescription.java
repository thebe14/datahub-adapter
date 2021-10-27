package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


/**
 * The basic details of a resource (used to create one)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceDescription extends ResourceBase {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String paymentMethod;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String endpointDocumentation;

    public String filename;
    public String path;


    public ResourceDescription() {}

    public ResourceDescription(String filename, String path) {
        this.filename = filename;
        this.path = path;
        this.title = String.format("%s/%s", path.length() > 1 ? path : "", filename);
    }
}
