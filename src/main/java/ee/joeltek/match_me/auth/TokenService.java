package ee.joeltek.match_me.auth;

import ee.joeltek.match_me.user.UserEntity;
import ee.joeltek.match_me.user.UserRepository;
import ee.joeltek.match_me.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

// If use @ReguiredArgsConstructor from lombok, then constructor will be generated automatically
// but for now for clarity constructor remains in code.
@Service
public class TokenService {
    public static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 3600L;
    private static final long REMEMBER_ME_REFRESH_TOKEN_EXPIRES_IN_SECONDS = 30L * 24L * 60L * 60L;

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public TokenService(
            UserRepository userRepository,
            UserService userService,
            JwtEncoder jwtEncoder,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResponse login(String email, String password, boolean rememberMe) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!userService.isLoginCorrect(user, password)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        String accessToken = createAccessToken(user);

        if (!rememberMe) {
            return LoginResponse.withoutRefreshToken(accessToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
        }

        String refreshToken = createRefreshToken(user);
        return LoginResponse.withRefreshToken(accessToken, refreshToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    @Transactional
    public RefreshTokenResponse refresh(String rawRefreshToken) {
        Instant now = Instant.now();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (!refreshToken.isActive(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        refreshToken.setRevokedAt(now);
        String newAccessToken = createAccessToken(refreshToken.getUser());
        String newRefreshToken = createRefreshToken(refreshToken.getUser());

        return new RefreshTokenResponse(newAccessToken, newRefreshToken, ACCESS_TOKEN_EXPIRES_IN_SECONDS);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .ifPresent(refreshToken -> refreshToken.setRevokedAt(Instant.now()));
    }

    private String createAccessToken(UserEntity user) {
        Instant now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer("match_me_backend")
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ACCESS_TOKEN_EXPIRES_IN_SECONDS))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String createRefreshToken(UserEntity user) {
        byte[] tokenBytes = new byte[64];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(Instant.now().plusSeconds(REMEMBER_ME_REFRESH_TOKEN_EXPIRES_IN_SECONDS));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

}
