package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * The basic details of a representation (used to create one)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepresentationDescription extends RepresentationBase {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String standard;


    public RepresentationDescription() {}

    public RepresentationDescription(String title, String language) {
        this.title = title;
        this.language = language;
    }
}
