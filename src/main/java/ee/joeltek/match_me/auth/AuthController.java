package ee.joeltek.match_me.auth;

import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.dto.RegisterUserResponse;
import ee.joeltek.match_me.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor //thanks to this no need to write constructor code
@RestController
public class AuthController {

    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> createNewUser(@Valid @RequestBody RegisterRequest request){
        UserEntity newUser = userService.createUser(request.getEmail(), request.getPassword());
        RegisterUserResponse response = new RegisterUserResponse(
                newUser.getUserId(),
                newUser.getEmail()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = this.tokenService.login(
                request.getEmail(),
                request.getPassword(),
                request.isRememberMe()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(tokenService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody(required = false) RefreshTokenRequest request
    ) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            tokenService.revokeRefreshToken(request.getRefreshToken());
        }

        return ResponseEntity.noContent().build();
    }
}
