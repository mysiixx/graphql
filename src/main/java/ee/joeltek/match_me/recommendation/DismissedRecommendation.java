package ee.joeltek.match_me.recommendation;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dismissed_recommendations")
@Data
@NoArgsConstructor
public class DismissedRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long dismissedUserId;

    public DismissedRecommendation(Long userId, Long dismissedUserId) {
        this.userId = userId;
        this.dismissedUserId = dismissedUserId;
    }
}