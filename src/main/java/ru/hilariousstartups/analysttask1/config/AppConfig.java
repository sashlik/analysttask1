package ru.hilariousstartups.analysttask1.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    public Map<Integer, Map<String, List<Integer>>> task1refMap() {
        Map<Integer, Map<String, List<Integer>>> reference = new HashMap<>();
        Map<String, List<Integer>> releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(11, 12));
        releaseMap.put("2", List.of());
        reference.put(2, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(14, 15));
        releaseMap.put("2", List.of());
        reference.put(3, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(7, 13));
        releaseMap.put("2", List.of());
        reference.put(6, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of());
        releaseMap.put("2", List.of(18, 23));
        reference.put(8, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of());
        releaseMap.put("2", List.of(25, 26));
        reference.put(9, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(18, 20, 21, 22));
        releaseMap.put("2", List.of());
        reference.put(12, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(7, 8, 9));
        releaseMap.put("2", List.of());
        reference.put(13, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(4, 10));
        releaseMap.put("2", List.of());
        reference.put(16, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(18));
        releaseMap.put("2", List.of(19));
        reference.put(18, releaseMap);

        releaseMap = new HashMap<>();
        releaseMap.put("1", List.of(1, 2, 3, 4, 5));
        releaseMap.put("2", List.of());
        reference.put(20, releaseMap);

        return reference;
    }

}
