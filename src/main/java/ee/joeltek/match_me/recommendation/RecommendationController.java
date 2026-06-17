package ee.joeltek.match_me.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@CrossOrigin
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(JwtAuthenticationToken authentication) {

        Long userId = Long.valueOf(authentication.getToken().getSubject());

        List<RecommendationResponse> matches = recommendationService.getRecommendationsForUser(userId);

        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{recommendedUserId}/score")
    public ResponseEntity<RecommendationScoreResponse> getScore(@PathVariable("recommendedUserId") Long recommendedUserId, JwtAuthenticationToken authentication) {
        Long currentUserId = Long.valueOf(authentication.getToken().getSubject());

        return ResponseEntity.ok(recommendationService.getScore(currentUserId, recommendedUserId));
    }

    @PostMapping("/{targetUserId}/dismiss")
    public ResponseEntity<Void> dismissRecommendation(
            @PathVariable("targetUserId") Long targetUserId,
            JwtAuthenticationToken authentication) {

        Long userId = Long.valueOf(authentication.getToken().getSubject());

        recommendationService.dismissRecommendation(userId, targetUserId);

        return ResponseEntity.noContent().build();
    }
}