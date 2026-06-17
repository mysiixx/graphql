package ee.joeltek.match_me.profile;

import ee.joeltek.match_me.connection.ConnectionService;
import ee.joeltek.match_me.recommendation.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileAccessService {
    private final RecommendationService recommendationService;
    private final ConnectionService connectionService;

    public boolean canViewUser(Long requesterUserId, Long targetUserId){
        return recommendationService.isRecommended(requesterUserId, targetUserId) ||
                connectionService.hasOutstandingConnectionRequest(requesterUserId, targetUserId) ||
                connectionService.isConnected(requesterUserId, targetUserId) ||
                requesterUserId.equals(targetUserId);
    }
}
