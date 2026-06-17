package ee.joeltek.match_me.websocket.event;

import ee.joeltek.match_me.chat.MessageDto;

public record MessageSentEvent(
    Long chatId,
    MessageDto message
) {}
