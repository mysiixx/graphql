package ee.joeltek.match_me.connection.dto;

import ee.joeltek.match_me.connection.ConnectionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectionUpdateRequest {
    @NotNull(message = "status is required")
    ConnectionStatus status;
}
