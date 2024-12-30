package com.github.thkwag.thymelab.processor.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HotReloadControllerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private HotReloadController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new HotReloadController(messagingTemplate);
    }

    @Test
    @DisplayName("Test handleReload returns message")
    void testHandleReload() {
        // Given
        String message = "test-message";

        // When
        String result = controller.handleReload(message);

        // Then
        assertEquals(message, result);
    }

    @Test
    @DisplayName("Test notifyClients sends reload message")
    void testNotifyClients() {
        // When
        controller.notifyClients();

        // Then
        verify(messagingTemplate).convertAndSend("/topic/reload", "reload");
    }

    @Test
    @DisplayName("Test multiple notifications")
    void testMultipleNotifications() {
        // When
        controller.notifyClients();
        controller.notifyClients();
        controller.notifyClients();

        // Then
        verify(messagingTemplate, times(3)).convertAndSend("/topic/reload", "reload");
    }
} 