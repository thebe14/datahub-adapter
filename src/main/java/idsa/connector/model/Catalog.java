package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.Date;


/**
 * The details of a catalog
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Catalog extends CatalogDescription {

    private String catalogId;

    public Date creationDate;
    public Date modificationDate;

    // The additional properties
    public Map<String, String> additional;

    // The links of the catalog
    public Map<String, Link> _links;


    /**
     * Extracts the id of the catalog from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == catalogId)
            catalogId = Link.getIdFromLinks(_links);

        return catalogId;
    }
}
