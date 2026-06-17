package ee.joeltek.match_me.dev;

import ee.joeltek.match_me.bio.BioRepository;
import ee.joeltek.match_me.location.LocationSource;
import ee.joeltek.match_me.location.UserLocationRepository;
import ee.joeltek.match_me.location.UserLocationService;
import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.profile.UserProfileRepository;
import ee.joeltek.match_me.support.IntegrationTestSupport;
import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserRepository;
import ee.joeltek.match_me.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeederServiceIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private SeederService seederService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private BioRepository bioRepository;

    @Autowired
    private UserLocationRepository userLocationRepository;

    @Autowired
    private UserLocationService userLocationService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @Test
    void clearDataRemovesSeededDataResetsIdentityAndCleansUploads() throws Exception {
        UserEntity user = userService.createUser("seed-clear@test.com", "Password123!");
        userLocationService.updateUserLocation(
                user.getUserId(),
                new UpdateUserLocationRequest(59.4370, 24.7536, 10000, LocationSource.MANUAL)
        );

        Path uploadPath = Path.of(uploadDir);
        Files.createDirectories(uploadPath);
        Files.writeString(uploadPath.resolve("orphan-avatar.txt"), "temp");

        seederService.clearData();

        assertEquals(0L, userRepository.count());
        assertEquals(0L, userProfileRepository.count());
        assertEquals(0L, bioRepository.count());
        assertEquals(0L, userLocationRepository.count());
        assertTrue(Files.exists(uploadPath));
        try (Stream<Path> files = Files.list(uploadPath)) {
            assertEquals(0L, files.count());
        }

        UserEntity recreatedUser = userService.createUser("seed-clear-next@test.com", "Password123!");
        assertEquals(1L, recreatedUser.getUserId());
    }

    @Test
    void seed20UsersCreatesTwentyCompleteUsers() {
        seederService.seed20Users();

        assertEquals(20L, userRepository.count());
        assertEquals(20L, userProfileRepository.count());
        assertEquals(20L, bioRepository.count());
        assertEquals(20L, userLocationRepository.count());
    }

    @Test
    void seed200UsersCreatesTwoHundredCompleteUsers() {
        seederService.seed200Users();

        assertEquals(200L, userRepository.count());
        assertEquals(200L, userProfileRepository.count());
        assertEquals(200L, bioRepository.count());
        assertEquals(200L, userLocationRepository.count());
    }
}
