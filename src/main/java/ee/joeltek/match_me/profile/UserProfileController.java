package ee.joeltek.match_me.profile;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;
    private final UserProfileService profileService;


    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getMyProfile(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(JwtAuthenticationToken authentication, @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse updatedProfile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable Long id, JwtAuthenticationToken authentication) {
        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse profile = profileService.getUserProfile(id, requesterUserId);

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/profile/picture")
    public ResponseEntity<ProfileResponse> updateProfilePicture(@RequestParam("file") MultipartFile file, JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse response = profileService.updateProfilePicture(userId, file);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/me/profile/picture")
    public ResponseEntity<ProfileResponse> deleteProfilePicture (JwtAuthenticationToken authentication){
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse response = profileService.deleteProfilePicture(userId);
        
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
