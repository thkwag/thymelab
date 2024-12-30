package com.github.thkwag.thymelab.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ui.Model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
class ThymeleafDataServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private Model model;

    @Mock
    private Resource resource;

    private ThymeleafDataService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ThymeleafDataService(objectMapper, resourceLoader);
    }

    @Test
    @DisplayName("Test loading JSON data from classpath")
    void testLoadJsonDataFromClasspath() throws IOException {
        // Given
        String templatePath = "test";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("key", "value");

        when(resourceLoader.getResource("classpath:/default/thymelab/data/test.json"))
            .thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(Files.newInputStream(createTestJsonFile(jsonData)));
        when(objectMapper.readValue(anyString(), eq(Map.class)))
            .thenReturn(jsonData);

        // When
        service.loadJsonData(templatePath, model);

        // Then
        verify(model).asMap();
        verify(objectMapper).readValue(anyString(), eq(Map.class));
        assertNotNull(service);
    }

    @Test
    @DisplayName("Test loading JSON data from file system")
    void testLoadJsonDataFromFileSystem() throws IOException {
        // Given
        String templatePath = "test";
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("key", "value");

        File jsonFile = createTestJsonFile(jsonData).toFile();
        service = new ThymeleafDataService(objectMapper, resourceLoader);
        setPrivateField(service, "thymeleafDataDir", jsonFile.getParent());

        when(objectMapper.readValue(any(String.class), eq(Map.class)))
            .thenReturn(jsonData);

        // When
        service.loadJsonData(templatePath, model);

        // Then
        verify(model).asMap();
        verify(objectMapper).readValue(anyString(), eq(Map.class));
    }

    @Test
    @DisplayName("Test handling non-existent JSON file")
    void testHandleNonExistentJsonFile() throws IOException {
        // Given
        String templatePath = "nonexistent";
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(false);

        // When
        service.loadJsonData(templatePath, model);

        // Then
        verify(model, never()).addAttribute(anyString(), any());
        verify(objectMapper, never()).readValue(anyString(), any(Class.class));
    }

    @Test
    @DisplayName("Test handling invalid JSON data")
    void testHandleInvalidJsonData() throws IOException {
        // Given
        String templatePath = "invalid";
        when(resourceLoader.getResource(anyString())).thenReturn(resource);
        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(Files.newInputStream(createInvalidJsonFile()));
        when(objectMapper.readValue(any(String.class), eq(Map.class)))
            .thenThrow(new IOException("Invalid JSON"));

        // When
        service.loadJsonData(templatePath, model);

        // Then
        verify(model, never()).addAttribute(anyString(), any());
    }

    private Path createTestJsonFile(Map<String, Object> data) throws IOException {
        Path jsonFile = tempDir.resolve("test.json");
        Files.writeString(jsonFile, "{\"key\":\"value\"}");
        return jsonFile;
    }

    private Path createInvalidJsonFile() throws IOException {
        Path jsonFile = tempDir.resolve("invalid.json");
        Files.writeString(jsonFile, "invalid json content");
        return jsonFile;
    }

    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
} 