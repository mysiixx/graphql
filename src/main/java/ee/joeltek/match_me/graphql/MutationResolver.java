package ee.joeltek.match_me.graphql;

import java.util.Map;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import ee.joeltek.match_me.auth.LoginRequest;
import ee.joeltek.match_me.auth.LoginResponse;
import ee.joeltek.match_me.auth.RegisterRequest;
import ee.joeltek.match_me.auth.TokenService;
import ee.joeltek.match_me.bio.BioRequest;
import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.chat.ChatDto;
import ee.joeltek.match_me.chat.ChatRequests;
import ee.joeltek.match_me.chat.ChatService;
import ee.joeltek.match_me.chat.MessageDto;
import ee.joeltek.match_me.connection.ConnectionService;
import ee.joeltek.match_me.location.UserLocationService;
import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.location.dto.UserLocationResponse;
import ee.joeltek.match_me.profile.ProfileResponse;
import ee.joeltek.match_me.profile.UpdateProfileRequest;
import ee.joeltek.match_me.profile.UserProfileService;
import ee.joeltek.match_me.recommendation.RecommendationService;
import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserService;
import ee.joeltek.match_me.user.dto.RegisterUserResponse;
import ee.joeltek.match_me.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MutationResolver {
    private final TokenService tokenService;
    private final UserService userService;
    private final UserLocationService locationService;
    private final UserProfileService profileService;
    private final ChatService chatService;
    private final BioService bioService;
    private final ConnectionService connectionService;
    private final RecommendationService recommendationService;

    @MutationMapping
    public RegisterUserResponse register(@Argument @Valid RegisterRequest request) {
        UserEntity newUser = userService.createUser(request.getEmail(), request.getPassword());

        return new RegisterUserResponse(
                newUser.getUserId(),
                newUser.getEmail());
    }

    @MutationMapping
    public LoginResponse login(@Argument @Valid LoginRequest request) {
        LoginResponse response = this.tokenService.login(
            request.getEmail(),
            request.getPassword(),
            request.isRememberMe()
        );

        return response;
    }

    @MutationMapping
    public ChatDto createChat(@Argument ChatRequests.CreateChat request, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return chatService.createChat(userId, request.targetUserId());
    }

    @MutationMapping
    public MessageDto sendMessage(@Argument Long chatId, @Argument ChatRequests.SendMessage request, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return chatService.sendMessage(chatId, userId, request.content());
    }

    @MutationMapping
    public Map<String, String> submitBio(@Argument BioRequest request, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return bioService.submitBio(userId, request);
    }

    @MutationMapping
    public UserResponse requestConnection(@Argument Long targetUserId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        connectionService.requestConnection(userId, targetUserId);
        return userService.getById(targetUserId, userId);
    }

    @MutationMapping
    public UserResponse acceptConnection(@Argument Long targetUserId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        connectionService.acceptConnection(targetUserId, userId);
        return userService.getById(targetUserId, userId);
    }

    @MutationMapping
    public UserResponse rejectConnection(@Argument Long targetUserId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        connectionService.acceptConnection(targetUserId, userId);
        return userService.getById(targetUserId, userId);
    }

    @MutationMapping
    public Boolean disconnectConnection(@Argument Long targetUserId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        connectionService.disconnectConnection(targetUserId, userId);
        return true;
    }

    @MutationMapping
    public UserLocationResponse updateLocation(@Argument UpdateUserLocationRequest request, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return locationService.updateUserLocation(userId, request);
    }

    @MutationMapping
    public ProfileResponse updateProfile(@Argument UpdateProfileRequest request, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return profileService.updateProfile(userId, request);
    }

    @MutationMapping
    public Boolean dismissRecommendation(@Argument Long targetUserId, JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        recommendationService.dismissRecommendation(userId, targetUserId);
        return true;
    }
}
