package ee.joeltek.match_me.websocket.event;

public record UserOfflineEvent(Long userId, boolean isOnline) {
    public UserOfflineEvent(Long userId) {
        this(userId, false);
    }
}
