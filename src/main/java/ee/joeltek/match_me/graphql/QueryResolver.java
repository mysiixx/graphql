package ee.joeltek.match_me.graphql;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import ee.joeltek.match_me.bio.BioResponse;
import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.chat.ChatDto;
import ee.joeltek.match_me.chat.ChatService;
import ee.joeltek.match_me.chat.MessageDto;
import ee.joeltek.match_me.chat.PaginatedResponse;
import ee.joeltek.match_me.connection.ConnectionService;
import ee.joeltek.match_me.location.UserLocationService;
import ee.joeltek.match_me.location.dto.UserLocationResponse;
import ee.joeltek.match_me.profile.ProfileResponse;
import ee.joeltek.match_me.profile.UserProfileService;
import ee.joeltek.match_me.recommendation.RecommendationScoreResponse;
import ee.joeltek.match_me.recommendation.RecommendationService;
import ee.joeltek.match_me.user.UserService;
import ee.joeltek.match_me.user.dto.OnboardingStatusResponse;
import ee.joeltek.match_me.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QueryResolver {
    private final UserProfileService profileService;
    private final UserService userService;
    private final BioService bioService;
    private final RecommendationService recommendationService;
    private final ConnectionService connectionService;
    private final UserLocationService locationService;
    private final ChatService chatService;

    @QueryMapping
    public UserResponse user(@Argument Long userId, JwtAuthenticationToken auth) {
        Long requesterUserId = Long.valueOf(auth.getToken().getSubject());
        return userService.getById(userId, requesterUserId);
    }

    @QueryMapping
    public BioResponse bio(@Argument Long userId, JwtAuthenticationToken auth) {
        Long requesterUserId = Long.valueOf(auth.getToken().getSubject());
        return bioService.getUserBio(userId, requesterUserId);
    }

    @QueryMapping
    public ProfileResponse profile(@Argument Long userId, JwtAuthenticationToken auth) {
        Long requesterUserId = Long.valueOf(auth.getToken().getSubject());
        return profileService.getUserProfile(userId, requesterUserId);
    }

    @QueryMapping
    public RecommendationScoreResponse score(@Argument Long userId, JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());
        return recommendationService.getScore(currentUserId, userId);
    }

    @QueryMapping
    public UserResponse me(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return userService.getMe(userId);
    }

    @QueryMapping
    public BioResponse myBio(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return bioService.getMyBio(userId);
    }

    @QueryMapping
    public ProfileResponse myProfile(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return profileService.getProfile(userId);
    }

    @QueryMapping
    public UserLocationResponse myLocation(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return locationService.getUserLocation(userId);
    }

    @QueryMapping
    public OnboardingStatusResponse onboardingStatus(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return userService.getMyOnboardingStatus(userId);
    }

    @QueryMapping
    public List<UserResponse> recommendations(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return recommendationService.getRecommendationsForUser(userId)
            .stream()
            .map(user -> userService.getById(user.getId(), userId))
            .toList();
    }

    @QueryMapping
    public List<UserResponse> connections(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return connectionService.getAllAccepted(userId)
            .stream()
            .map(user -> userService.getById(user.getId(), userId))
            .toList();
    }

    @QueryMapping
    public List<UserResponse> inConnectionRequests(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return connectionService.getAllIncomingRequests(userId)
            .stream()
            .map(user -> userService.getById(user.getSenderId(), userId))
            .toList();
    }

    @QueryMapping List<UserResponse> outConnectionRequests(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return connectionService.getAllSentRequests(userId)
            .stream()
            .map(user -> userService.getById(user.getReceiverId(), userId))
            .toList();
    }

    @QueryMapping List<ChatDto> chats(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return chatService.getUserChats(userId);
    }

    @QueryMapping PaginatedResponse<MessageDto> messages(@Argument Long chatId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return chatService.getChatMessages(chatId, userId, 0, 20);
    }
}

