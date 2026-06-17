package ee.joeltek.match_me.websocket.event;

import java.time.Instant;

public record ChatUpdatedEvent(
    Long chatId,
    String lastMessage,
    Instant lastMessageAt,
    Long lastMessageSenderId,
    long unreadCount
) {}
