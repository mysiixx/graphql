package ee.joeltek.match_me.websocket;

import ee.joeltek.match_me.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;


@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {
    private final ChatRepository chatRepository;
    private final ChatWebSocketService webSocketService;
    private final PresenceService presenceService;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        if (event.getUser() == null) {
            return;
        }
        Long userId = extractUserId(event.getUser());
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();

        if (sessionId == null) {
            return;
        }

        boolean becameOnline = presenceService.markOnline(userId, sessionId);
        if (!becameOnline) {
            return;
        }
        chatRepository.findAllChatsForUser(userId)
                .forEach(chat -> webSocketService.sendUserOnline(chat.getId(), userId));
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() == null) {
            return;
        }
        Long userId = extractUserId(event.getUser());
        boolean becameOffline = presenceService.markOffline(userId, event.getSessionId());

        if (!becameOffline) {
            return;
        }
        chatRepository.findAllChatsForUser(userId)
                .forEach(chat -> webSocketService.sendUserOffline(chat.getId(), userId));
    }

    private Long extractUserId(Principal principal) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) principal;
        return Long.valueOf(token.getToken().getSubject());
    }
}