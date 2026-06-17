package ee.joeltek.match_me.bio;

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

public class UsersIdBioControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void getNonexistingUserBioReturns404() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(get("/users/99999/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserBioWithoutRecommendationSnapshotReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.visionaryScore").doesNotExist())
                .andExpect(jsonPath("$.executorScore").doesNotExist());
    }

    @Test
    void getUserBioInRecommendationsListReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionaryScore").exists())
                .andExpect(jsonPath("$.challengerScore").exists())
                .andExpect(jsonPath("$.architectScore").exists())
                .andExpect(jsonPath("$.harmonizerScore").exists())
                .andExpect(jsonPath("$.explorerScore").exists())
                .andExpect(jsonPath("$.executorScore").exists());
    }

    @Test
    void getUserBioWithPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        createPendingConnection(mockMvc, user1, user2);

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionaryScore").exists())
                .andExpect(jsonPath("$.executorScore").exists());
    }

    @Test
    void getUserBioWithIncomingPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user2);

        createPendingConnection(mockMvc, user2, user1);

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionaryScore").exists())
                .andExpect(jsonPath("$.executorScore").exists());
    }

    @Test
    void getConnectedUserBioReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        createPendingConnection(mockMvc, user1, user2);
        Long user1Id = user1.id();
        acceptConnection(mockMvc, user1Id, user2);

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionaryScore").exists())
                .andExpect(jsonPath("$.executorScore").exists());
    }

    @Test
    void getExistingButNotVisibleUserBioReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        mockMvc.perform(get("/users/" + user2.id() + "/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.visionaryScore").doesNotExist())
                .andExpect(jsonPath("$.executorScore").doesNotExist());
    }
}
