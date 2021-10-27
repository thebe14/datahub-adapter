package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * The details of an artifact (used to create one)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactDescription extends ArtifactBase {

    public String accessUrl;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String value;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public KeyValue basicAuth;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public KeyValue apiKey;

    public boolean automatedDownload;

    public String filename;


    /**
     * Constructor
     */
    public ArtifactDescription() {
        this.automatedDownload = true;
    }

    /**
     * Constructor
     */
    public ArtifactDescription(String title, String accessUrl, String accessToken) {
        this.title = title;
        this.filename = title;
        this.accessUrl = accessUrl;
        this.apiKey = new KeyValue("X-Auth-Token", accessToken);
        this.automatedDownload = true;
    }

    /**
     * A key-value pair
     */
    public static class KeyValue {

        public KeyValue() {}

        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key;
        public String value;
    }
}
