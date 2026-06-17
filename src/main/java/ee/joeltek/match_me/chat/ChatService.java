package ee.joeltek.match_me.chat;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.common.UnauthorizedOperationException;
import ee.joeltek.match_me.connection.ConnectionEntity;
import ee.joeltek.match_me.connection.ConnectionRepository;
import ee.joeltek.match_me.connection.ConnectionStatus;
import ee.joeltek.match_me.websocket.ChatWebSocketService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ConnectionRepository connectionRepository;
    private final ChatWebSocketService webSocketService;

    // 1. Fetching Inbox with Unread Counts
    @Transactional(readOnly = true)
    public List<ChatDto> getUserChats(Long userId) {
        List<Chat> chats = chatRepository.findAllChatsForUser(userId);

        return chats.stream().map(chat -> {
            Long otherUserId = chat.getConnection().getSenderId().equals(userId)
                    ? chat.getConnection().getReceiverId()
                    : chat.getConnection().getSenderId();

            long unreadCount = messageRepository.countUnreadMessagesForUserInChat(chat.getId(), userId);

            return new ChatDto(chat.getId(), otherUserId, chat.getLastMessageAt(), unreadCount);
        }).collect(Collectors.toList());
    }

    // 2. Creating a new Chat Room
    @Transactional
    public ChatDto createChat(Long currentUserId, Long targetUserId) {
        ConnectionEntity connection = connectionRepository.findAllAcceptedForUser(ConnectionStatus.ACCEPTED, currentUserId)
                .stream()
                .filter(c -> c.getSenderId().equals(targetUserId) || c.getReceiverId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> new UnauthorizedOperationException("You must have an accepted connection to start a chat."));

        Chat chat = chatRepository.findByConnectionId(connection.getId())
                .orElseGet(() -> chatRepository.save(new Chat(connection)));

        return new ChatDto(chat.getId(), targetUserId, chat.getLastMessageAt(), 0); // Unread is 0 for a new chat
    }

    // 3. Sending a Message
    @Transactional
    public MessageDto sendMessage(Long chatId, Long senderId, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        if (
            !chat.getConnection().getSenderId().equals(senderId)
            && !chat.getConnection().getReceiverId().equals(senderId)
            || !chat.getConnection().getStatus().equals(ConnectionStatus.ACCEPTED)
        ) {
            throw new UnauthorizedOperationException("You are not a participant in this chat.");
        }

        Message message = new Message(chat, senderId, content);
        Message savedMessage = messageRepository.save(message);

        chat.setLastMessageAt(savedMessage.getSentAt());
        chatRepository.save(chat);

        MessageDto dto = new MessageDto(
                savedMessage.getId(),
                chat.getId(),
                savedMessage.getSenderId(),
                savedMessage.getContent(),
                savedMessage.getSentAt(),
                savedMessage.getReadAt()
        );

        // Send WebSocket chat.updated, message.sent events
        Long recipientId = chat.getConnection().getSenderId().equals(senderId)
                ? chat.getConnection().getReceiverId()
                : chat.getConnection().getSenderId();
        long senderUnreadCount = messageRepository.countUnreadMessagesForUserInChat(chat.getId(), senderId);
        long recipientUnreadCount = messageRepository.countUnreadMessagesForUserInChat(chat.getId(), recipientId);

        webSocketService.sendMessageSent(chatId, dto);
        webSocketService.sendChatUpdated(senderId, chatId, savedMessage.getContent(), chat.getLastMessageAt(), senderId, senderUnreadCount);
        webSocketService.sendChatUpdated(recipientId, chatId, savedMessage.getContent(), chat.getLastMessageAt(), senderId, recipientUnreadCount);

        return dto;
    }

    // 4. Fetching Messages (with Pagination)
    @Transactional
    public PaginatedResponse<MessageDto> getChatMessages(Long chatId, Long userId, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        if (
                !chat.getConnection().getSenderId().equals(userId)
                && !chat.getConnection().getReceiverId().equals(userId)
                || !chat.getConnection().getStatus().equals(ConnectionStatus.ACCEPTED)
        ) {
            throw new UnauthorizedOperationException("You are not a participant in this chat.");
        }

        Page<Message> messagePage = messageRepository.findByChatIdOrderBySentAtDesc(chatId, PageRequest.of(page, size));

        List<MessageDto> items = messagePage.getContent().stream()
                .map(m -> new MessageDto(m.getId(), chat.getId(), m.getSenderId(), m.getContent(), m.getSentAt(), m.getReadAt()))
                .collect(Collectors.toList());

        return new PaginatedResponse<>(
                items,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages()
        );
    }

    // Mark unread messages as read and send WebSocket event
    @Transactional
    public void markChatAsRead(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

        if (!chat.getConnection().getSenderId().equals(userId)
                && !chat.getConnection().getReceiverId().equals(userId)) {
            throw new UnauthorizedOperationException("You are not a participant in this chat.");
        }

        List<Message> unreadMessages = messageRepository.findAllUnreadMessagesForUserInChat(chatId, userId);

        if (unreadMessages.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        unreadMessages.forEach(message -> message.setReadAt(now));
        messageRepository.saveAll(unreadMessages);

        unreadMessages.forEach(message ->
                webSocketService.sendMessageRead(chatId, message.getId(), now)
        );

        Message lastMessage = messageRepository.findFirstByChat_IdOrderBySentAtDesc(chatId)
                .orElseThrow(() -> new ResourceNotFoundException("Last message not found"));

        long unreadCount = messageRepository.countUnreadMessagesForUserInChat(chatId, userId);
        webSocketService.sendChatUpdated(
                userId,
                chatId,
                lastMessage.getContent(),
                lastMessage.getSentAt(),
                lastMessage.getSenderId(),
                unreadCount
        );
    }
}
