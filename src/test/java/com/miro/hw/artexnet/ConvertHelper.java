package com.miro.hw.artexnet;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.DateFormat;

public class ConvertHelper {

    public static String objectToJsonString(Object object, DateFormat dateFormat) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (dateFormat != null) {
            mapper.setDateFormat(dateFormat);
        }
        return mapper.writeValueAsString(object);
    }

    public static byte[] objectToJsonBytes(Object object) throws IOException {
        return objectToJsonBytes(object, null);
    }

    public static byte[] objectToJsonBytes(Object object, DateFormat dateFormat) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        if (dateFormat != null) {
            mapper.setDateFormat(dateFormat);
        }
        return mapper.writeValueAsBytes(object);
    }
}
