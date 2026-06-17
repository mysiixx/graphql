package ee.joeltek.match_me.auth;


import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void registerNewUserSuccessful() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"raskeParool\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void registerWithExistingEmailFails() throws Exception {
        String email = uniqueEmail();
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"raskeParool\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"raskeParool\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void login() throws Exception {
        String email = uniqueEmail();
        String password = "raskeParool";
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.expiresIn").value(TokenService.ACCESS_TOKEN_EXPIRES_IN_SECONDS));
    }

    @Test
    void loginWithRememberMeReturnsRefreshToken() throws Exception {
        String email = uniqueEmail();
        String password = "raskeParool";
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"rememberMe\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(TokenService.ACCESS_TOKEN_EXPIRES_IN_SECONDS));
    }

    @Test
    void refreshTokenReturnsNewTokenPair() throws Exception {
        String email = uniqueEmail();
        String password = "raskeParool";
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"rememberMe\":true}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.refreshToken");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(TokenService.ACCESS_TOKEN_EXPIRES_IN_SECONDS));
    }

    @Test
    void reusedRefreshTokenReturns401() throws Exception {
        String email = uniqueEmail();
        String password = "raskeParool";
        mockMvc.perform(post("/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"rememberMe\":true}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.refreshToken");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithEmptyBodyReturns400() throws Exception {

        mockMvc.perform(post("/login")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void protectedEndpointIsUnaccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileAccessibleWithToken() throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk());
    }

    @Test
    void connectionsAccessibleWithToken() throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/connections")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk());
    }

    @Test
    void getMeWithInvalidTokenReturns401() throws Exception {
        mockMvc.perform(get("/me")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + "eyJhbGciOiJSUzI1NiJ1.eyJpc3MiOiJtYXRjaF9tZV9iYWNrZW5kIiwic3ViIjoiNiIsImV4cCI6MTc3MzUxNTc5NSwiaWF0IjoxNzczNTEyMTk1fQ.pMJEVKeoc-ZQQ6u0DBN5GmdJfXMyn9F2RfIC1i3okDKUVvry-l1VKbPmnJMcddwgqjobjT0FZnph6MBlILpf0lJLzERKDaz2avi2N_A7LT8X0AwfdcqhMrz9bwvm45s5a84gMdvI-HwMzbmTjclPxSP4arGFjFNSnz5W8SfrOEuY6zl0hMMmkXNyC6Hvl35LpuCPS50l1lP5FwRi3ZZGpAmqkeTCflKsXFIpooeZy9I4o1kehnH8ivkJobivJd3w7EvnxUvJGQQTa_u_ky7PQJr4uqWXbUCMAgcGOL-EgZ7ckd0gB_rPc9jH8nooPL4zAL1LQRFUQo-9YkcmBwmqHg"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

}
