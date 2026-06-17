package ee.joeltek.match_me.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = MOCK)
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    public record RegisteredUser(
            Long id,
            String email,
            String password,
            String accessToken
    ) {}

    public record ProfileFixtureData(
            String displayName,
            String firstName,
            String lastName,
            String city,
            String birthDate,
            String aboutMe,
            String connectionType
    ) {}

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@email.com";
    }

    protected String uniquePassword() {
        return UUID.randomUUID().toString();
    }

    @BeforeEach
    protected void clearDatabase(@Autowired JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "messages");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "chats");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "dismissed_recommendations");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "recommendations");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "connections");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "bios");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "profiles");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "user_locations");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "refresh_tokens");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "users");

    }

    protected RegisteredUser registerAndLogin() throws Exception {
        return AuthTestHelper.registerAndLogin(mockMvc, uniqueEmail(), uniquePassword());
    }

    protected String authHeaderValue (RegisteredUser user) {
        return "Bearer " + user.accessToken();
    }

}
