package ee.joeltek.match_me.websocket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import ee.joeltek.match_me.chat.Chat;
import ee.joeltek.match_me.chat.ChatRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PresenceService {
    private final ChatRepository chatRepository;

    private final Map<Long, Set<String>> sessionsByUserId = new ConcurrentHashMap<>();

    public boolean markOnline(Long userId, String sessionId) {
        Set<String> sessions = sessionsByUserId.computeIfAbsent(
                userId,
                ignored -> ConcurrentHashMap.newKeySet()
        );

        boolean wasOffline = sessions.isEmpty();
        sessions.add(sessionId);

        return wasOffline;
    }

    public boolean markOffline(Long userId, String sessionId) {
        Set<String> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return false;
        }

        sessions.remove(sessionId);

        if (!sessions.isEmpty()) {
            return false;
        }

        sessionsByUserId.remove(userId);
        return true;
    }

    public boolean isOnline(Long userId) {
        Set<String> sessions = sessionsByUserId.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public Map<Long, Boolean> getUserChatsPresence(Long userId) {
        Map<Long, Boolean> presence = new LinkedHashMap<>();

        for (Chat chat : chatRepository.findAllChatsForUser(userId)) {
            Long otherUserId = getOtherUserId(chat, userId);
            presence.put(otherUserId, isOnline(otherUserId));
        }

        return presence;
    }

    private Long getOtherUserId(Chat chat, Long userId) {
        Long senderId = chat.getConnection().getSenderId();
        Long receiverId = chat.getConnection().getReceiverId();

        return senderId.equals(userId) ? receiverId : senderId;
    }
}
