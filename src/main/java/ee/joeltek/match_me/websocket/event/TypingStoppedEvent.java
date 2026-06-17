package ee.joeltek.match_me.websocket.event;

public record TypingStoppedEvent(
    Long chatId,
    Long userId,
    boolean isTyping
) {
    public TypingStoppedEvent(Long chatId, Long userId) {
        this(chatId, userId, false);
    }
}
