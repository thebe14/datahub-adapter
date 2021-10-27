package egi.eu.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Duration;
import java.util.Date;


/**
 * Optional policy for publish action
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishPolicy {

    @Schema(title="Access policy type", required=true, defaultValue="FREE")
    public PolicyType type;

    @Schema(title="Allowed number of uses")
    public long useCount;

    @Schema(title="Start of allowed use", format="date-time", example = "2020-07-11T00:00:00Z")
    public Date useFrom;

    @Schema(title="End of allowed use", format="date-time", example = "2020-12-31T23:00:00Z")
    public Date useUntil;

    @Schema(title="Date and time consumer must delete resource and cease use", format="date-time", example = "2020-12-15T00:00:00Z")
    public Date deleteAt;

    @Schema(title="Duration of allowed use (from requesting access)")
    public Duration useFor;

    @Schema(title="Notify with message on use")
    public String notifyMessage;

    @Schema(title="Notify link", format = "uri", example = "https://host/path/notification.html")
    public String notifyLink;


    public PublishPolicy() { useCount = -1; }

    public PublishPolicy(PolicyType type) { this.type = type; useCount = -1; }

    /**
     * Policy types
     */
    public static enum PolicyType {
        PROHIBIT,
        FREE,
        COUNTED,
        INTERVAL,
        INTERVAL_DELETE,
        DURATION,
        NOTIFY,
        LOG;

        private PolicyType() {}
    }
}
