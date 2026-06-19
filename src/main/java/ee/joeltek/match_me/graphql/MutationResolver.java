package ee.joeltek.match_me.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import ee.joeltek.match_me.auth.LoginRequest;
import ee.joeltek.match_me.auth.LoginResponse;
import ee.joeltek.match_me.auth.RegisterRequest;
import ee.joeltek.match_me.auth.TokenService;
import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserService;
import ee.joeltek.match_me.user.dto.RegisterUserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MutationResolver {
    private final UserService userService;
    private final TokenService tokenService;

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
}
