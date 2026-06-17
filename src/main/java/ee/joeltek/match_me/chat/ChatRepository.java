package ee.joeltek.match_me.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
                SELECT c FROM Chat c
                WHERE (c.connection.senderId = :userId OR c.connection.receiverId = :userId)
                  AND c.connection.status = ee.joeltek.match_me.connection.ConnectionStatus.ACCEPTED
                ORDER BY c.lastMessageAt DESC
            """)
    List<Chat> findAllChatsForUser(@Param("userId") Long userId);

    @Query("""
                SELECT count(c) > 0
                FROM Chat c
                WHERE c.id = :chatId
                  AND (c.connection.senderId = :userId or c.connection.receiverId = :userId)
                  AND c.connection.status = ee.joeltek.match_me.connection.ConnectionStatus.ACCEPTED
            """)
    boolean existsParticipant(Long chatId, Long userId);

    Optional<Chat> findByConnectionId(Long connectionId);

    boolean existsByConnectionId(Long connectionId);
}