package ee.joeltek.match_me.user;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static ee.joeltek.match_me.support.RecommendationFixtures.generateRecommendations;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsersIdControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void getNonexistingUserReturns404() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(get("/users/99999")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserWithoutRecommendationSnapshotReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());
        //get user info
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.displayName").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }

    @Test
    void getUserInRecommendationsListReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        generateRecommendations(mockMvc, user1);

        //get user info
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());

    }

    @Test
    void getUserWithPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());
        generateRecommendations(mockMvc, user1);
        //request connection
        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1))
                        .content("""
                                {
                                "targetUserId": %d
                                }
                                """.formatted(user2.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty());
                
        //get User with pending connection
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());
    }

    @Test
    void getUserWithIncomingPendingConnectionReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());
        generateRecommendations(mockMvc, user2);
        //user 2 requests connection
        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user2))
                        .content("""
                                {
                                "targetUserId": %d
                                }
                                """.formatted(user1.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty());

        //get User with incoming pending connection
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());
    }

    @Test
    void getConnectedUserReturns200() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());
        generateRecommendations(mockMvc, user1);
        createPendingConnection(mockMvc, user1, user2);

        Long user1Id = user1.id();
        acceptConnection(mockMvc, user1Id, user2);

        //get connected User
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());
    }

    @Test
    void getExistingButNotVisibleUserReturns404() throws Exception {
        RegisteredUser user1 = registerAndLogin();
        RegisteredUser user2 = registerAndLogin();

        completeProfileAndBio(mockMvc, user1.accessToken());
        completeProfileAndBio(mockMvc, user2.accessToken());

        //get User
        mockMvc.perform(get("/users/" + user2.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.displayName").doesNotExist())
                .andExpect(jsonPath("$.profilePictureUrl").doesNotExist());
    }
}
