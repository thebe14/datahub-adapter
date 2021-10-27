package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Map;


/**
 * The details of a contract
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Contract extends ContractDescription {

    private String contractId;

    public Date creationDate;
    public Date modificationDate;

    // The additional properties
    public Map<String, String> additional;

    // The links of the catalog
    public Map<String, Link> _links;


    /**
     * Extracts the id of the contract from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == contractId)
            contractId = Link.getIdFromLinks(_links);

        return contractId;
    }
}
