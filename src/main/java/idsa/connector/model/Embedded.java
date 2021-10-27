package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;


/**
 * A list of child (embedded) entities
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Embedded<T> {

    // The child entities
    public Map<String, List<T>> _embedded;

    // The links of the catalog
    public Map<String, Link> _links;

    // Pagination support
    public Page page;
}
