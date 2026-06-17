package ee.joeltek.match_me.auth;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
