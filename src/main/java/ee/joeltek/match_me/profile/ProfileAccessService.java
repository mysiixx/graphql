package ee.joeltek.match_me.profile;

import org.springframework.stereotype.Service;

import ee.joeltek.match_me.connection.ConnectionRepository;
import ee.joeltek.match_me.connection.ConnectionStatus;
import ee.joeltek.match_me.recommendation.RecommendationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileAccessService {
    private final ConnectionRepository connectionRepository;
    private final RecommendationRepository recommendationRepository;

    public boolean canViewUser(Long requesterUserId, Long targetUserId){
        return recommendationRepository.existsByOwnerUserIdAndRecommendedUserId(requesterUserId, targetUserId) 
            || connectionRepository.existsBySenderIdAndReceiverIdAndStatus(requesterUserId, targetUserId, ConnectionStatus.PENDING)
            || connectionRepository.existsBySenderIdAndReceiverIdAndStatus(targetUserId, requesterUserId, ConnectionStatus.PENDING)
            || connectionRepository.existsBySenderIdAndReceiverIdAndStatus(requesterUserId, targetUserId, ConnectionStatus.ACCEPTED)
            || connectionRepository.existsBySenderIdAndReceiverIdAndStatus(targetUserId, requesterUserId, ConnectionStatus.ACCEPTED)
            || requesterUserId.equals(targetUserId);
    }
}
