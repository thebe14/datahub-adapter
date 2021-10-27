package onedata.oneprovider.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The details of a space
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpaceRoot {

    public String name;
    public String spaceId;
    public String fileId;

    // The providers supporting this space
    public List<Provider> providers;

    public static class Provider {

        public String providerName;
        public String providerId;
    }
}
