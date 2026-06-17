package ee.joeltek.match_me.auth;

import ee.joeltek.match_me.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Getter
    @Setter
    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Getter
    @Setter
    @Column(nullable = false)
    private Instant expiresAt;

    @Getter
    @Setter
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Getter
    @Setter
    private Instant revokedAt;

    public boolean isActive(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
