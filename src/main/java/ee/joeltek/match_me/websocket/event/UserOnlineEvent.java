package ee.joeltek.match_me.websocket.event;

public record UserOnlineEvent(Long userId, boolean isOnline) {
    public UserOnlineEvent(Long userId) {
        this(userId, true);
    }
}
