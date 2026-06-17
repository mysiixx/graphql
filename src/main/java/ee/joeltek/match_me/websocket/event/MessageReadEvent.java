package ee.joeltek.match_me.websocket.event;

import java.time.Instant;

public record MessageReadEvent(
    Long chatId,
    Long messageId,
    Instant readAt
) {}
