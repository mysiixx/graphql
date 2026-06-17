package ee.joeltek.match_me.recommendation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findAllByOwnerUserIdOrderByRankOrderAsc(Long ownerUserId);
    boolean existsByOwnerUserIdAndRecommendedUserId(Long ownerUserId, Long recommendedUserId);
    boolean existsByOwnerUserId(Long ownerUserId);
    void deleteAllByOwnerUserId(Long ownerUserId);
    void deleteByOwnerUserIdAndRecommendedUserId(Long ownerUserId, Long recommendedUserId);
    Optional<Recommendation> findByOwnerUserIdAndRecommendedUserId(Long ownerUserId, Long recommendedUserId);
}
