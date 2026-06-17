package ee.joeltek.match_me.connection;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.joeltek.match_me.chat.ChatService;
import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.common.InvalidOperationException;
import ee.joeltek.match_me.common.ResourceExistsException;
import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.common.UnauthorizedOperationException;
import ee.joeltek.match_me.profile.UserProfile;
import ee.joeltek.match_me.profile.UserProfileRepository;
import ee.joeltek.match_me.recommendation.RecommendationRepository;
import ee.joeltek.match_me.user.UserRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final RecommendationRepository recommendationRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final ChatService chatService;

    public List<ConnectionEntity> getAllAccepted(Long currentUserId) {
        return connectionRepository.findAllAcceptedForUser(ConnectionStatus.ACCEPTED, currentUserId);
    }

    public List<ConnectionEntity> getAllIncomingRequests(Long currentUserId) {
        return connectionRepository.findAllByReceiverIdAndStatus(currentUserId, ConnectionStatus.PENDING);
    }

    public List<ConnectionEntity> getAllSentRequests(Long currentUserId) {
        return connectionRepository.findAllBySenderIdAndStatus(currentUserId, ConnectionStatus.PENDING);
    }

    @Transactional
    public ConnectionEntity requestConnection(Long senderId, Long receiverId) {
        if (!userRepository.existsById(senderId)) throw new ResourceNotFoundException("Sender not found");
        if (!userRepository.existsById(receiverId)) throw new ResourceNotFoundException("Receiver not found");

        UserProfile sProfile = profileRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender profile not found"));
        if (!isCompleteProfile(sProfile))
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "Sender profile must be complete");

        UserProfile rProfile = profileRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver profile not found"));
        if (!isCompleteProfile(rProfile))
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "Receiver profile must be complete");

        if(senderId.equals(receiverId)) throw new InvalidOperationException("You cannot connect with yourself");        
        
        // Check both ways if a connection request already exists
        boolean connectionExists = connectionRepository.existsBySenderIdAndReceiverId(senderId, receiverId)
            || connectionRepository.existsBySenderIdAndReceiverId(receiverId, senderId);
        if(connectionExists) {
            throw new ResourceExistsException("Connection request already exists");
        }

        // Check both ways if a user is recommended
        boolean isRecommended = recommendationRepository.existsByOwnerUserIdAndRecommendedUserId(senderId, receiverId)
            || recommendationRepository.existsByOwnerUserIdAndRecommendedUserId(receiverId, senderId);
        if(!isRecommended) {
            throw new BusinessRuleException(HttpStatus.FORBIDDEN, "You can only connect with recommended users");
        }

        ConnectionEntity saved = connectionRepository.save(new ConnectionEntity(senderId, receiverId));

        // Clean up recommendations in both directions
        recommendationRepository.deleteByOwnerUserIdAndRecommendedUserId(senderId, receiverId);
        recommendationRepository.deleteByOwnerUserIdAndRecommendedUserId(receiverId, senderId);
        
        return saved;
    }

    // Accept pending request and create chat between users
    @Transactional
    public ConnectionEntity acceptConnection(Long targetUserId, Long currentUserId) {
        ConnectionEntity connection = connectionRepository
            .findBySenderIdAndReceiverIdAndStatus(currentUserId, targetUserId, ConnectionStatus.PENDING)
            .or(() -> connectionRepository.findBySenderIdAndReceiverIdAndStatus(targetUserId, currentUserId, ConnectionStatus.PENDING))
            .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!connection.getReceiverId().equals(currentUserId))
            throw new UnauthorizedOperationException("Only the receiver can respond to this request");

        connection.acceptConnection();
        ConnectionEntity savedConnection = connectionRepository.save(connection);

        chatService.createChat(currentUserId, connection.getSenderId());

        return savedConnection;
    }

    // Reject pending request
    @Transactional
    public ConnectionEntity rejectConnection(Long targetUserId, Long currentUserId) {
        ConnectionEntity connection = connectionRepository.findBySenderIdAndReceiverIdAndStatus(currentUserId, targetUserId, ConnectionStatus.PENDING)
            .or(() -> connectionRepository.findBySenderIdAndReceiverIdAndStatus(targetUserId, currentUserId, ConnectionStatus.PENDING))
            .orElseThrow(() -> new ResourceNotFoundException("Connection request not found"));

        if (!connection.getReceiverId().equals(currentUserId))
            throw new UnauthorizedOperationException("Only the receiver can respond to this request");

        connection.rejectConnection();
        return connectionRepository.save(connection);
    }

    // Disconnect accepted connection by changing status to REJECTED /maybe change in the future
    @Transactional
    public void disconnectConnection(Long targetUserId, Long currentUserId) {
        ConnectionEntity connection = connectionRepository
            .findBySenderIdAndReceiverId(currentUserId, targetUserId)
            .or(() -> connectionRepository.findBySenderIdAndReceiverId(targetUserId, currentUserId))
            .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (connection.getStatus() == ConnectionStatus.REJECTED) {
            throw new BusinessRuleException("This connection has already been cancelled or rejected.");
        }

        connection.rejectConnection();
    }

    private boolean isCompleteProfile(UserProfile profile) {
        return profile.getDisplayName() != null
                && profile.getFirstName() != null
                && profile.getLastName() != null
                && profile.getAboutMe() != null
                && profile.getArchetype() != null
                && profile.getBirthDate() != null
                && profile.getCity() != null;
    }
}