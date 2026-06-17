package ee.joeltek.match_me.location;

import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.location.dto.UserLocationResponse;
import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserLocationService {
    private final UserLocationRepository userLocationRepository;
    private final UserRepository userRepository;

    public UserLocationResponse updateUserLocation(Long userId, UpdateUserLocationRequest request) {

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        PrecisionModel precisionModel = new PrecisionModel();
        GeometryFactory geometryFactory = new GeometryFactory(precisionModel, 4326);

        Point locationToSave = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        Optional<UserLocation> userLocationOptional = userLocationRepository.findById(userId);
        UserLocation userLocation = userLocationOptional.orElseGet(() -> new UserLocation(user));

        userLocation.setLocation(locationToSave);
        userLocation.setSource(request.getSource());
        userLocation.setPreferredRadiusMeters(request.getPreferredRadiusMeters());
        UserLocation updatedLocation =  userLocationRepository.save(userLocation);

        return mapToUserLocationResponseDto(updatedLocation);
    }

    public boolean isWithinPreferredRadius(Long requesterId, Long targetId) {
        // 1. Get the requester's location settings
        UserLocation requesterLocation = userLocationRepository.findById(requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requester location not set"));

        // 2. Ask the database for the distance in meters
        Double distanceInMeters = userLocationRepository.getDistanceBetweenUsers(requesterId, targetId);

        // 3. If it returns null, the target user hasn't set their location yet
        if (distanceInMeters == null) {
            return false;
        }

        // 4. Return true if they are within the chosen radius
        return distanceInMeters <= requesterLocation.getPreferredRadiusMeters();
    }

    private UserLocationResponse mapToUserLocationResponseDto (UserLocation userLocation) {
        UserLocationResponse response = new UserLocationResponse();
        response.setUserId(userLocation.getUserId());
        response.setLongitude(userLocation.getLocation().getX());
        response.setLatitude(userLocation.getLocation().getY());
        response.setPreferredRadiusMeters(userLocation.getPreferredRadiusMeters());
        response.setSource(userLocation.getSource());
        response.setUpdatedAt(userLocation.getUpdatedAt());
        return response;
    }

    public List<UserLocationResponse> getDiscoveryFeed(Long userId) {
        // 1. Ensure the user has actually set their location before they can search
        userLocationRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You must set your location to view the discovery feed"));

        // 2. Fetch the nearby user IDs from the database
        List<Long> nearbyUserIds = userLocationRepository.findMutualUserIdsWithinRadius(userId);

        // 3. Fetch the full UserLocation entities for those IDs
        List<UserLocation> nearbyLocations = userLocationRepository.findAllById(nearbyUserIds);

        // 4. Map the entities to the response DTOs
        return nearbyLocations.stream()
                .map(this::mapToUserLocationResponseDto)
                .toList();
    }

    public UserLocationResponse getUserLocation(Long userId) {
        UserLocation userLocation = userLocationRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Location not set for user"));

        return mapToUserLocationResponseDto(userLocation);
    }
}
