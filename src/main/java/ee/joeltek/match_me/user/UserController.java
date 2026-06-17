package ee.joeltek.match_me.user;

import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.profile.ProfileAccessService;
import ee.joeltek.match_me.user.dto.OnboardingStatusResponse;
import ee.joeltek.match_me.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final ProfileAccessService profileAccessService;

    @GetMapping("/me")
    public UserResponse getMe(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return userService.getById(userId);
    }

    @GetMapping("/me/onboarding-status")
    public OnboardingStatusResponse getMyOnboardingStatus(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return userService.getMyOnboardingStatus(userId);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id, JwtAuthenticationToken authentication) {
        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());

        if (profileAccessService.canViewUser(requesterUserId, id)) {
            return userService.getById(id);
        } else throw new ResourceNotFoundException("User not found");
    }
}
