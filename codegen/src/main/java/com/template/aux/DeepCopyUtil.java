package com.template.aux;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DeepCopyUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T deepCopy(T object, TypeReference<T> typeReference) {
        try {
            String json = objectMapper.writeValueAsString(object);
            T result = objectMapper.readValue(json, typeReference);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Deep copy failed", e);
        }
    }
    
}
