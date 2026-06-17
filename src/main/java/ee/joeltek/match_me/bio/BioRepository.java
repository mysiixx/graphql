package ee.joeltek.match_me.bio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BioRepository extends JpaRepository<UserBio, Long> {
    List<UserBio> findAllByUserIdNot(Long userId);
}