package ee.joeltek.match_me.profile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ee.joeltek.match_me.common.BusinessRuleException;
import ee.joeltek.match_me.common.ResourceNotFoundException;
import ee.joeltek.match_me.storage.StorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ProfileAccessService profileAccessService;
    private final StorageService storageService;


    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getMyProfile(JwtAuthenticationToken authentication) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse profile = userProfileService.getProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(JwtAuthenticationToken authentication, @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        ProfileResponse updatedProfile = userProfileService.updateProfile(userId, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @GetMapping("/users/{id}/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable Long id, JwtAuthenticationToken authentication) {
        Long requesterUserId = Long.valueOf(authentication.getToken().getSubject());

        if (profileAccessService.canViewUser(requesterUserId, id)) {
            ProfileResponse profile = userProfileService.getProfile(id);
            return ResponseEntity.ok(profile);
        } else throw new ResourceNotFoundException("User not found");
    }

    @PutMapping("/me/profile/picture")
    public ResponseEntity<ProfileResponse> updateProfilePicture(@RequestParam("file") MultipartFile file, JwtAuthenticationToken authentication) {
        if (file.isEmpty()) throw new BusinessRuleException("Uploaded file is empty.");
        List<String> allowedFileExtensions = List.of("jpg", "jpeg", "png");
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png");
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String contentType = file.getContentType();
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        String fileExt = StringUtils.getFilenameExtension(file.getOriginalFilename());
        //validate file
        if (originalFileName.contains(".."))
            throw new BusinessRuleException("Filename contains invalid path sequence: " + originalFileName);
        if (fileExt == null || !allowedFileExtensions.contains(fileExt.toLowerCase()))
            throw new BusinessRuleException("Wrong file type. Allowed file extensions are: .jpg, .jpeg and .png");
        if (!allowedMimeTypes.contains(contentType))
            throw new BusinessRuleException("Wrong file type. Allowed file types are: image/jpeg and image/png");
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) throw new BusinessRuleException("File is not an image.");
        } catch (IOException e) {
            throw new BusinessRuleException("Cant read file.");
        }

        String filename = userId + "." + fileExt;
        //store file
        storageService.store(file, filename);
        //update profile
        ProfileResponse response = userProfileService.updateProfilePictureUrl(userId, fileExt);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @DeleteMapping("/me/profile/picture")
    public ResponseEntity<ProfileResponse> deleteProfilePicture (JwtAuthenticationToken authentication){
        Long userId = Long.valueOf(authentication.getToken().getSubject());

        //delete file from server
        String fileUrl = userProfileService.getCustomProfilePictureUrl(userId);
        if (fileUrl == null) throw new ResourceNotFoundException("Custom profile picture not set.");
        String fileExt = fileUrl.substring(fileUrl.indexOf("."));
        String filename = userId + fileExt;
        System.out.println(filename);
        storageService.delete(filename);

        //mark customProfilePictureUrl null and return updated profile
        ProfileResponse response = userProfileService.deleteProfilePictureUrl(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}