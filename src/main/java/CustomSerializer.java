import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Set;

public class CustomSerializer<T> extends JsonSerializer<T> {

    private Set<String> fieldsToMask;

    public CustomSerializer(Set<String> fieldsToMask) {
        this.fieldsToMask = fieldsToMask;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        for (Field field : value.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                String fieldName = field.getName();
                Object fieldValue = field.get(value);
                if (fieldsToMask.contains(fieldName)) {
                    gen.writeStringField(fieldName, "****");
                } else {
                    gen.writeObjectField(fieldName, fieldValue);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        gen.writeEndObject();
    }
}
