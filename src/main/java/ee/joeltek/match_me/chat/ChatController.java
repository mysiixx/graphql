package ee.joeltek.match_me.chat;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@CrossOrigin
public class ChatController {

    private final ChatService chatService;

    // 1. Get all chats (Inbox)
    @GetMapping
    public ResponseEntity<List<ChatDto>> getChats(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());

        List<ChatDto> chats = chatService.getUserChats(userId);
        return ResponseEntity.ok(chats);
    }

    // 2. Create a new chat room from an accepted connection
    @PostMapping
    public ResponseEntity<ChatDto> createChat(
            @Valid @RequestBody ChatRequests.CreateChat request,
            JwtAuthenticationToken auth) {

        Long userId = Long.valueOf(auth.getToken().getSubject());

        ChatDto chat = chatService.createChat(userId, request.targetUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    // 3. Get message history with pagination
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<PaginatedResponse<MessageDto>> getMessages(
            @PathVariable("chatId") Long chatId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            JwtAuthenticationToken auth) {

        Long userId = Long.valueOf(auth.getToken().getSubject());

        PaginatedResponse<MessageDto> response = chatService.getChatMessages(chatId, userId, page, size);
        return ResponseEntity.ok(response);
    }

    // 4. Send a new message
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable("chatId") Long chatId,
            @Valid @RequestBody ChatRequests.SendMessage request,
            JwtAuthenticationToken auth) {

        Long userId = Long.valueOf(auth.getToken().getSubject());

        MessageDto savedMessage = chatService.sendMessage(chatId, userId, request.content());

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMessage);
    }

    //mark recipient messages read
    @PostMapping("/{chatId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markChatAsRead(
            @PathVariable Long chatId,
            JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());

        chatService.markChatAsRead(chatId, userId);
    }
}