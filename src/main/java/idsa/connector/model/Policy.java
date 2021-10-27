package idsa.connector.model;

import com.fasterxml.jackson.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Usage policy (one or more of these form a contract rule)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Policy {

    @JsonIgnore
    private String policyId;

    @JsonProperty("@context")
    PolicyContext context;

    @JsonProperty("@type")
    public String type;

    @JsonProperty("@id")
    public String id;

    @JsonProperty("ids:title")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<PolicyDetail> title;

    @JsonProperty("ids:description")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<PolicyDetail> description;

    @JsonProperty("ids:action")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<PolicyDetail> action;

    @JsonProperty("ids:constraint")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Constraint> constraints;

    @JsonProperty("ids:preDuty")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Duty> preDuties;

    @JsonProperty("ids:postDuty")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Duty> postDuties;


    /**
     * Construct a policy
     */
    public Policy() { this(false); }

    /**
     * Construct a policy
     */
    public Policy(boolean needsXsd) {
        this.type = "ids:Permission";
        this.id = String.format("https://w3id.org/idsa/autogen/permission/%s", extractId());
        this.context = new PolicyContext(needsXsd);
        this.title = new ArrayList<>();
        this.description = new ArrayList<>();
        this.action = new ArrayList<>();
        this.action.add(new PolicyDetail(ActionType.USE));
    }

    /**
     * Build policy to prohibit use
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy ProhibitUse(String title) {
        Policy pol = new Policy();
        pol.description.add(new PolicyDetail("prohibit-access"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        return pol;
    }

    /**
     * Build policy to allow unrestricted use
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy FreeUse(String title) {
        Policy pol = new Policy();
        pol.description.add(new PolicyDetail("provide-access"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        return pol;
    }

    /**
     * Build policy to allow use a number of times
     * @param useCount How many times can the resource be used
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy CountedUse(Long useCount, String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("n-times-usage"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.constraints = Constraint.Counted(useCount);

        return pol;
    }

    /**
     * Build policy to allow use in a date/time interval
     * @param useFrom The start of the permitted usage period
     * @param useUntil The end of the permitted usage period
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy IntervalUse(Date useFrom, Date useUntil, String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("usage-during-interval"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.constraints = Constraint.Interval(useFrom, useUntil);

        return pol;
    }

    /**
     * Build policy to allow use in a date/time interval and delete after use
     * @param useFrom The start of the permitted usage period
     * @param useUntil The end of the permitted usage period
     * @param deleteAt The date and time when the resource should be deleted by the consumer (and not used anymore)
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy IntervalUseDeleteAt(Date useFrom, Date useUntil, Date deleteAt, String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("usage-until-deletion"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.constraints = Constraint.Interval(useFrom, useUntil);
        pol.postDuties = Duty.Delete(deleteAt);

        return pol;
    }

    /**
     * Build policy to allow use for a limited duration
     * @param duration How long can the resource be used
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy DurationUse(Duration duration, String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("duration-usage"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.constraints = Constraint.Duration(duration);

        return pol;
    }

    /**
     * Build policy to allow use with notification
     * @param notifyMessage The message to show on use
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy NotifyUse(String notifyMessage, String notifyLink, String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("usage-notification"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.constraints = Constraint.Notification(notifyLink);
        pol.postDuties = Duty.Notification(title, notifyMessage);

        return pol;
    }

    /**
     * Build policy to allow use with logging
     * @param title The title of the policy
     * @return New policy
     */
    public static Policy LogUse(String title) {
        Policy pol = new Policy(true);
        pol.description.add(new PolicyDetail("usage-logging"));
        if(null != title)
            pol.title.add(new PolicyDetail(title));

        pol.postDuties = Duty.Log();

        return pol;
    }

    /**
     * Obtains the id of this policy
     * @return Unique identifier
     */
    public String extractId() {
        if(null == policyId)
            policyId = UUID.randomUUID().toString();

        return policyId;
    }

    /**
     * Policy context
     */
    public static class PolicyContext {

        public String ids;
        public String idsc;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String xsd;


        public PolicyContext(boolean needsXsd) {
            this.ids = "https://w3id.org/idsa/core/";
            this.idsc = "https://w3id.org/idsa/code/";

            if(needsXsd)
                this.xsd = "http://www.w3.org/2001/XMLSchema#";
        }
    }

    /**
     * Policy detail
     */
    public static class PolicyDetail {

        @JsonProperty("@id")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String id;

        @JsonProperty("@value")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String value;

        @JsonProperty("@type")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String type;


        public PolicyDetail() {}

        public PolicyDetail(String text) {
            this.value = text;
            this.type = "http://www.w3.org/2001/XMLSchema#string";
        }

        public PolicyDetail(ActionType action) {
            switch(action) {
                case USE:
                    id = "USE"; break;
                default:
                    throw new RuntimeException("unknownPolicyAction");
            }

            id = String.format("https://w3id.org/idsa/code/%s", id);
        }
    }

    /**
     * Action types
     */
    public static enum ActionType {
        USE;

        private ActionType() {}
    }
}
