package onedata.onezone.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The details of an access token
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Token {

    public String token;
}
