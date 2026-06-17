package ee.joeltek.match_me.user;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MeControllerIntegrationTests extends IntegrationTestSupport {

    @Test
    void getMeReturnsSafeUserSummary () throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/me")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profileComplete").doesNotExist())
                .andExpect(jsonPath("$.bioComplete").doesNotExist());
    }

    @Test
    void getMeDoesNotExposeSensitiveFields () throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/me")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void getMyOnboardingStatusReturnsIncompleteForNewUser() throws Exception {
        RegisteredUser user = registerAndLogin();

        mockMvc.perform(get("/me/onboarding-status")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileComplete").value(false))
                .andExpect(jsonPath("$.bioComplete").value(false));
    }

    @Test
    void getMyOnboardingStatusReturnsCompleteAfterRequiredOnboardingIsCompleted() throws Exception {
        RegisteredUser user = registerAndLogin();
        completeProfileAndBio(mockMvc, user.accessToken());

        mockMvc.perform(get("/me/onboarding-status")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileComplete").value(true))
                .andExpect(jsonPath("$.bioComplete").value(true));
    }

    @Test
    void getMyOnboardingStatusWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/me/onboarding-status")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void getMeWithoutTokenReturns401 () throws Exception {

        mockMvc.perform(get("/me")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void getNotExistingUserProfileReturns404() throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/users/999999/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void getUserByIdReturnsSafeUserSummary() throws Exception {
        RegisteredUser user = registerAndLogin();
        mockMvc.perform(get("/users/" + user.id())
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id()))
                .andExpect(jsonPath("$.displayName").exists())
                .andExpect(jsonPath("$.displayName").isNotEmpty())
                .andExpect(jsonPath("$.profilePictureUrl").hasJsonPath())
                .andExpect(jsonPath("$.profileComplete").doesNotExist())
                .andExpect(jsonPath("$.bioComplete").doesNotExist())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

}
