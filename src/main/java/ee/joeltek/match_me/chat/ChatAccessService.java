package ee.joeltek.match_me.chat;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatAccessService {
    private final ChatRepository chatRepository;

    public boolean isParticipant(Long chatId, Long userId) {
        return chatRepository.existsParticipant(chatId, userId);
    }
}