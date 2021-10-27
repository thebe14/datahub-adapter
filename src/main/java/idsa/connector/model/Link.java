package idsa.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;


/**
 * Link to another entity
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

    public String href;
    public boolean templated;


    /**
     * Extracts the id of an entity from its self URL.
     * @return entity id, empty string on error
     */
    static public String getIdFromLinks(Map<String, Link> links) {
        // Get the self link
        Link self = links.get("self");
        if(null == self)
            // No self link
            return "";

        URL urlSelf;
        try {
            urlSelf = new URL(self.href);
        } catch (MalformedURLException e) {
            return "";
        }

        String path = urlSelf.getPath();
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 2 || lastSlash == path.length() - 1)
            // Self links ends in slash (/)
            return "";

        return path.substring(lastSlash + 1);
    }
}
