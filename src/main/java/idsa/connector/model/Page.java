package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * Pagination support for some other entity
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Page {

    // Page counters
    public long number;
    public long totalPages;

    // Element counters
    public long size;
    public long totalElements;
}
