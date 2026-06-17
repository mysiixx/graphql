package ee.joeltek.match_me.chat;
import java.time.Instant;

public record ChatDto(Long id,
                      Long otherUserId,
                      Instant lastMessageAt,
                      long unreadCount) {}