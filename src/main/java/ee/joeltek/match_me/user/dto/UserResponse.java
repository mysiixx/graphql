package ee.joeltek.match_me.user.dto;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String displayName;
    private String profilePictureUrl;

    public UserResponse(Long id, String displayName, String profilePictureUrl) {
        this.id = id;
        this.displayName = displayName;
        this.profilePictureUrl = profilePictureUrl;
    }
}