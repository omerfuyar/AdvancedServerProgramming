package com.dsu.hello_server;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

// Accounts and tokens. Like the other services it speaks in exceptions:
//   IllegalArgumentException -> 400, ConflictException -> 409,
//   BadCredentialsException / InvalidBearerTokenException -> 401 (ApiExceptionHandler)
@Service
public class AuthService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = 72; // BCrypt ignores everything after 72 bytes
    private static final String WRONG_CREDENTIALS = "wrong username or password";
    private static final String INVALID_TOKEN = "token is invalid or expired";

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final long ttlMinutes;
    private final long refreshTtlDays;
    private final SecureRandom random = new SecureRandom();

    public AuthService(AppUserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.ttl-minutes}") long ttlMinutes,
            @Value("${app.jwt.refresh-ttl-days}") long refreshTtlDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.ttlMinutes = ttlMinutes;
        this.refreshTtlDays = refreshTtlDays;
    }

    // Everyone who signs up is USER. admin comes from reset.sql only.
    @Transactional
    public AppUser signup(String username, String password) {
        checkSignup(username, password); // 400
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("username " + username + " is taken"); // 409
        }
        return userRepository.save(new AppUser(username, passwordEncoder.encode(password), "USER"));
    }

    // Unknown user and wrong password give the same message: nobody learns which usernames exist.
    @Transactional
    public TokenResponse login(String username, String password) {
        if (username == null || password == null) {
            throw new BadCredentialsException(WRONG_CREDENTIALS);
        }
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException(WRONG_CREDENTIALS));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException(WRONG_CREDENTIALS);
        }
        return issueTokens(user);
    }

    // Optional challenge: the refresh token is used once. It is deleted and a new pair comes back.
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        RefreshToken stored = findRefreshToken(refreshToken);
        if (stored.isExpired()) {
            throw new InvalidBearerTokenException(INVALID_TOKEN);
        }
        refreshTokenRepository.delete(stored);
        return issueTokens(stored.getUser());
    }

    // Logging out twice, or with an unknown token, still answers 204: the token is gone either way.
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByTokenHash(sha256(refreshToken))
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    private RefreshToken findRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidBearerTokenException(INVALID_TOKEN);
        }
        return refreshTokenRepository.findByTokenHash(sha256(refreshToken))
                .orElseThrow(() -> new InvalidBearerTokenException(INVALID_TOKEN));
    }

    private TokenResponse issueTokens(AppUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("room-api")
                .issuedAt(now)
                .expiresAt(now.plus(ttlMinutes, ChronoUnit.MINUTES))
                .subject(user.getUsername())
                .claim("role", user.getRole())
                .build();
        // Without the HS256 header the encoder looks for an RSA key and fails
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokenRepository.save(new RefreshToken(
                sha256(refreshToken), user, LocalDateTime.now().plusDays(refreshTtlDays)));

        return new TokenResponse(accessToken, "Bearer", ttlMinutes * 60, refreshToken);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkSignup(String username, String password) {
        List<String> problems = new ArrayList<>();
        if (username == null || !username.matches("[a-z0-9]{3,20}")) {
            problems.add("username must be 3 to 20 lowercase letters or digits");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            problems.add("password must be 8 to 72 characters");
        }
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
        }
    }
}
