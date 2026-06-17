package ee.joeltek.match_me.connection;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(
    name = "connections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
)
public class ConnectionEntity {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(nullable = false)
    private Long senderId;

    @Getter
    @Column(nullable = false)
    private Long receiverId;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status;

    @Getter
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Setter
    @Column(nullable = false, updatable = true)
    private Instant updatedAt;


    protected ConnectionEntity() {}
    public ConnectionEntity(Long senderId, Long receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = ConnectionStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }


    public void acceptConnection() {
        this.status = ConnectionStatus.ACCEPTED;
        this.updatedAt = Instant.now();
    }

    public void rejectConnection() {
        this.status = ConnectionStatus.REJECTED;
        this.updatedAt = Instant.now();
    }
}