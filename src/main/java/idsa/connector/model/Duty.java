package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Duty (mandatory operation) for a policy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Duty {

    @JsonIgnore
    private String dutyId;

    @JsonProperty("@type")
    public String type;

    @JsonProperty("@id")
    public String id;

    @JsonProperty("ids:title")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DutyDetail title;

    @JsonProperty("ids:description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public DutyDetail description;

    @JsonProperty("ids:action")
    public DutyDetail action;

    @JsonProperty("ids:constraint")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Constraint> constraints;


    /**
     * Construct a duty
     */
    public Duty() {
        this.type = "ids:Duty";
        this.id = String.format("https://w3id.org/idsa/autogen/duty/%s", extractId());
    }

    /**
     * Build usage notification duty
     * @param title The title of the notification
     * @param description The description of the notification
     * @return New duty list
     */
    public static List<Duty> Notification(String title, String description) {
        Duty duty = new Duty();
        duty.action = new DutyDetail(ActionType.NOTIFY);
        if(null != title)
            duty.title = new DutyDetail(title);
        if(null != description)
            duty.description = new DutyDetail(description);

        return Arrays.asList(duty);
    }

    /**
     * Build log usage duty
     * @return New duty list
     */
    public static List<Duty> Log() {
        Duty duty = new Duty();
        duty.action = new DutyDetail(ActionType.LOG);

        return Arrays.asList(duty);
    }

    /**
     * Build delete after use duty
     * @return New duty list
     */
    public static List<Duty> Delete(Date deleteAt) {
        Duty duty = new Duty();
        duty.action = new DutyDetail(ActionType.DELETE);
        duty.constraints = Constraint.DateTime(deleteAt);

        return Arrays.asList(duty);
    }

    /**
     * Obtains the id of this duty
     * @return Unique identifier
     */
    public String extractId() {
        if(null == dutyId)
            dutyId = UUID.randomUUID().toString();

        return dutyId;
    }

    /**
     * Duty detail
     */
    public static class DutyDetail {

        @JsonProperty("@id")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String id;

        @JsonProperty("@value")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String value;

        @JsonProperty("@type")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String type;


        public DutyDetail() {}

        public DutyDetail(String text) {
            this.value = text;
            this.type = "http://www.w3.org/2001/XMLSchema#string";
        }

        public DutyDetail(ActionType action) {
            switch(action) {
                case NOTIFY:
                    id = "NOTIFY"; break;
                case LOG:
                    id = "LOG"; break;
                case DELETE:
                    id = "DELETE"; break;
                default:
                    throw new RuntimeException("unknownDutyAction");
            }

            id = String.format("https://w3id.org/idsa/code/%s", id);
        }
    }

    /**
     * Action types
     */
    public static enum ActionType {
        NOTIFY,
        LOG,
        DELETE;

        private ActionType() {}
    }
}
