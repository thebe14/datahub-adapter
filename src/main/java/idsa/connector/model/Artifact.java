package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Map;


/**
 * The details of an artifact
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifact extends ArtifactBase {

    private String artifactId;

    public Date creationDate;
    public Date modificationDate;
    public String remoteId;
    public long numAccessed;
    public long byteSize;
    public long checkSum;

    // The additional properties
    public Map<String, String> additional;

    // The links of the artifact
    public Map<String, Link> _links;


    /**
     * Extracts the id of the artifact from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == artifactId)
            artifactId = Link.getIdFromLinks(_links);

        return artifactId;
    }
}
