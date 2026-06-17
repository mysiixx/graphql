package ee.joeltek.match_me.websocket;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @GetMapping("/chats/presence")
    public ResponseEntity<Map<Long, Boolean>> getMyChatsUsersPresence(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        return ResponseEntity.ok(presenceService.getUserChatsPresence(userId));    }
}
