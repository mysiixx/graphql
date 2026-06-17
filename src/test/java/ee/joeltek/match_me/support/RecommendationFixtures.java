package ee.joeltek.match_me.support;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class RecommendationFixtures extends IntegrationTestSupport {

    public static void generateRecommendations(MockMvc mockMvc, RegisteredUser user) throws Exception {
        mockMvc.perform(get("/recommendations")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk());
    }

    public static void dismissRecommendation(MockMvc mockMvc, RegisteredUser user, Long targetUserId) throws Exception {
        mockMvc.perform(post("/recommendations/" + targetUserId + "/dismiss")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isNoContent());
    }
}
