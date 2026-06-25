package ee.joeltek.match_me.graphql;

import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import ee.joeltek.match_me.bio.BioResponse;
import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.profile.ProfileResponse;
import ee.joeltek.match_me.profile.UserProfileService;
import ee.joeltek.match_me.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserResolver {
    private final UserProfileService profileService;
    private final BioService bioService;

    @SchemaMapping(typeName = "User", field = "profile")
    public ProfileResponse profile(UserResponse user) {
        return profileService.getProfile(user.getId());
    }

    @SchemaMapping(typeName = "User", field = "bio")
    public BioResponse bio(JwtAuthenticationToken auth, UserResponse user) {
        Long userId = Long.valueOf(auth.getToken().getSubject());
        return bioService.getUserBio(user.getId(), userId);
    }
}
