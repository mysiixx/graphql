package ee.joeltek.match_me.bio;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BioController {
    private final BioService bioService;

    @PutMapping("/me/bio")
    public ResponseEntity<Map<String, String>> submitBio(@Valid @RequestBody BioRequest request, JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return ResponseEntity.ok(bioService.submitBio(userId, request));
    }

    @GetMapping("/me/bio")
    public ResponseEntity<BioResponse> getMyBio(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        BioResponse bio = bioService.getMyBio(userId);
        return ResponseEntity.ok(bio);
    }

    @GetMapping("/users/{id}/bio")
    public ResponseEntity<BioResponse> getUserBio(@PathVariable Long id, JwtAuthenticationToken authentication) {
        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());
        BioResponse bio = bioService.getUserBio(id, requesterUserId);
        return ResponseEntity.ok(bio);
    }
}