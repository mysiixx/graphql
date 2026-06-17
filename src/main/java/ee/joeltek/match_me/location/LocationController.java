package ee.joeltek.match_me.location;

import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.location.dto.UserLocationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/location")
@RequiredArgsConstructor
public class LocationController {

    private final UserLocationService userLocationService;

    @GetMapping
    public ResponseEntity<UserLocationResponse> getMyLocation(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity.ok(userLocationService.getUserLocation(userId));
    }

    @PutMapping
    public ResponseEntity<UserLocationResponse> updateLocation(
            @Valid @RequestBody UpdateUserLocationRequest request,
            JwtAuthenticationToken auth) {

        Long userId = Long.valueOf(auth.getToken().getSubject());
        return ResponseEntity.ok(userLocationService.updateUserLocation(userId, request));
    }

    @GetMapping("/discovery")
    public ResponseEntity<List<UserLocationResponse>> getDiscoveryFeed(JwtAuthenticationToken auth) {
        Long userId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity.ok(userLocationService.getDiscoveryFeed(userId));
    }
}