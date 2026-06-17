package ee.joeltek.match_me.support;

import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestHelper extends IntegrationTestSupport {

    public static RegisteredUser registerAndLogin(MockMvc mockMvc, String email, String password ) throws Exception {

        MvcResult registerResult = mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        Long userId = Long.valueOf(JsonPath.read(registerResult.getResponse().getContentAsString(), "$.id").toString());
        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        return new RegisteredUser(userId, email, password, accessToken);
    }

}
