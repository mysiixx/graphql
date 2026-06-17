package ee.joeltek.match_me.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ee.joeltek.match_me.bio.UserBio;
import ee.joeltek.match_me.profile.UserProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.Instant;


@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Getter
    @Setter
    @Column(nullable = false, unique = true)
    @JsonIgnore //additional rule to prevent e-mail leaking
    private String email;

    @Getter
    @Setter
    @Column(nullable = false)
    @JsonIgnore //additional rule to prevent passwordHash leaking
    private String passwordHash;

    @Getter
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserBio bio;

    public UserEntity() {

    }
    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }
    public void setProfile(UserProfile profile) {
        this.profile = profile;
        if (profile != null) {
            profile.setUser(this);
        }
    }
    public void setBio(UserBio bio) {
        this.bio = bio;
        if (bio != null) {
            bio.setUser(this);
        }
    }
}
