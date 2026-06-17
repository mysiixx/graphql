package ee.joeltek.match_me.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
    public static LoginResponse withoutRefreshToken(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, null, expiresIn);
    }

    public static LoginResponse withRefreshToken(String accessToken, String refreshToken, long expiresIn) {
        return new LoginResponse(accessToken, refreshToken, expiresIn);
    }
}
