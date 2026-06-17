package ee.joeltek.match_me.connection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;


@Data
public class ConnectionCreateRequest {
    @Positive
    @NotNull(message = "targetUserId is required")
    Long targetUserId;
}