package ee.joeltek.match_me.profile;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String displayName;
    private String firstName;
    private String lastName;
    //private String profilePictureUrl;
    private String city;
    private LocalDate birthDate;
    private String aboutMe;
    private ConnectionType connectionType;
    //private String archetype;
}
