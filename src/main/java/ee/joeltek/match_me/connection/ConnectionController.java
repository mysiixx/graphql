package ee.joeltek.match_me.connection;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.connection.dto.ConnectionCreateRequest;
import ee.joeltek.match_me.connection.dto.ConnectionResponse;
import ee.joeltek.match_me.connection.dto.ConnectionUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/connections")
@CrossOrigin
public class ConnectionController {
    private final ConnectionService connectionService;

    @GetMapping
    public ResponseEntity<List<ConnectionResponse>> getAccepted(JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity.ok(
            connectionService.getAllAccepted(currentUserId).stream()
                .map(e -> mapEntityToResponse(e, currentUserId))
                .toList()
        );
    }

    @GetMapping("/requests")
    public ResponseEntity<List<ConnectionResponse>> getIncomingRequests(JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity.ok(
            connectionService.getAllIncomingRequests(currentUserId).stream()
                .map(e -> mapEntityToResponse(e, currentUserId))
                .toList()
        );
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<ConnectionResponse>> getSentRequests(JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity.ok(
            connectionService.getAllSentRequests(currentUserId).stream()
                .map(e -> mapEntityToResponse(e, currentUserId))
                .toList()
        );
    }

    @PostMapping
    public ResponseEntity<ConnectionResponse> requestConnection(@Valid @RequestBody ConnectionCreateRequest dto, JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    mapEntityToResponse(
                       connectionService.requestConnection(currentUserId, dto.getTargetUserId()), currentUserId
                    )
                );
    }

    // Accept or reject pending request
    @PatchMapping("/{targetUserId}")
    public ResponseEntity<ConnectionResponse> updateConnection(
            @PathVariable("targetUserId") @Positive Long targetUserId,
            @Valid @RequestBody ConnectionUpdateRequest dto,
            JwtAuthenticationToken auth) {

        Long currentUserId = Long.valueOf(auth.getToken().getSubject());
        System.out.println(currentUserId);

        ConnectionResponse response = switch(dto.getStatus()) {
            case ACCEPTED -> mapEntityToResponse(
                connectionService.acceptConnection(targetUserId, currentUserId),
                currentUserId
            );
            case REJECTED -> mapEntityToResponse(
                connectionService.rejectConnection(targetUserId, currentUserId),
                currentUserId
            );
            default -> throw new BusinessRuleException(
                "Invalid status transition: " + dto.getStatus()
            );
        };

        return ResponseEntity.ok(response);
    }

    // Disconnect accepted connection
    @DeleteMapping("/{targetUserId}")
    public ResponseEntity<Void> disconnectConnection(@PathVariable("targetUserId") @Positive @NotNull Long targetUserId, JwtAuthenticationToken auth) {
        Long currentUserId = Long.valueOf(auth.getToken().getSubject());

        connectionService.disconnectConnection(targetUserId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // Get other user id and create response
    private ConnectionResponse mapEntityToResponse(ConnectionEntity entity, Long currentUserId) {
        Long otherUserId = entity.getSenderId().equals(currentUserId) ? entity.getReceiverId() : entity.getSenderId();
        
        return new ConnectionResponse(otherUserId);
    }
}