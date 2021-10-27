package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * The basic details of an artifact
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ArtifactBase {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String title;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String description;
}
