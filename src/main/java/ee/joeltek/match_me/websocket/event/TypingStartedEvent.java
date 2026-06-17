package ee.joeltek.match_me.websocket.event;

public record TypingStartedEvent(
    Long chatId,
    Long userId,
    boolean isTyping
) {
    public TypingStartedEvent(Long chatId, Long userId) {
        this(chatId, userId, true);
    }
}
