package ee.joeltek.match_me.profile;

import ee.joeltek.match_me.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @Getter
    private Long userId;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Getter
    @Setter
    @Column(length = 50, unique = true)
    private String displayName;

    @Getter
    @Setter
    @Column(length = 50)
    private String firstName;

    @Getter
    @Setter
    @Column(length = 50)
    private String lastName;

    @Getter
    @Setter
    @Column
    private String customProfilePictureUrl;

    @Getter
    @Setter
    @Column(length = 50)
    private String city;

    @Getter
    @Setter
    @Column
    private LocalDate birthDate;

    @Getter
    @Setter
    @Column(length = 500)
    private String aboutMe;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ConnectionType connectionType;

    @Getter
    @Setter
    @Column
    private String archetype;

    public UserProfile() {
    }

    public UserProfile(UserEntity user) {
        this.user = user;
    }
}
