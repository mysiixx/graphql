package ee.joeltek.match_me.bio;

import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.profile.ProfileAccessService;
import ee.joeltek.match_me.profile.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BioController {

    private final BioService bioService;
    private final BioRepository bioRepository;
    private final UserProfileService userProfileService;
    private final ProfileAccessService profileAccessService;

    @PutMapping("/me/bio")
    public ResponseEntity<Map<String, String>> submitBio(
            @Valid @RequestBody BioRequest request,
            JwtAuthenticationToken authentication) {

        Long userId = Long.valueOf(authentication.getToken().getSubject());
        UserBio bio = bioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));

        Archetype winningArchetype = bioService.updateBioScores(bio, request.getAnswers());
        bioRepository.save(bio);
        userProfileService.updateArchetype(userId, winningArchetype.name());

        return ResponseEntity.ok(Map.of(
                "message", "Bio processed successfully!",
                "archetype", winningArchetype.name()
        ));
    }

    @GetMapping("/me/bio")
    public ResponseEntity<BioResponse> getMyBio(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return fetchBioResponse(userId);
    }

    @GetMapping("/users/{id}/bio")
    public ResponseEntity<BioResponse> getUserBio(@PathVariable Long id, JwtAuthenticationToken authentication) {

        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());

        if (profileAccessService.canViewUser(requesterUserId, id)) {
            return fetchBioResponse(id);
        } else throw new ResourceNotFoundException("User not found");
    }

    private ResponseEntity<BioResponse> fetchBioResponse(Long userId) {
        UserBio bio = bioRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));

        BioResponse response = new BioResponse(
                bio.getUserId(),
                bio.getVisionaryScore(),
                bio.getChallengerScore(),
                bio.getArchitectScore(),
                bio.getHarmonizerScore(),
                bio.getExplorerScore(),
                bio.getExecutorScore()
        );
        return ResponseEntity.ok(response);
    }
}