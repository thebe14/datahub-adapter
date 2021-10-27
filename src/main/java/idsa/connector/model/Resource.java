package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Map;


/**
 * The details of a resource
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resource extends ResourceBase {

    private String resourceId;

    public Date creationDate;
    public Date modificationDate;
    public String remoteId;
    public long numAccessed;
    public long byteSize;
    public long checkSum;

    // The additional properties
    public Map<String, String> additional;

    // The links of the resource
    public Map<String, Link> _links;


    /**
     * Extracts the id of the resource from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == resourceId)
            resourceId = Link.getIdFromLinks(_links);

        return resourceId;
    }
}
