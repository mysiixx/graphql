package ee.joeltek.match_me.graphql;

import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import ee.joeltek.match_me.bio.BioResponse;
import ee.joeltek.match_me.user.UserService;
import ee.joeltek.match_me.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BioResolver {
    private final UserService userService;
    
    @SchemaMapping(typeName = "Bio", field = "user")
    public UserResponse user(JwtAuthenticationToken auth, BioResponse bio) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return userService.getById(bio.getId(), userId);
    }
}
