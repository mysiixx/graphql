package ee.joeltek.match_me.recommendation;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.HIGH_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.LOW_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.MAXED_OUT_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfile;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RecommendationControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private DismissedRecommendationRepository dismissedRecommendationRepository;

    @Test
    void getRecommendationsWithoutCompletedProfileReturns403() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRecommendationsWithoutBioReturns403() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeProfile(mockMvc, user.accessToken());

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRecommendationsReturnsSortedMatchesAndPersistsSnapshot() throws Exception {
        RegisteredUser owner = registerAndLogin();
        RegisteredUser exactMatch = registerAndLogin();
        RegisteredUser closeMatch = registerAndLogin();
        RegisteredUser weakMatch = registerAndLogin();
        RegisteredUser otherCityUser = registerAndLogin();

        completeProfileAndBio(mockMvc, owner.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, exactMatch.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, closeMatch.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        completeProfileAndBio(mockMvc, weakMatch.accessToken(), "Tallinn", LOW_MATCH_ANSWERS);
        completeProfileAndBio(mockMvc, otherCityUser.accessToken(), "Tartu", MAXED_OUT_ANSWERS);

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(exactMatch.id()))
                .andExpect(jsonPath("$[1].id").value(closeMatch.id()))
                .andExpect(jsonPath("$[2].id").value(weakMatch.id()));

        List<Recommendation> persisted = recommendationRepository.findAllByOwnerUserIdOrderByRankOrderAsc(owner.id());

        org.junit.jupiter.api.Assertions.assertEquals(3, persisted.size());
        org.junit.jupiter.api.Assertions.assertEquals(exactMatch.id(), persisted.get(0).getRecommendedUserId());
        org.junit.jupiter.api.Assertions.assertEquals(closeMatch.id(), persisted.get(1).getRecommendedUserId());
        org.junit.jupiter.api.Assertions.assertEquals(weakMatch.id(), persisted.get(2).getRecommendedUserId());
    }

    @Test
    void getRecommendationsOnlyReturnsSameConnectionType() throws Exception {
        RegisteredUser owner = registerAndLogin();
        RegisteredUser sameConnectionType = registerAndLogin();
        RegisteredUser differentConnectionType = registerAndLogin();

        completeProfileAndBio(mockMvc, owner.accessToken(), "Tallinn", MAXED_OUT_ANSWERS, "FRIENDSHIP");
        completeProfileAndBio(mockMvc, sameConnectionType.accessToken(), "Tallinn", MAXED_OUT_ANSWERS, "FRIENDSHIP");
        completeProfileAndBio(mockMvc, differentConnectionType.accessToken(), "Tallinn", MAXED_OUT_ANSWERS, "WORK");

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(sameConnectionType.id()));
    }

    @Test
    void getRecommendationsReturnsMaximumTenResults() throws Exception {
        RegisteredUser owner = registerAndLogin();
        completeProfileAndBio(mockMvc, owner.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);

        for (int i = 0; i < 12; i++) {
            RegisteredUser candidate = registerAndLogin();
            completeProfileAndBio(mockMvc, candidate.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        }

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    void dismissRecommendationRemovesDismissedUserAndIsIdempotent() throws Exception {
        RegisteredUser owner = registerAndLogin();
        RegisteredUser firstCandidate = registerAndLogin();
        RegisteredUser secondCandidate = registerAndLogin();

        completeProfileAndBio(mockMvc, owner.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, firstCandidate.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, secondCandidate.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/recommendations/" + firstCandidate.id() + "/dismiss")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/recommendations/" + firstCandidate.id() + "/dismiss")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(secondCandidate.id()));

        org.junit.jupiter.api.Assertions.assertTrue(
                dismissedRecommendationRepository.existsByUserIdAndDismissedUserId(owner.id(), firstCandidate.id())
        );
        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                dismissedRecommendationRepository.findAllByUserId(owner.id()).size()
        );
    }

    @Test
    void getRecommendationsExcludesPendingAndAcceptedConnections() throws Exception {
        RegisteredUser owner = registerAndLogin();
        RegisteredUser pendingCandidate = registerAndLogin();
        RegisteredUser acceptedCandidate = registerAndLogin();
        RegisteredUser remainingCandidate = registerAndLogin();

        completeProfileAndBio(mockMvc, owner.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, pendingCandidate.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, acceptedCandidate.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        completeProfileAndBio(mockMvc, remainingCandidate.accessToken(), "Tallinn", LOW_MATCH_ANSWERS);

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        createPendingConnection(mockMvc, owner, pendingCandidate);

        createPendingConnection(mockMvc, owner, acceptedCandidate);
        acceptConnection(mockMvc, owner.id(), acceptedCandidate);

        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(remainingCandidate.id()));
    }
}
