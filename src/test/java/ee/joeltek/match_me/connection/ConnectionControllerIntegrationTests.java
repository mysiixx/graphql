package ee.joeltek.match_me.connection;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.HIGH_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.MAXED_OUT_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static ee.joeltek.match_me.support.RecommendationFixtures.generateRecommendations;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ConnectionControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private ConnectionRepository connectionRepository;

    @Test
    void requestConnectionCreatesPendingConnectionAndIncomingRequest() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);

        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(receiver.id())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/connections/requests")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void requestConnectionWithoutRecommendationReturns403() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);

        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(receiver.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestConnectionToSelfReturns400() throws Exception {
        RegisteredUser user = registerAndLogin();

        completeProfileAndBio(mockMvc, user.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        generateRecommendations(mockMvc, user);

        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(user.id())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void requestConnectionWithExistingRequestReturns409() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);

        createPendingConnection(mockMvc, sender, receiver);

        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(sender.id())))
                .andExpect(status().isConflict());
    }

    @Test
    void acceptConnectionByReceiverMakesItVisibleInAcceptedList() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);
		createPendingConnection(mockMvc, sender, receiver);

        Long receiverId = receiver.id();
		Long senderId = sender.id();

        mockMvc.perform(patch("/connections/" + senderId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver))
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(senderId));

        mockMvc.perform(get("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(receiverId));

        mockMvc.perform(get("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(senderId));
    }

    @Test
    void acceptConnectionByNonReceiverReturns403() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);
		createPendingConnection(mockMvc, sender, receiver);

        Long receiverId = receiver.id();

        mockMvc.perform(patch("/connections/" + receiverId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender))
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectConnectionRemovesItFromPendingRequests() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);
		createPendingConnection(mockMvc, sender, receiver);

        Long senderId = sender.id();

        mockMvc.perform(patch("/connections/" + senderId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver))
                        .content("""
                                {
                                  "status": "REJECTED"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/connections/requests")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void disconnectAcceptedConnectionReturns204AndRemovesItFromAcceptedList() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);
		createPendingConnection(mockMvc, sender, receiver);

        Long receiverId = receiver.id();
		Long senderId = sender.id();
        acceptConnection(mockMvc, senderId, receiver);

        mockMvc.perform(delete("/connections/" + receiverId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        org.junit.jupiter.api.Assertions.assertEquals(
                ConnectionStatus.REJECTED,
                connectionRepository.findBySenderIdAndReceiverId(senderId, receiverId).orElseThrow().getStatus()
        );
    }

    @Test
    void disconnectPendingConnectionReturns400() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        completeProfileAndBio(mockMvc, sender.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, receiver.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, sender);
		createPendingConnection(mockMvc, sender, receiver);

        Long receiverId = receiver.id();
		Long senderId = sender.id();

        mockMvc.perform(delete("/connections/" + receiverId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(sender)))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(
                ConnectionStatus.REJECTED,
                connectionRepository.findBySenderIdAndReceiverId(senderId, receiverId).orElseThrow().getStatus()
        );
    }
}