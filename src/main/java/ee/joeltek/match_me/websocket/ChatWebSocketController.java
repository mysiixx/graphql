package ee.joeltek.match_me.websocket;

import java.security.Principal;

import ee.joeltek.match_me.chat.ChatAccessService;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final ChatWebSocketService webSocketService;
    private final ChatAccessService chatAccessService;


    @MessageMapping("/chats/{chatId}/typing.started")
    public void typingStarted(@DestinationVariable Long chatId, Principal principal) {
        Long userId = extractUserId(principal);
        requireChatParticipant(chatId, userId);
        webSocketService.sendTypingStarted(chatId, userId);
    }

    @MessageMapping("/chats/{chatId}/typing.stopped")
    public void typingStopped(@DestinationVariable Long chatId, Principal principal) {
        Long userId = extractUserId(principal);
        requireChatParticipant(chatId, userId);
        webSocketService.sendTypingStopped(chatId, userId);
    }

    private Long extractUserId(Principal principal) {
        if (!(principal instanceof JwtAuthenticationToken token)) {
            throw new MessageDeliveryException("Missing authenticated WebSocket user");
        }
        return Long.valueOf(token.getToken().getSubject());
    }

    private void requireChatParticipant(Long chatId, Long userId) {
        if (!chatAccessService.isParticipant(chatId, userId)) {
            throw new MessageDeliveryException("Not allowed to access this chat");
        }
    }
}
