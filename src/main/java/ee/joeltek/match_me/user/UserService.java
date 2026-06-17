package ee.joeltek.match_me.user;

import ee.joeltek.match_me.bio.BioService;
import ee.joeltek.match_me.bio.UserBio;
import ee.joeltek.match_me.profile.*;

import ee.joeltek.match_me.user.dto.OnboardingStatusResponse;
import ee.joeltek.match_me.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageResolver profileImageResolver;
    private final DisplayNameGenerator displayNameGenerator;
    private final UserProfileService userProfileService;
    private final BioService bioService;

    @Transactional
    public UserEntity createUser(String email, String password) {

        Optional<UserEntity> findUser = userRepository.findByEmail(email);
        if (findUser.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists.");
        }

        String passwordHash = passwordEncoder.encode(password);
        UserEntity user = new UserEntity(email, passwordHash);

        UserProfile profile = new UserProfile();
        profile.setUser(user);

        profile.setDisplayName(displayNameGenerator.generateUniqueDisplayName());

        UserBio bio = new UserBio();

        user.setProfile(profile);
        user.setBio(bio);

        return userRepository.save(user);
    }

    public boolean isLoginCorrect(UserEntity user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    public UserResponse getById(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return new UserResponse(user.getUserId(), profile.getDisplayName(), profileImageResolver.resolveProfileImageUrl(profile));
    }

    public OnboardingStatusResponse getMyOnboardingStatus(Long userId) {

        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        boolean profileComplete = userProfileService.isProfileComplete(profile);
        boolean bioComplete = bioService.isBioComplete(userId);

        return new OnboardingStatusResponse(
                profileComplete,
                bioComplete
                );
    }
}
