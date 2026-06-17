package ee.joeltek.match_me.chat;
import java.time.Instant;

public record MessageDto(Long id,
                         Long chatId,
                         Long senderId,
                         String content,
                         Instant sentAt,
                         Instant readAt) {}