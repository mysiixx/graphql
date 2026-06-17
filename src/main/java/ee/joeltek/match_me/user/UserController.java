package ee.joeltek.match_me.user;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import ee.joeltek.match_me.user.dto.OnboardingStatusResponse;
import ee.joeltek.match_me.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return userService.getMe(userId);
    }

    @GetMapping("/me/onboarding-status")
    public OnboardingStatusResponse getMyOnboardingStatus(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return userService.getMyOnboardingStatus(userId);
    }

    @GetMapping("/users/{id}")
    public UserResponse getUserById(@PathVariable Long id, JwtAuthenticationToken authentication) {
        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());
        return userService.getById(id, requesterUserId);
    }
}
