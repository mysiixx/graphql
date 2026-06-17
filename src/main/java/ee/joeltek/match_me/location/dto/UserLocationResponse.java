package ee.joeltek.match_me.location.dto;

import ee.joeltek.match_me.location.LocationSource;
import lombok.Data;

import java.time.Instant;

@Data
public class UserLocationResponse {
    private long userId;
    private double longitude;
    private double latitude;
    private int preferredRadiusMeters;
    private LocationSource source;
    private Instant updatedAt;
}
