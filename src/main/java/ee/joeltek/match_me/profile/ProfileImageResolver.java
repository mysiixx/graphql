package ee.joeltek.match_me.profile;

import org.springframework.stereotype.Service;

@Service
public class ProfileImageResolver {

    public String resolveProfileImageUrl (UserProfile userProfile) {

        if (userProfile.getCustomProfilePictureUrl() != null && !userProfile.getCustomProfilePictureUrl().isBlank()){
            return userProfile.getCustomProfilePictureUrl();
        }
        else if (userProfile.getArchetype() != null && !userProfile.getArchetype().isBlank()) {
            String archetype = userProfile.getArchetype();
            return "/avatars/" + archetype + ".png";
        }
        return "/avatars/DEFAULT.png";
    }

}
