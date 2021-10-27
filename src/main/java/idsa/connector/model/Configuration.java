package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * The configuration of an IDSA connector
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    public String connectorId;
    public String title;
    public String description;
    public String version;
    public List<String> inboundModelVersion;
    public String outboundModelVersion;
    public String status;
    public String logLevel;
    public String deployMode;
    public String defaultEndpoint;
}
