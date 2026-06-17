package ee.joeltek.match_me.recommendation;

import ee.joeltek.match_me.bio.BioRepository;
import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.bio.UserBio;
import ee.joeltek.match_me.connection.ConnectionRepository;
import ee.joeltek.match_me.connection.ConnectionStatus;
import ee.joeltek.match_me.location.UserLocationRepository;
import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.profile.UserProfile;
import ee.joeltek.match_me.profile.UserProfileRepository;
import ee.joeltek.match_me.profile.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BioRepository bioRepository;
    private final BioService bioService;
    private final DismissedRecommendationRepository dismissedRepository;
    private final RecommendationRepository recommendationRepository;
    private final UserLocationRepository userLocationRepository;
    private final ConnectionRepository connectionRepository;
    private final UserProfileService userProfileService;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public List<RecommendationResponse> getRecommendationsForUser(Long userId) {
        // 1. Validate prerequisites (Must have a Bio and Location)
        UserBio myBio = validateRecommendationPrerequisites(userId);
        UserProfile myProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.FORBIDDEN, "Profile not found for user: " + userId));

        // 2. Rebuild the recommendation snapshot from the current relationship state.
        recommendationRepository.deleteAllByOwnerUserId(userId);

        // 3. Get nearby user IDs
        List<Long> nearbyUserIds = userLocationRepository.findMutualUserIdsWithinRadius(userId);
        List<Long> connectedOrPendingUserIds = connectionRepository.findConnectedAndPendingUserIdsByUserId(
                userId,
                List.of(ConnectionStatus.ACCEPTED, ConnectionStatus.PENDING)
        );

        // 4. Calculate Match Scores
        record MatchCandidate(Long targetUserId, double score) {
        }
        List<MatchCandidate> candidates = new ArrayList<>();

        for (Long targetUserId : nearbyUserIds) {
            // Skip if target profile or bio not completed
            if (!isRecommendationEligible(targetUserId)) {
                continue;
            }

            UserProfile targetProfile = userProfileRepository.findById(targetUserId).orElseThrow();
            if (targetProfile.getConnectionType() != myProfile.getConnectionType()) {
                continue;
            }

            // Skip if already dismissed or already in an active relationship flow.
            if (dismissedRepository.existsByUserIdAndDismissedUserId(userId, targetUserId)
                    || connectedOrPendingUserIds.contains(targetUserId)) {
                continue;
            }

            // Get target's bio and calculate percentage
            bioRepository.findById(targetUserId).ifPresent(targetBio -> {
                double matchPercentage = bioService.calculateMatchPercentage(myBio, targetBio);

                // Optional: Only recommend people with > 50% match
                if (matchPercentage >= 50.0) {
                    candidates.add(new MatchCandidate(targetUserId, matchPercentage));
                }
            });
        }

        // 5. Sort by best match score descending
        candidates.sort(Comparator.comparingDouble(MatchCandidate::score).reversed());

        // 6. Limit to top 10 and save to DB
        List<Recommendation> newRecommendations = new ArrayList<>();
        Instant generatedAt = Instant.now();
        int rank = 1;

        for (MatchCandidate candidate : candidates.stream().limit(10).toList()) {
            Recommendation rec = new Recommendation();
            rec.setOwnerUserId(userId);
            rec.setRecommendedUserId(candidate.targetUserId());
            rec.setScore(candidate.score());
            rec.setRankOrder(rank++);
            rec.setGeneratedAt(generatedAt);
            newRecommendations.add(rec);
        }

        recommendationRepository.saveAll(newRecommendations);

        // 7. Return the response
        return newRecommendations.stream()
                .map(r -> new RecommendationResponse(r.getRecommendedUserId()))
                .toList();
    }

    private boolean isRecommendationEligible(Long userId) {
        return userProfileRepository.findById(userId)
                .map(profile -> userProfileService.isProfileComplete(profile))
                .orElse(false)
                && bioService.isBioComplete(userId);
    }

    private UserBio validateRecommendationPrerequisites(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.FORBIDDEN, "Profile not found for user: " + userId));

        if (!userProfileService.isProfileComplete(profile)) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "Profile is incomplete for user: " + userId);
        }

        if (!bioService.isBioComplete(userId)) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "Bio is incomplete for user: " + userId);
        }

        return bioRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuleException(HttpStatus.FORBIDDEN, "Bio not found for user: " + userId));
    }

    @Transactional
    public void dismissRecommendation(Long userId, Long targetUserId) {
        // Save the dismissal so this user doesn't show up in the feed again
        // And remove the recommendation from database
        if (!dismissedRepository.existsByUserIdAndDismissedUserId(userId, targetUserId)) {
            dismissedRepository.save(new DismissedRecommendation(userId, targetUserId));
            recommendationRepository.deleteByOwnerUserIdAndRecommendedUserId(userId, targetUserId);
        }
    }

    public boolean isRecommended(Long requesterUserId, Long targetUserId) {
        // Checks if a recommendation exists between these two users
        return recommendationRepository.existsByOwnerUserIdAndRecommendedUserId(requesterUserId, targetUserId);
    }

    // Returns match percentage
    public RecommendationScoreResponse getScore(Long currentUserId, Long recommendationId) {
        Recommendation r = recommendationRepository.findByOwnerUserIdAndRecommendedUserId(currentUserId, recommendationId)
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found with id: " + recommendationId));
            
        return new RecommendationScoreResponse(r.getScore());
    }
}
