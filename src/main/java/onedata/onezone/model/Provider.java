package onedata.onezone.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The details of a provider
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Provider {

    public String name;
    public String providerId;
    public String clusterId;
    public String domain;
    public float longitude;
    public float latitude;
    public long creationTime;
}
