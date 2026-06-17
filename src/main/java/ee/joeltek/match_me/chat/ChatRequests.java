package ee.joeltek.match_me.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ChatRequests {

    // For POST /chats
    public record CreateChat(@NotNull Long targetUserId) {}

    // For POST /chats/{chatId}/messages
    public record SendMessage(@NotBlank String content) {}
}