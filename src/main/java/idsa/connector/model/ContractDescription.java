package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;


/**
 * The basic details of a contract (used to create one)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractDescription {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String title;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String description;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String consumer;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String provider;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date start;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Date end;


    public ContractDescription() {}

    public ContractDescription(String title) { this.title = title; }

    public ContractDescription(String filename, String path) {
        this.title = String.format("%s/%s", path.length() > 1 ? path : "", filename);
    }
}
