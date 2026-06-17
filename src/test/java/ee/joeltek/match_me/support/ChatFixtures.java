package ee.joeltek.match_me.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.HIGH_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.MAXED_OUT_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static ee.joeltek.match_me.support.RecommendationFixtures.generateRecommendations;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ChatFixtures extends IntegrationTestSupport {

    public static Long createChat(MockMvc mockMvc, RegisteredUser initiator, RegisteredUser target) throws Exception {
        MvcResult result = mockMvc.perform(post("/chats")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + initiator.accessToken())
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(target.id())))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }

    public static Long createAcceptedConnectionAndChat(MockMvc mockMvc, RegisteredUser initiator, RegisteredUser target) throws Exception {
        completeProfileAndBio(mockMvc, initiator.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, target.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, initiator);
        createPendingConnection(mockMvc, initiator, target);

        Long senderId = initiator.id();
        acceptConnection(mockMvc, senderId, target);

        return createChat(mockMvc, initiator, target);
    }

    public static Long sendMessage(MockMvc mockMvc, RegisteredUser sender, Long chatId, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/chats/" + chatId + "/messages")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + sender.accessToken())
                        .content("""
                                {
                                  "content": "%s"
                                }
                                """.formatted(content)))
                .andExpect(status().isCreated())
                .andReturn();

        return Long.valueOf(JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString());
    }
}
