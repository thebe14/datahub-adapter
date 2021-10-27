package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import egi.eu.ActionResource;
import org.jboss.logging.Logger;


/**
 * The basic details of a contract rule
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleDescription {

    private static final Logger LOG = Logger.getLogger(RuleDescription.class);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String title;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String description;

    // String representation of a usage policy
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String value;


    public RuleDescription() {}

    public RuleDescription(String title) {
        this.title = title;
    }

    public RuleDescription(String title, Policy policy) throws JsonProcessingException {
        this.title = title;
        this.value = new ObjectMapper().writeValueAsString(policy);

        // Escape quote (") and slash (/) characters by putting a backslash (\) in front of them
        this.value = this.value.replaceAll("\"", "\\\\\"");
        this.value = this.value.replaceAll("\\/", "\\\\\\/");

        LOG.infof("Rule policy is: %s", this.value);
    }
}
