package com.github.thkwag.thymelab.processor.controller;

import com.github.thkwag.thymelab.processor.service.ThymeleafDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PageControllerTest {

    @Mock
    private ThymeleafDataService thymeleafDataService;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    private PageController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new PageController(thymeleafDataService);
    }

    @Test
    @DisplayName("Test root path returns index")
    void testRootPath() {
        // Given
        when(request.getRequestURI()).thenReturn("/");

        // When
        String viewName = controller.handleRequest(model, request);

        // Then
        assertEquals("index", viewName);
        verify(thymeleafDataService).loadJsonData("index", model);
    }

    @Test
    @DisplayName("Test custom path returns path name")
    void testCustomPath() {
        // Given
        String path = "pages/custom";
        when(request.getRequestURI()).thenReturn("/" + path);

        // When
        String viewName = controller.handleRequest(model, request);

        // Then
        assertEquals(path, viewName);
        verify(thymeleafDataService).loadJsonData(path, model);
    }

    @Test
    @DisplayName("Test nested path returns full path")
    void testNestedPath() {
        // Given
        String path = "pages/nested/path";
        when(request.getRequestURI()).thenReturn("/" + path);

        // When
        String viewName = controller.handleRequest(model, request);

        // Then
        assertEquals(path, viewName);
        verify(thymeleafDataService).loadJsonData(path, model);
    }

    @Test
    @DisplayName("Test static resource paths are not handled")
    void testStaticResourcePaths() {
        // Given
        String[] staticPaths = {
            "/css/style.css",
            "/js/script.js",
            "/images/image.png",
            "/hot-reload",
            "/thymeleaf/js/script.js"
        };

        for (String path : staticPaths) {
            // When & Then
            assertThrows(Exception.class, () -> {
                when(request.getRequestURI()).thenReturn(path);
                controller.handleRequest(model, request);
            });
        }
    }
} 