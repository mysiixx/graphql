package ee.joeltek.match_me.support;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;

public final class ConnectionFixtures extends IntegrationTestSupport{

    public static void createPendingConnection(MockMvc mockMvc, RegisteredUser sender, RegisteredUser receiver) throws Exception {
        mockMvc.perform(post("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + sender.accessToken())
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(receiver.id())))
                .andExpect(status().isCreated());
    }

    public static void acceptConnection(MockMvc mockMvc, Long senderId, RegisteredUser receiver) throws Exception {
        mockMvc.perform(patch("/connections/" + senderId)
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + receiver.accessToken())
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isOk());
    }

}
