package ee.joeltek.match_me.bio;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static ee.joeltek.match_me.support.ProfileBioFixtures.MAXED_OUT_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeBio;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeBioControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void getMyBioReturnsCurrentUsersBio() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeBio(mockMvc, user.accessToken(), MAXED_OUT_ANSWERS);

        mockMvc.perform(get("/me/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.visionaryScore").value(30))
                .andExpect(jsonPath("$.challengerScore").value(30))
                .andExpect(jsonPath("$.architectScore").value(30))
                .andExpect(jsonPath("$.harmonizerScore").value(30))
                .andExpect(jsonPath("$.explorerScore").value(30))
                .andExpect(jsonPath("$.executorScore").value(30));
    }

    @Test
    void getMyBioWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/me/bio")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMyBioReturnsWinningArchetype() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(put("/me/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user))
                        .content("""
                                {
                                  "answers": [10,10,10, 1,1,1, 1,1,1, 1,1,1, 1,1,1, 1,1,1]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bio processed successfully!"))
                .andExpect(jsonPath("$.archetype").value("VISIONARY"));

        mockMvc.perform(get("/me/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.visionaryScore").value(30))
                .andExpect(jsonPath("$.executorScore").value(3))
                .andExpect(jsonPath("$.explorerScore").value(3))
                .andExpect(jsonPath("$.architectScore").value(3))
                .andExpect(jsonPath("$.harmonizerScore").value(3))
                .andExpect(jsonPath("$.challengerScore").value(3));
    }

    @Test
    void updateMyBioWithInvalidAnswerCountReturns400() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(put("/me/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user))
                        .content("""
                                {
                                  "answers": [1,2,3]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
