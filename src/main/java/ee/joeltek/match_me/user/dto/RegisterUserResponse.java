package ee.joeltek.match_me.user.dto;

import lombok.Data;


@Data
public class RegisterUserResponse {
    private Long id;
    private String email;

    public RegisterUserResponse(Long id, String email) {
        this.id = id;
        this.email = email;
    }
}
