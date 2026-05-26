package com.github.arsenmonets.newshub.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.github.arsenmonets.newshub.dto.NewsPreviewDTO;

@Controller
public class NewsWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public NewsWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNewsUpdate(NewsPreviewDTO newsDTO) {
        messagingTemplate.convertAndSend("/topic/news/updated", newsDTO);
    }

    public void sendNewsCreate(NewsPreviewDTO newsDTO) {
        messagingTemplate.convertAndSend("/topic/news/created", newsDTO);
    }

    public void sendNewsDelete(Long id) {
        messagingTemplate.convertAndSend("/topic/news/deleted", id);
    }
}
