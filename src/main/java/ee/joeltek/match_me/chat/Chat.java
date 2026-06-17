package ee.joeltek.match_me.chat;

import ee.joeltek.match_me.connection.ConnectionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connection_id", nullable = false, unique = true)
    private ConnectionEntity connection;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastMessageAt;

    public Chat(ConnectionEntity connection) {
        this.connection = connection;
        this.createdAt = Instant.now();
        this.lastMessageAt = Instant.now();
    }
}