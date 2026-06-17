package ee.joeltek.match_me.connection;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
    boolean existsBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, ConnectionStatus status);

    Optional<ConnectionEntity> findBySenderIdAndReceiverId(Long senderId, Long receiverId);
    Optional<ConnectionEntity> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, ConnectionStatus status);

    List<ConnectionEntity> findAllByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    List<ConnectionEntity> findAllBySenderIdAndStatus(Long senderId, ConnectionStatus status);

    @Query("""
               SELECT c FROM ConnectionEntity c
               WHERE c.status = :status
               AND (c.senderId = :userId OR c.receiverId = :userId)
            """)
    List<ConnectionEntity> findAllAcceptedForUser(
            @Param("status") ConnectionStatus status,
            @Param("userId") Long userId
    );

    @Query("""
            SELECT
              CASE
                WHEN connection.senderId = :userId THEN connection.receiverId
                ELSE connection.senderId
              END
            FROM ConnectionEntity connection
            WHERE connection.status IN (:statusesToConsider)
              AND (connection.senderId = :userId OR connection.receiverId = :userId)
            """)
    List<Long> findConnectedAndPendingUserIdsByUserId(@Param("userId") Long userId, @Param("statusesToConsider") List<ConnectionStatus> statusesToConsider);
}