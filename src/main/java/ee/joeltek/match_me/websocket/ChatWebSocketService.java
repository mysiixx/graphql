package ee.joeltek.match_me.websocket;

import java.time.Instant;

import ee.joeltek.match_me.websocket.event.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import ee.joeltek.match_me.chat.MessageDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageSent(Long chatId, MessageDto message) {
        messagingTemplate.convertAndSend(
            "/topic/chats/" + chatId + "/message.sent",
            new MessageSentEvent(chatId, message)
        );
    }

    public void sendMessageRead(Long chatId, Long messageId, Instant readAt) {
        messagingTemplate.convertAndSend(
            "/topic/chats/" + chatId + "/message.read",
            new MessageReadEvent(chatId, messageId, readAt)
        );
    }

    public void sendChatUpdated(Long userId, Long chatId, String lastMessage, Instant lastMessageAt, Long lastMessageSenderId, long unreadCount) {
        messagingTemplate.convertAndSend(
            "/topic/users/" + userId + "/chats/" + chatId + "/chat.updated",
            new ChatUpdatedEvent(chatId, lastMessage, lastMessageAt, lastMessageSenderId, unreadCount)
        );
    }

    public void sendTypingStarted(Long chatId, Long userId) {
        messagingTemplate.convertAndSend(
            "/topic/chats/" + chatId + "/typing.started",
            new TypingStartedEvent(chatId, userId)
        );
    }

    public void sendTypingStopped(Long chatId, Long userId) {
        messagingTemplate.convertAndSend(
            "/topic/chats/" + chatId + "/typing.stopped",
            new TypingStoppedEvent(chatId, userId)
        );
    }

    public void sendUserOnline(Long chatId, Long userId) {
        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId + "/user.online",
                new UserOnlineEvent(userId)
        );
    }

    public void sendUserOffline(Long chatId, Long userId) {
        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId + "/user.offline",
                new UserOfflineEvent(userId)
        );
    }
}
