package onedata.onezone.model;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import onedata.onezone.EmptySerializer;


/**
 * An object with no proterties, but that can be serialized
 */
@JsonSerialize(using = EmptySerializer.class)
public class Empty {
}
