package ee.joeltek.match_me.connection.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class ConnectionResponse {
    // REST API - Other users id
    // GraphQL - connection id
    private Long id;
}