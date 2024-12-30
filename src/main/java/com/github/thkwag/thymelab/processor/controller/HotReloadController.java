package com.github.thkwag.thymelab.processor.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HotReloadController {
    
    private final SimpMessagingTemplate messagingTemplate;

    public HotReloadController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/reload")
    @SendTo("/topic/reload")
    public String handleReload(String message) {
        return message;
    }

    public void notifyClients() {
        log.debug("Notifying clients to reload");
        messagingTemplate.convertAndSend("/topic/reload", "reload");
    }
} 