package com.github.thkwag.thymelab.processor.hotreload;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HotReloadWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public HotReloadWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyClients() {
        log.debug("Notifying clients to reload");
        messagingTemplate.convertAndSend("/topic/reload", "reload");
    }
} 