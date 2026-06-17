package ee.joeltek.match_me.recommendation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DismissedRecommendationRepository extends JpaRepository<DismissedRecommendation, Long> {

    List<DismissedRecommendation> findAllByUserId(Long userId);

    boolean existsByUserIdAndDismissedUserId(Long userId, Long dismissedUserId);
}