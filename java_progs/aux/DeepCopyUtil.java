package java_progs.aux;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class DeepCopyUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Generic deep copy function that works for objects, lists, arrays, and primitives
    public static <T> T deepCopy(T object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            return objectMapper.readValue(json, new TypeReference<T>() {});
        } catch (Exception e) {
            throw new RuntimeException("Deep copy failed", e);
        }
    }
}
