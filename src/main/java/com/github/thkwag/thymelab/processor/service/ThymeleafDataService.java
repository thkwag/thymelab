package com.github.thkwag.thymelab.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@Service
public class ThymeleafDataService {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${watch.directory.thymeleaf-data:classpath:/default/thymelab/data/}")
    private String thymeleafDataDir;

    public ThymeleafDataService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public void loadJsonData(String templatePath, Map<String, Object> model) {
        try {
            String directory = thymeleafDataDir;
            Path jsonPath;

            if (directory.startsWith("classpath:")) {
                Resource resource = resourceLoader.getResource(directory + templatePath + ".json");
                if (resource.exists()) {
                    String jsonContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.readValue(jsonContent, Map.class);
                    model.putAll(data);
                    log.debug("Loaded JSON data for template {}: {}", templatePath, data);
                }
            } else {
                jsonPath = Paths.get(directory, templatePath + ".json");
                if (Files.exists(jsonPath)) {
                    String jsonContent = Files.readString(jsonPath);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = objectMapper.readValue(jsonContent, Map.class);
                    model.putAll(data);
                    log.debug("Loaded JSON data for template {}: {}", templatePath, data);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load JSON data for template: {}", templatePath, e);
        }
    }

    public void loadJsonData(String templatePath, Model model) {
        loadJsonData(templatePath, model.asMap());
    }
} 