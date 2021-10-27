package onedata.onezone.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * The details of an access token
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenRequest {

    public Type type;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<Caveat> caveats;


    public TokenRequest() {
        this.type = new Type();
    }

    public TokenRequest(Caveat caveat) {
        this.type = new Type();
        this.caveats = Arrays.asList(caveat);
    }

    public TokenRequest(List<Caveat> caveats) {
        this.type = new Type();
        this.caveats = caveats;
    }


    /**
     * An access token type
     */
    public static class Type {

        public Empty accessToken;


        public Type() {
            this.accessToken = new Empty();
        }
    }

    /**
     * An access token caveat (restriction)
     */
    public static class Caveat {

        public String type;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Long validUntil;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public List<String> whitelist;


        /**
         * Construct read-only caveat
         */
        public Caveat() {
            this.type = "data.readonly";
            this.validUntil = null;
        }

        /**
         * Construct time limit caveat
         * @param validUntil is the date and time until the token will be valid
         */
        public Caveat(LocalDateTime validUntil) {
            this.type = "time";
            this.validUntil = validUntil.toEpochSecond(ZoneOffset.UTC);
        }

        /**
         * Construct whitelist caveat
         * @param fileId The id of the file for which the token will be valid
         */
        public Caveat(String fileId) {
            this.type = "data.objectid";
            this.whitelist = Arrays.asList(fileId);
            this.validUntil = null;
        }
    }
}
