package ee.joeltek.match_me.config;


import ee.joeltek.match_me.chat.ChatAccessService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

import static java.lang.Long.parseLong;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtDecoder jwtDecoder;
    private final ChatAccessService chatAccessService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt);
                    accessor.setUser(auth);
                } catch (Exception e) {
                    throw new MessageDeliveryException("Invalid JWT");
                }
            } else {
                throw new MessageDeliveryException("Missing JWT");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();
            String destination = accessor.getDestination();

            if (destination != null && isChatDestination(destination)) {
                if (!(principal instanceof JwtAuthenticationToken auth)) {
                    throw new MessageDeliveryException("Missing authenticated WebSocket user");
                }

                Long userId = Long.valueOf(auth.getToken().getSubject());
                authorizeChatDestination(destination, userId);
            }
        }
        return message;
    }

    private boolean isChatDestination(String destination) {
        String[] parts = destination.split("/");
        return parts.length >= 4 && ("chats".equals(parts[2]) || "users".equals(parts[2]));
    }

    private void authorizeChatDestination(String destination, Long userId) {
        String[] parts = destination.split("/");

        if (parts.length >= 4 && "chats".equals(parts[2])) {
            Long chatId = parseDestinationId(parts[3]);
            requireChatParticipant(chatId, userId);
            return;
        }

        if (parts.length >= 6 && "users".equals(parts[2]) && "chats".equals(parts[4])) {
            Long destinationUserId = parseDestinationId(parts[3]);
            Long chatId = parseDestinationId(parts[5]);

            if (!destinationUserId.equals(userId)) {
                throw new MessageDeliveryException("Not allowed to access another user's chat updates");
            }

            requireChatParticipant(chatId, userId);
            return;
        }

        if ("users".equals(parts[2])) {
            throw new MessageDeliveryException("Invalid chat update destination");
        }
    }

    private Long parseDestinationId(String value) {
        try {
            return parseLong(value);
        } catch (NumberFormatException e) {
            throw new MessageDeliveryException("Invalid chat destination");
        }
    }

    private void requireChatParticipant(Long chatId, Long userId) {
        if (!chatAccessService.isParticipant(chatId, userId)) {
            throw new MessageDeliveryException("Not allowed to access this chat");
        }
    }
}
