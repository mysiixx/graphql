package ee.joeltek.match_me.profile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByDisplayNameIgnoreCase(String displayName);

    List<UserProfile> findAllByCity(String city);
}