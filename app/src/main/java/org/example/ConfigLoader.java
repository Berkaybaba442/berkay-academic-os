package org.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;

public class ConfigLoader {

    public AppConfig load() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        InputStream inputStream = getClass().getResourceAsStream("/config.json");

        if (inputStream == null) {
            throw new IllegalStateException("config.json bulunamadı");
        }

        return mapper.readValue(inputStream, AppConfig.class);
    }
}