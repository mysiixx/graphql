package ee.joeltek.match_me.profile;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;


import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfile;
import static ee.joeltek.match_me.support.ProfileBioFixtures.updateProfile;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeProfileControllerIntegrationTests extends IntegrationTestSupport {


    @Test
    void getMyProfileReturnsCurrentUsersProfile() throws Exception {
        RegisteredUser user = registerAndLogin();
        ProfileFixtureData expected = completeProfile(mockMvc, user.accessToken());

        mockMvc.perform(get("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value(expected.displayName()))
                .andExpect(jsonPath("$.firstName").value(expected.firstName()))
                .andExpect(jsonPath("$.lastName").value(expected.lastName()))
                .andExpect(jsonPath("$.city").value(expected.city()))
                .andExpect(jsonPath("$.birthDate").value(expected.birthDate()))
                .andExpect(jsonPath("$.aboutMe").value(expected.aboutMe()));
    }

    @Test
    void getMyProfileWithoutTokenReturns401() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeProfile(mockMvc, user.accessToken());

        mockMvc.perform(get("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void updateMyProfileFirstTimeReturnsUpdatedProfile() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeProfile(mockMvc, user.accessToken());
        Map<String, String> profileUpdateData = new HashMap<>();
        profileUpdateData.put("displayName", "John Doe");
        profileUpdateData.put("firstName", "John");
        profileUpdateData.put("lastName", "Doe");
        profileUpdateData.put("birthDate", "1999-04-12");
        profileUpdateData.put("city", "London");
        profileUpdateData.put("aboutMe", "Nothing to say.");

        updateProfile(mockMvc, user.accessToken(), profileUpdateData);

        mockMvc.perform(get("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value(profileUpdateData.get("displayName")))
                .andExpect(jsonPath("$.firstName").value(profileUpdateData.get("firstName")))
                .andExpect(jsonPath("$.lastName").value(profileUpdateData.get("lastName")))
                .andExpect(jsonPath("$.city").value(profileUpdateData.get("city")))
                .andExpect(jsonPath("$.birthDate").value(profileUpdateData.get("birthDate")))
                .andExpect(jsonPath("$.aboutMe").value(profileUpdateData.get("aboutMe")));
    }

    @Test
    void updateMyProfilePartialUpdateChangesOnlyProvidedFields() throws Exception {
        RegisteredUser user = registerAndLogin();
        Map<String, String> profileUpdateData = new HashMap<>();
        profileUpdateData.put("displayName", "John Doe");
        profileUpdateData.put("firstName", "John");
        profileUpdateData.put("lastName", "Doe");
        profileUpdateData.put("birthDate", "1999-04-12");
        profileUpdateData.put("city", "London");
        profileUpdateData.put("aboutMe", "Nothing to say.");

        updateProfile(mockMvc, user.accessToken(), profileUpdateData);

        Map<String, String> profileUpdatePartialData = new HashMap<>();
        profileUpdatePartialData.put("lastName", "Denver");
        profileUpdatePartialData.put("birthDate", "1943-12-31");

        updateProfile(mockMvc, user.accessToken(), profileUpdatePartialData);

        mockMvc.perform(get("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value(profileUpdateData.get("displayName")))
                .andExpect(jsonPath("$.firstName").value(profileUpdateData.get("firstName")))
                .andExpect(jsonPath("$.lastName").value(profileUpdatePartialData.get("lastName")))
                .andExpect(jsonPath("$.city").value(profileUpdateData.get("city")))
                .andExpect(jsonPath("$.birthDate").value(profileUpdatePartialData.get("birthDate")))
                .andExpect(jsonPath("$.aboutMe").value(profileUpdateData.get("aboutMe")));
    }

    @Test
    void updateMyProfileWithDuplicateDisplayNameReturns409() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        Map<String, String> profileUpdateData = new HashMap<>();
        profileUpdateData.put("displayName", "John Doe");
        profileUpdateData.put("firstName", "John");
        profileUpdateData.put("lastName", "Doe");
        profileUpdateData.put("birthDate", "1999-04-12");
        profileUpdateData.put("city", "London");
        profileUpdateData.put("aboutMe", "Nothing to say.");

        updateProfile(mockMvc, user1.accessToken(), profileUpdateData);

        RegisteredUser user2 = registerAndLogin();
        Map<String, String> profileUpdateData2 = new HashMap<>();
        profileUpdateData2.put("displayName", "John Doe");
        profileUpdateData2.put("firstName", "Clark");
        profileUpdateData2.put("lastName", "Kent");
        profileUpdateData2.put("birthDate", "1979-04-12");
        profileUpdateData2.put("city", "Krypton");
        profileUpdateData2.put("aboutMe", "Just flying around.");

        String profileData = """
                {
                  "aboutMe": "%s",
                  "birthDate": "%s",
                  "displayName": "%s",
                  "firstName": "%s",
                  "lastName": "%s",
                  "city": "%s"
                }
                """.formatted(profileUpdateData2.get("aboutMe"), profileUpdateData2.get("birthDate"), profileUpdateData2.get("displayName"), profileUpdateData2.get("firstName"), profileUpdateData2.get("lastName"), profileUpdateData2.get("city"));

        mockMvc.perform(patch("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user2))
                        .content(profileData))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void updateMyProfileWithInvalidBodyReturns400() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeProfile(mockMvc, user.accessToken());

        mockMvc.perform(patch("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user))
                        .content("{malformed JSON}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void updateProfilePictureStoresCustomPictureAndReturnsCreated() throws Exception {
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/avatar.jpg");
        final MockMultipartFile avatar = new MockMultipartFile("file", "avatar.jpg", "image/png", inputStream);

        String expectedFileName = user.id() + ".jpg";

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profilePictureUrl").value("/uploads/avatars/" + expectedFileName));
    }

    @Test
    void updateProfilePictureWithoutTokenReturns401() throws Exception {

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/avatar.jpg");
        final MockMultipartFile avatar = new MockMultipartFile("file", "avatar.jpg", "image/png", inputStream);

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", "")
                        .file(avatar))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void updateProfilePictureWithEmptyFileReturns400() throws Exception {
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("");
        final MockMultipartFile avatar = new MockMultipartFile("file", "avatar.jpg", "image/png", inputStream);

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());

    }

    @Test
    void updateProfilePictureWithUnsupportedMimeTypeReturns400() throws Exception {
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/unsupported.txt");
        final MockMultipartFile avatar = new MockMultipartFile("file", "unsupported.txt", "text/plain", inputStream);

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void updateProfilePictureWithInvalidImageContentReturns400() throws Exception{
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/invalid-image.png");
        final MockMultipartFile avatar = new MockMultipartFile("file", "invalid-image.png", "image/png", inputStream);

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void updateProfilePictureWithPathTraversalFilenameReturns400() throws Exception{
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/avatar.jpg");
        final MockMultipartFile avatar = new MockMultipartFile("file", "../avatar.jpg", "image/jpeg", inputStream);

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void deleteProfilePictureRemovesCustomPictureAndRestoresResolvedDefault() throws Exception{
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/avatar.jpg");
        final MockMultipartFile avatar = new MockMultipartFile("file", "avatar.jpg", "image/png", inputStream);

        String expectedFileName = user.id() + ".jpg";

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profilePictureUrl").value("/uploads/avatars/" + expectedFileName));

        mockMvc.perform(delete("/me/profile/picture")
                .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.profilePictureUrl").value("/avatars/DEFAULT.png"));
    }

    @Test
    void deleteProfilePictureWithoutTokenReturns401() throws Exception{
        RegisteredUser user = registerAndLogin();

        final InputStream inputStream = getClass().getResourceAsStream("/test-data/avatar.jpg");
        final MockMultipartFile avatar = new MockMultipartFile("file", "avatar.jpg", "image/png", inputStream);

        String expectedFileName = user.id() + ".jpg";

        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/picture")
                        .header("Authorization", authHeaderValue(user))
                        .file(avatar))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profilePictureUrl").value("/uploads/avatars/" + expectedFileName));

        mockMvc.perform(delete("/me/profile/picture")
                        .header("Authorization", ""))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void deleteProfilePictureWhenNoCustomPictureExistsReturns404() throws Exception{
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(delete("/me/profile/picture")
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }
}

