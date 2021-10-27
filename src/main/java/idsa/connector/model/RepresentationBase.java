package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


/**
 * The basic details of a representation
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class RepresentationBase {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String title;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String description;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String mediaType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String language;
}
