package ee.joeltek.match_me.user.dto;

import lombok.Data;

@Data
public class OnboardingStatusResponse {
    private boolean profileComplete;
    private boolean bioComplete;

    public OnboardingStatusResponse(
            boolean profileComplete,
            boolean bioComplete
    ) {
        this.profileComplete = profileComplete;
        this.bioComplete = bioComplete;
    }
}