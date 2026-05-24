package cn.edu.scnu.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IdCardMaskSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null || value.length() < 8) {
            gen.writeString(value);
            return;
        }
        gen.writeString(value.substring(0, 6) + "********" + value.substring(value.length() - 4));
    }
}
