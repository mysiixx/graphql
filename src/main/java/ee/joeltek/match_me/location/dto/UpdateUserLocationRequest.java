package ee.joeltek.match_me.location.dto;

import ee.joeltek.match_me.location.LocationSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserLocationRequest {
    @NotNull(message = "Latitude is required")
    @Min(-90) @Max(90)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(-180) @Max(180)
    private Double longitude;

    @NotNull(message = "Preferred radius is required")
    @Min(10000) @Max(20000000)
    private Integer preferredRadiusMeters;

    @NotNull(message = "Source is required")
    private LocationSource source;

}