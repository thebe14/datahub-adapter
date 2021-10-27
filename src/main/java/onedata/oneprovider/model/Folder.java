package onedata.oneprovider.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * The content of a folder
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Folder {

    public boolean isLast;

    // The files and sub-folders in this folder
    public List<Item> children;

    /**
     * A folder child item
     */
    public static class Item {

        @JsonProperty("id")
        public String fileId;
        public String name;

        @JsonIgnore
        public String accessToken;


        public Item() {}

        public Item(String name, String fileId) {
            this.name = name;
            this.fileId = fileId;
        }
    }
}
