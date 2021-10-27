package onedata.onezone.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The details of a space
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Space {

    public String name;
    public String spaceId;

    // The providers supporting this space
    // - key is the provider Id
    // - value is the size of storage provisioned for this space by provider
    public Map<String, Long> providers;
}
