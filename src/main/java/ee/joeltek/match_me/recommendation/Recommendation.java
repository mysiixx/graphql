package ee.joeltek.match_me.recommendation;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table (
        name = "recommendations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"owner_user_id", "recommended_user_id"}),
                @UniqueConstraint(columnNames = {"owner_user_id", "rank_order"})
        }
)
public class Recommendation {

    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Setter
    @Column( name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Getter
    @Setter
    @Column( name = "recommended_user_id", nullable = false)
    private Long recommendedUserId;

    @Getter
    @Setter
    @Column ( nullable = false)
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private double score;

    @Getter
    @Setter
    @Column( nullable = false, name = "rank_order" )
    @Min(1)
    @Max(10)
    private int rankOrder;

    @Getter
    @Setter
    @Column( nullable = false)
    private Instant generatedAt;

}
