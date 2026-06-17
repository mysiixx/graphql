package ee.joeltek.match_me.bio;

import ee.joeltek.match_me.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="bios")
public class UserBio {

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
    @Column
    private int visionaryScore;

    @Getter
    @Setter
    @Column
    private int challengerScore;

    @Getter
    @Setter
    @Column
    private int architectScore;

    @Getter
    @Setter
    @Column
    private int harmonizerScore;

    @Getter
    @Setter
    @Column
    private int explorerScore;

    @Getter
    @Setter
    @Column
    private int executorScore;

    public UserBio() {
    }

    public UserBio(UserEntity user) {
        this.user = user;
    }
}
