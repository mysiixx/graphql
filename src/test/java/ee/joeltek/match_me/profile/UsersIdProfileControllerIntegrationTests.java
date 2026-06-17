package ee.joeltek.match_me.profile;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static ee.joeltek.match_me.support.RecommendationFixtures.generateRecommendations;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsersIdProfileControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void getNonexistingUserProfileReturns404() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(get("/users/99999/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserProfileWithoutRecommendationSnapshotReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.displayName").doesNotExist())
                .andExpect(jsonPath("$.aboutMe").doesNotExist());
    }

    @Test
    void getUserProfileInRecommendationsListReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.city").exists())
                .andExpect(jsonPath("$.birthDate").exists())
                .andExpect(jsonPath("$.aboutMe").exists())
                .andExpect(jsonPath("$.archetype").exists());
    }

    @Test
    void getUserProfileWithPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        createPendingConnection(mockMvc, user1, user2);

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.aboutMe").exists());
    }

    @Test
    void getUserProfileWithIncomingPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user2);

        createPendingConnection(mockMvc, user2, user1);

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.aboutMe").exists());
    }

    @Test
    void getConnectedUserProfileReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        createPendingConnection(mockMvc, user1, user2);
        acceptConnection(mockMvc, user1.id(), user2);

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.aboutMe").exists());
    }

    @Test
    void getExistingButNotVisibleUserProfileReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        mockMvc.perform(get("/users/" + user2.id() + "/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.displayName").doesNotExist())
                .andExpect(jsonPath("$.aboutMe").doesNotExist());
    }
}
