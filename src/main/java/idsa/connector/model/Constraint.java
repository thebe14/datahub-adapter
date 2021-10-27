package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.time.Duration;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * Constraint for a policy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint {

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @JsonIgnore
    private String constraintId;

    @JsonProperty("@type")
    public String type;

    @JsonProperty("@id")
    public String id;

    @JsonProperty("ids:leftOperand")
    public ConstraintDetail leftOperand;

    @JsonProperty("ids:rightOperand")
    public ConstraintDetail rightOperand;

    @JsonProperty("ids:operator")
    public ConstraintDetail operator;


    /**
     * Construct a constraint
     */
    public Constraint() {
        this.type = "ids:Constraint";
        this.id = String.format("https://w3id.org/idsa/autogen/constraint/%s", extractId());
    }

    /**
     * Build counted usage constraint
     * @param useCount The count of permitted usages
     * @return New constraint list
     */
    public static List<Constraint> Counted(Long useCount) {
        Constraint con = new Constraint();
        con.leftOperand = new ConstraintDetail(LeftOperandType.COUNT);
        con.rightOperand = new ConstraintDetail(RightOperandType.DOUBLE, useCount.toString());
        con.operator = new ConstraintDetail(OperatorType.LTEQ);

        return Arrays.asList(con);
    }

    /**
     * Build point in time constraint
     * @param dateTime The date and time of the contrained action/duty
     * @return New constraint list
     */
    public static List<Constraint> DateTime(Date dateTime) {
        Constraint con = new Constraint();
        con.leftOperand = new ConstraintDetail(LeftOperandType.POLICY_EVALUATION_TIME);
        con.rightOperand = new ConstraintDetail(RightOperandType.TIMESTAMP, dateTimeFormat.format(dateTime));
        con.operator = new ConstraintDetail(OperatorType.TEMPORAL_EQUALS);

        return Arrays.asList(con);
    }
    
    /**
     * Build interval usage constraint
     * @param useFrom The start of the permitted usage period
     * @param useUntil The end of the permitted usage period
     * @return New constraint list
     */
    public static List<Constraint> Interval(Date useFrom, Date useUntil) {
        Constraint conFrom = new Constraint();
        conFrom.leftOperand = new ConstraintDetail(LeftOperandType.POLICY_EVALUATION_TIME);
        conFrom.rightOperand = new ConstraintDetail(RightOperandType.TIMESTAMP, dateTimeFormat.format(useFrom));
        conFrom.operator = new ConstraintDetail(OperatorType.AFTER);

        Constraint conUntil = new Constraint();
        conUntil.leftOperand = new ConstraintDetail(LeftOperandType.POLICY_EVALUATION_TIME);
        conUntil.rightOperand = new ConstraintDetail(RightOperandType.TIMESTAMP, dateTimeFormat.format(useUntil));
        conUntil.operator = new ConstraintDetail(OperatorType.BEFORE);

        return Arrays.asList(conFrom, conUntil);
    }

    /**
     * Build duration usage constraint
     * @param duration The duration of permitted usage
     * @return New constraint list
     */
    public static List<Constraint> Duration(Duration duration) {
        Constraint con = new Constraint();
        con.leftOperand = new ConstraintDetail(LeftOperandType.ELAPSED_TIME);
        con.rightOperand = new ConstraintDetail(RightOperandType.DURATION, duration.toString());
        con.operator = new ConstraintDetail(OperatorType.SHORTER_EQ);

        return Arrays.asList(con);
    }

    /**
     * Build usage notification constraint
     * @param notifyLink The URL to the notification
     * @return New constraint list
     */
    public static List<Constraint> Notification(String notifyLink) {
        Constraint con = new Constraint();
        con.leftOperand = new ConstraintDetail(LeftOperandType.ENDPOINT);
        con.rightOperand = new ConstraintDetail(RightOperandType.URI, notifyLink);
        con.operator = new ConstraintDetail(OperatorType.DEFINES_AS);

        return Arrays.asList(con);
    }

    /**
     * Obtains the id of this constraint
     * @return Unique identifier
     */
    public String extractId() {
        if(null == constraintId)
            constraintId = UUID.randomUUID().toString();

        return constraintId;
    }

    /**
     * Constraint detail
     */
    public static class ConstraintDetail {

        @JsonProperty("@id")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String id;

        @JsonProperty("@value")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String value;

        @JsonProperty("@type")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String type;


        public ConstraintDetail() {}

        public ConstraintDetail(LeftOperandType leftOperand) {
            switch(leftOperand) {
                case ENDPOINT:
                    id = "ENDPOINT"; break;
                case SYSTEM:
                    id = "SYSTEM"; break;
                case COUNT:
                    id = "COUNT"; break;
                case POLICY_EVALUATION_TIME:
                    id = "POLICY_EVALUATION_TIME"; break;
                case ELAPSED_TIME:
                    id = "ELAPSED_TIME"; break;
                default:
                    throw new RuntimeException("unknownConstraintLeftOperand");
            }

            id = String.format("https://w3id.org/idsa/code/%s", id);
        }

        public ConstraintDetail(RightOperandType rightOperand, String value) {
            this.value = value;

            switch(rightOperand) {
                case URI:
                    type = "xsd:anyURI"; break;
                case DOUBLE:
                    type = "xsd:double"; break;
                case TIMESTAMP:
                    type = "xsd:dateTimeStamp"; break;
                case DURATION:
                    type = "xsd:duration"; break;
                default:
                    throw new RuntimeException("unknownConstraintRightOperand");
            }
        }

        public ConstraintDetail(OperatorType operator) {
            switch(operator) {
                case DEFINES_AS:
                    id = "DEFINES_AS"; break;
                case SAME_AS:
                    id = "SAME_AS"; break;
                case LTEQ:
                    id = "LTEQ"; break;
                case AFTER:
                    id = "AFTER"; break;
                case BEFORE:
                    id = "BEFORE"; break;
                case SHORTER_EQ:
                    id = "SHORTER_EQ"; break;
                case TEMPORAL_EQUALS:
                    id = "TEMPORAL_EQUALS"; break;
                default:
                    throw new RuntimeException("unknownConstraintOperator");
            }

            id = String.format("https://w3id.org/idsa/code/%s", id);
        }
    }

    /**
     * Left operand types
     */
    public static enum LeftOperandType {
        ENDPOINT,
        SYSTEM,
        COUNT,
        POLICY_EVALUATION_TIME,
        ELAPSED_TIME;

        private LeftOperandType() {}
    }

    /**
     * Right operand types
     */
    public static enum RightOperandType {
        URI,
        DOUBLE,
        TIMESTAMP,
        DURATION;

        private RightOperandType() {}
    }

    /**
     * Operator types
     */
    public static enum OperatorType {
        DEFINES_AS,
        SAME_AS,
        LTEQ,
        AFTER,
        BEFORE,
        SHORTER_EQ,
        TEMPORAL_EQUALS;

        private OperatorType() {}
    }
}
