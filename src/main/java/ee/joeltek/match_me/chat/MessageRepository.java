package ee.joeltek.match_me.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByChatIdOrderBySentAtDesc(Long chatId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.senderId != :userId AND m.readAt IS NULL")
    long countUnreadMessagesForUserInChat(@Param("chatId") Long chatId, @Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.senderId != :userId AND m.readAt IS NULL")
    List<Message> findAllUnreadMessagesForUserInChat(@Param("chatId") Long chatId, @Param("userId") Long userId);

    Optional<Message> findFirstByChat_IdOrderBySentAtDesc (Long chatId);
}