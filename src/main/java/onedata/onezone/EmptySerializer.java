package onedata.onezone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

import onedata.onezone.model.Empty;


/**
 * Serializer for empty objects (that do not have any properties)
 */
public class EmptySerializer extends StdSerializer<Empty> {

    public EmptySerializer() {
        this(null);
    }

    public EmptySerializer(Class<Empty> t) {
        super(t);
    }

    @Override
    public void serialize(Empty value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // Write "{}" instead of "null"
        gen.writeStartObject();
        gen.writeEndObject();
    }
}
