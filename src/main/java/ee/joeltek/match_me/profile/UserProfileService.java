package ee.joeltek.match_me.profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.common.ResourceExistsException;
import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.location.UserLocationRepository;
import ee.joeltek.match_me.storage.StorageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final ProfileAccessService profileAccessService;
    private final UserProfileRepository userProfileRepository;
    private final ProfileImageResolver profileImageResolver;
    private final UserLocationRepository userLocationRepository;
    private final StorageService storageService;


    @Value("${app.upload-url-prefix}")
    String uploadUrlPrefix;

    public ProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));

        return mapToDto(profile);
    }

    public ProfileResponse getUserProfile(Long userId, Long requesterUserId) {
        if (!profileAccessService.canViewUser(requesterUserId, userId)) throw new ResourceNotFoundException("User not found");
        ProfileResponse profile = getProfile(userId);

        return profile;
    }

    public ProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));
        if (request.getDisplayName() != null) {
            if (userProfileRepository.existsByDisplayNameIgnoreCase(request.getDisplayName()) &&
                    !request.getDisplayName().equalsIgnoreCase(profile.getDisplayName()))
                throw new ResourceExistsException("Display name already exists");
            profile.setDisplayName(request.getDisplayName());
        }
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getBirthDate() != null) profile.setBirthDate(request.getBirthDate());
        if (request.getAboutMe() != null) profile.setAboutMe(request.getAboutMe());
        if (request.getConnectionType() != null) profile.setConnectionType(request.getConnectionType());

        UserProfile updatedProfile = userProfileRepository.save(profile);
        return mapToDto(updatedProfile);
    }

    private ProfileResponse mapToDto(UserProfile profile) {
        ProfileResponse dto = new ProfileResponse();
        dto.setId(profile.getUserId());
        dto.setDisplayName(profile.getDisplayName());
        dto.setFirstName(profile.getFirstName());
        dto.setLastName(profile.getLastName());
        dto.setProfilePictureUrl(profileImageResolver.resolveProfileImageUrl(profile));
        dto.setCity(profile.getCity());
        dto.setBirthDate(profile.getBirthDate());
        dto.setAboutMe(profile.getAboutMe());
        dto.setConnectionType(profile.getConnectionType());
        dto.setArchetype(profile.getArchetype());
        return dto;
    }

    public void updateArchetype(Long userId, String archetype) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));
        profile.setArchetype(archetype);
        userProfileRepository.save(profile);
    }

    public boolean isProfileComplete(UserProfile userProfile) {

        return userProfile.getAboutMe() != null &&
                !userProfile.getAboutMe().isBlank() &&
                userProfile.getArchetype() != null &&
                !userProfile.getArchetype().isBlank() &&
                userProfile.getBirthDate() != null &&
                userProfile.getDisplayName() != null &&
                !userProfile.getDisplayName().isBlank() &&
                userProfile.getFirstName() != null &&
                !userProfile.getFirstName().isBlank() &&
                userProfile.getLastName() != null &&
                !userProfile.getLastName().isBlank() &&
                userProfile.getCity() != null &&
                !userProfile.getCity().isBlank() &&
                userProfile.getConnectionType() != null &&
                userLocationRepository.existsById(userProfile.getUserId());
    }

    public List<Long> usersInSameCity(Long id) {
        UserProfile userProfile = userProfileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User profile not found for userId: " + id));
        String city = userProfile.getCity();
        if (city == null || city.isBlank())
            throw new ResourceNotFoundException("User city not found for userId: " + id);
        return userProfileRepository.findAllByCity(city).stream().map(UserProfile::getUserId).toList();
    }

    public ProfileResponse updateProfilePicture(Long userId, MultipartFile file) {
        if (file.isEmpty()) throw new BusinessRuleException("Uploaded file is empty.");
        List<String> allowedFileExtensions = List.of("jpg", "jpeg", "png");
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png");
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String contentType = file.getContentType();

        String fileExt = StringUtils.getFilenameExtension(file.getOriginalFilename());
        // validate file
        if (originalFileName.contains(".."))
            throw new BusinessRuleException("Filename contains invalid path sequence: " + originalFileName);
        if (fileExt == null || !allowedFileExtensions.contains(fileExt.toLowerCase()))
            throw new BusinessRuleException("Wrong file type. Allowed file extensions are: .jpg, .jpeg and .png");
        if (!allowedMimeTypes.contains(contentType))
            throw new BusinessRuleException("Wrong file type. Allowed file types are: image/jpeg and image/png");
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null)
                throw new BusinessRuleException("File is not an image.");
        } catch (IOException e) {
            throw new BusinessRuleException("Cant read file.");
        }

        String filename = userId + "." + fileExt;
        // store file
        storageService.store(file, filename);
        // update profile
        ProfileResponse response = updateProfilePictureUrl(userId, fileExt);

        return response;
    }

    public ProfileResponse deleteProfilePicture(Long userId) {
        //delete file from server
        String fileUrl = getCustomProfilePictureUrl(userId);
        if (fileUrl == null) throw new ResourceNotFoundException("Custom profile picture not set.");
        String fileExt = fileUrl.substring(fileUrl.indexOf("."));
        String filename = userId + fileExt;
        System.out.println(filename);
        storageService.delete(filename);

        //mark customProfilePictureUrl null and return updated profile
        ProfileResponse response = deleteProfilePictureUrl(userId);

        return response;
    }

    private ProfileResponse deleteProfilePictureUrl(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));
        profile.setCustomProfilePictureUrl(null);
        userProfileRepository.save(profile);
        return mapToDto(profile);
    }

    private String getCustomProfilePictureUrl(Long userId) {
         UserProfile profile = userProfileRepository.findById(userId)
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));
         return profile.getCustomProfilePictureUrl();
     }

     private ProfileResponse updateProfilePictureUrl(Long userId, String fileExt) {
         UserProfile profile = userProfileRepository.findById(userId)
                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found for user: " + userId));
         profile.setCustomProfilePictureUrl(uploadUrlPrefix + "/" + userId + "." + fileExt);
         userProfileRepository.save(profile);
         return mapToDto(profile);
     }
}
