package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * The basic details of a catalog (used to create one)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogDescription {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String title;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String description;


    public CatalogDescription() {}

    public CatalogDescription(String title) {
        this.title = title;
    }
}
