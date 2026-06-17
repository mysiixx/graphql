package ee.joeltek.match_me.dev;

import ee.joeltek.match_me.bio.Archetype;
import ee.joeltek.match_me.bio.BioRepository;
import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.bio.UserBio;
import ee.joeltek.match_me.location.LocationSource;
import ee.joeltek.match_me.location.UserLocationService;
import ee.joeltek.match_me.location.dto.UpdateUserLocationRequest;
import ee.joeltek.match_me.profile.ConnectionType;
import ee.joeltek.match_me.profile.UpdateProfileRequest;
import ee.joeltek.match_me.profile.UserProfileService;
import ee.joeltek.match_me.storage.FileSystemStorageService;
import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserRepository;
import ee.joeltek.match_me.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Random;

import static ee.joeltek.match_me.dev.DemoUsers.DEMO_USERS;

@RequiredArgsConstructor
@Service
public class SeederService {
    private final BioRepository bioRepository;
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final BioService bioService;
    private final UserLocationService userLocationService;
    private final JdbcTemplate jdbcTemplate;
    private final FileSystemStorageService fileSystemStorageService;
    private final NameGenerator nameGenerator;
    private final UserRepository userRepository;
    private final LocationGenerator locationGenerator;
    private final BioAnswersGenerator bioAnswersGenerator;

    private static final Random random = new Random(42);

    @Transactional
    public void clearData() {
        //clear database
        jdbcTemplate.execute("""
                TRUNCATE
                dismissed_recommendations,
                recommendations,
                messages,
                chats,
                connections,
                user_locations,
                bios,
                profiles,
                users
                RESTART IDENTITY CASCADE;
                """);
        //empty avatars folder
        fileSystemStorageService.deleteAllStoredAvatars();
    }

    @Transactional
    public void seed20Users() {
        clearData();
        for (DemoUsers.DemoUser user : DEMO_USERS) {
            //register user
            UserEntity registeredUser = userService.createUser(user.email(), user.password());
            Long userId = registeredUser.getUserId();

            //fill profile
            UpdateProfileRequest profileRequest = new UpdateProfileRequest();
            profileRequest.setFirstName(user.firstName());
            profileRequest.setLastName(user.lastName());
            profileRequest.setCity(user.city());
            profileRequest.setBirthDate(LocalDate.parse(user.birthDate()));
            profileRequest.setConnectionType(randomConnectionType());
            profileRequest.setAboutMe("""
                    I’m either planning something big, fixing something small, or thinking about both at the same time.
                    I appreciate good conversations, unexpected ideas, and people who know when to challenge and when to just enjoy the moment.
                    If we end up talking about something completely random at 2am, that’s usually a good sign.
                    """);
            userProfileService.updateProfile(userId, profileRequest);

            //answer bio questions
            UserBio bio = bioRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));
            Archetype winningArchetype = bioService.updateBioScores(bio, user.bioAnswers());
            bioRepository.save(bio);
            userProfileService.updateArchetype(userId, winningArchetype.name());

            //set geolocation
            UpdateUserLocationRequest locationRequest = new UpdateUserLocationRequest(user.latitude(), user.longitude(), user.preferred_radius_meters(), LocationSource.MANUAL);
            userLocationService.updateUserLocation(userId, locationRequest);
        }
    }

    @Transactional
    public void seed200Users() {
        clearData();
        for (int i = 0; i < 200; i++) {
            //register user
            String firstName = nameGenerator.getRandomFirstName();
            String lastName = nameGenerator.getRandomLastName();
            String email;
            do {
                int number = 100 + (int) (Math.random() * 899);
                email = firstName.toLowerCase() + "." + lastName.toLowerCase() + number + "@example.com";
            } while (userRepository.existsByEmail(email));

            UserEntity registeredUser = userService.createUser(email, "Password123!");
            Long userId = registeredUser.getUserId();

            //fill profile
            UpdateProfileRequest profileRequest = new UpdateProfileRequest();
            profileRequest.setFirstName(firstName);
            profileRequest.setLastName(lastName);

            LocationGenerator.Location userLocation = locationGenerator.getRandomLocation();
            profileRequest.setCity(userLocation.name());

            profileRequest.setBirthDate(LocalDate.parse(BirthdateGenerator.generate()));
            profileRequest.setConnectionType(randomConnectionType());
            profileRequest.setAboutMe("""
                    I’m either planning something big, fixing something small, or thinking about both at the same time.
                    I appreciate good conversations, unexpected ideas, and people who know when to challenge and when to just enjoy the moment.
                    If we end up talking about something completely random at 2am, that’s usually a good sign.
                    """);
            userProfileService.updateProfile(userId, profileRequest);

            //answer bio questions
            UserBio bio = bioRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bio not found for user: " + userId));
            Archetype winningArchetype = bioService.updateBioScores(bio, bioAnswersGenerator.getRandomBioAnswers());
            bioRepository.save(bio);
            userProfileService.updateArchetype(userId, winningArchetype.name());

            //set geolocation
            int min = 10000; //10 km
            int max = 200000; //200 km
            int preferred_radius_meters = random.nextInt(max - min + 1) + min;

            UpdateUserLocationRequest locationRequest = new UpdateUserLocationRequest(userLocation.latitude(), userLocation.longitude(), preferred_radius_meters, LocationSource.MANUAL);
            userLocationService.updateUserLocation(userId, locationRequest);
        }
    }

    private ConnectionType randomConnectionType() {
        ConnectionType[] values = ConnectionType.values();
        return values[random.nextInt(values.length)];
    }
}
