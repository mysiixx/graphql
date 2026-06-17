package ee.joeltek.match_me.chat;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    private Instant readAt;

    public Message(Chat chat, Long senderId, String content) {
        this.chat = chat;
        this.senderId = senderId;
        this.content = content;
        this.sentAt = Instant.now();
    }
}