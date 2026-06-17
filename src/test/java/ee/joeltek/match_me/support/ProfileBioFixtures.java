package ee.joeltek.match_me.support;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.springframework.test.web.servlet.MockMvc;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public final class ProfileBioFixtures extends IntegrationTestSupport {

    public static final List<Integer> MAXED_OUT_ANSWERS = List.of(
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10,
            10, 10, 10
    );

    public static final List<Integer> HIGH_MATCH_ANSWERS = List.of(
            9, 9, 9,
            9, 9, 9,
            9, 9, 9,
            9, 9, 9,
            9, 9, 9,
            9, 9, 9
    );

    public static final List<Integer> LOW_MATCH_ANSWERS = List.of(
            6, 6, 6,
            6, 6, 6,
            6, 6, 6,
            6, 6, 6,
            6, 6, 6,
            6, 6, 6
    );

    private static String randomDisplayName() {
    return "Name" + (1000 + (int) (Math.random() * 9000));
    }

    public static ProfileFixtureData completeProfile(MockMvc mockMvc, String token) throws Exception {
        return completeProfile(mockMvc, token, "Tallinn");
    }

    public static ProfileFixtureData completeProfile(MockMvc mockMvc, String token, String city) throws Exception {
        return completeProfile(mockMvc, token, city, "FRIENDSHIP");
    }

    public static ProfileFixtureData completeProfile(MockMvc mockMvc, String token, String city, String connectionType) throws Exception {
        String displayName = randomDisplayName();
        String firstName = "Mia";
        String lastName = "Stone";
        String birthDate = "1998-04-12";
        String aboutMe = "I enjoy live music and long walks.";

        String profileData = """
        {
          "aboutMe": "%s",
          "birthDate": "%s",
          "displayName": "%s",
          "firstName": "%s",
          "lastName": "%s",
          "city": "%s",
          "connectionType": "%s"
        }
        """.formatted(aboutMe, birthDate, displayName, firstName, lastName, city, connectionType);

        mockMvc.perform(patch("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                .content(profileData))
                .andExpect(status().isOk());
        return new ProfileFixtureData(displayName, firstName, lastName, city, birthDate, aboutMe, connectionType);
    }

    public static void completeBio(MockMvc mockMvc, String token) throws Exception {
        completeBio(mockMvc, token, List.of(
                3, 8, 5,
                8, 3, 7,
                9, 2, 6,
                4, 8, 2,
                5, 9, 3,
                7, 10, 4
        ));
    }

    public static void completeBio(MockMvc mockMvc, String token, List<Integer> answers) throws Exception {
        String bio = """
                {"answers": %s}
                """.formatted(answers.toString());
        mockMvc.perform(put("/me/bio")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(bio))
                .andExpect(status().isOk());
    }

    public static void completeLocation(MockMvc mockMvc, String token) throws Exception {
        completeLocation(mockMvc, token, "Tallinn");
    }

    public static void completeLocation(MockMvc mockMvc, String token, String city) throws Exception {
        double latitude = "Tartu".equals(city) ? 58.3776 : 59.4370;
        double longitude = "Tartu".equals(city) ? 26.7290 : 24.7536;

        mockMvc.perform(put("/me/location")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                  "latitude": %s,
                                  "longitude": %s,
                                  "preferredRadiusMeters": 10000,
                                  "source": "MANUAL"
                                }
                                """.formatted(latitude, longitude)))
                .andExpect(status().isOk());
    }

    public static void completeProfileAndBio(MockMvc mockMvc, String token) throws Exception{
        completeProfile(mockMvc, token);
        completeBio(mockMvc, token);
        completeLocation(mockMvc, token);
    }

    public static void completeProfileAndBio(MockMvc mockMvc, String token, String city, List<Integer> answers) throws Exception {
        completeProfileAndBio(mockMvc, token, city, answers, "FRIENDSHIP");
    }

    public static void completeProfileAndBio(MockMvc mockMvc, String token, String city, List<Integer> answers, String connectionType) throws Exception {
        completeProfile(mockMvc, token, city, connectionType);
        completeBio(mockMvc, token, answers);
        completeLocation(mockMvc, token, city);
    }

    public static void updateProfile(MockMvc mockMvc, String token, Map<String, String> profileData) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String profileJson = objectMapper.writeValueAsString(profileData);
        mockMvc.perform(patch("/me/profile")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(profileJson))
                .andExpect(status().isOk());
    }
}
