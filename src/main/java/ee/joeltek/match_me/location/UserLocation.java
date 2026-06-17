package ee.joeltek.match_me.location;

import java.time.Instant;

import org.locationtech.jts.geom.Point;

import ee.joeltek.match_me.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "user_locations")
public class UserLocation {

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
    @Column(columnDefinition = "geography(Point,4326)")
    private Point location;

    @Getter
    @Setter
    @Min(10000) //10 km
    @Max(20000000) //20 000 km - halfway round earth
    @Column
    private Integer preferredRadiusMeters;

    @Getter
    private Instant updatedAt;

    @Getter
    @Setter
    @Column
    @Enumerated(EnumType.STRING)
    private LocationSource source;

    @PrePersist
    public void initializeUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void updateUpdatedAt() {
        this.updatedAt = Instant.now();
    }

    public UserLocation() {
    }

    public UserLocation(UserEntity user) {
        this.user = user;
    }
}