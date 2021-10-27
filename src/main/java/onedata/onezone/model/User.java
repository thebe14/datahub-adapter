package onedata.onezone.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * The details of a user
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    public String name;
    public String userId;
    public String username;
    public String login;
    public String fullName;
    public List<String> emails;
    public List<String> emailList;
    public long creationTime;
    public boolean blocked;
    public boolean basicAuthEnabled;
    public String alias;
}
