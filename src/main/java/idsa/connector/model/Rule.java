package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Map;


/**
 * A contract rule
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule extends RuleDescription {

    private String ruleId;

    public Date creationDate;
    public Date modificationDate;

    // The additional properties
    public Map<String, String> additional;

    // The links of the rule
    public Map<String, Link> _links;


    /**
     * Extracts the id of the rule from its self URL.
     * @return the id
     */
    public String extractId() {
        if(null == ruleId)
            ruleId = Link.getIdFromLinks(_links);

        return ruleId;
    }
}

