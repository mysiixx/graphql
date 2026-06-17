package ee.joeltek.match_me.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {

    // Calculates the distance between two users in meters
    @Query(value = "SELECT ST_Distance(u1.location, u2.location) " +
            "FROM user_locations u1, user_locations u2 " +
            "WHERE u1.user_id = :user1Id AND u2.user_id = :user2Id",
            nativeQuery = true)
    Double getDistanceBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Finds ALL user IDs where BOTH users are within each other's preferred radius
    @Query(value = "SELECT u2.user_id " +
            "FROM user_locations u1, user_locations u2 " +
            "WHERE u1.user_id = :requesterId " +
            "  AND u2.user_id != :requesterId " +
            "  AND ST_Distance(u1.location, u2.location) <= u1.preferred_radius_meters " +
            "  AND ST_Distance(u1.location, u2.location) <= u2.preferred_radius_meters",
            nativeQuery = true)
    List<Long> findMutualUserIdsWithinRadius(@Param("requesterId") Long requesterId);
}