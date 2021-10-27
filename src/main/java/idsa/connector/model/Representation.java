package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Map;


/**
 * The details of a representation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Representation extends RepresentationBase {

    private String representationId;

    public Date creationDate;
    public Date modificationDate;
    public String remoteId;

    // The additional properties
    public Map<String, String> additional;

    // The links of the representation
    public Map<String, Link> _links;


    /**
     * Extracts the id of the representation from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == representationId)
            representationId = Link.getIdFromLinks(_links);

        return representationId;
    }
}
