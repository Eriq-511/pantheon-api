package com.cms.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha";
    private static final long EXPIRY_MS = 3_600_000L;

    // ──────────────────────── generation ────────────────────────

    @Test
    public void generateToken_shouldReturnNonBlankToken() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(token).isNotBlank();
    }

    @Test
    public void generateToken_tokenShouldHaveThreeParts() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ──────────────────────── claim extraction ───────────────────

    @Test
    public void getUsernameFromToken_shouldReturnSubjectUsername() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("john", "ADMIN");
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo("john");
    }

    @Test
    public void getRoleFromToken_shouldReturnStoredRole() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("jane", "ADMIN");
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    public void getRoleFromToken_shouldReturnAdminForAnyUser() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("any-user", "ADMIN");
        assertThat(jwtUtil.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    // ──────────────────────── validation ─────────────────────────

    @Test
    public void validateToken_shouldReturnTrue_forValidToken() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    public void validateToken_shouldReturnFalse_forMalformedToken() {
        JwtUtil jwtUtil = newJwtUtil();
        assertThat(jwtUtil.validateToken("not.a.real.jwt")).isFalse();
    }

    @Test
    public void validateToken_shouldReturnFalse_forEmptyString() {
        JwtUtil jwtUtil = newJwtUtil();
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    public void validateToken_shouldReturnFalse_forTamperedSignature() {
        JwtUtil jwtUtil = newJwtUtil();
        String token = jwtUtil.generateToken("admin", "ADMIN");
        // replace the last 5 chars of the signature
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    public void validateToken_shouldReturnFalse_forExpiredToken() {
        JwtUtil jwtUtil = newJwtUtil();
        // negative expiry means the token is born already expired
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000L);
        String expired = jwtUtil.generateToken("admin", "ADMIN");
        assertThat(jwtUtil.validateToken(expired)).isFalse();
    }

    @Test
    public void validateToken_shouldReturnFalse_forTokenSignedWithDifferentKey() {
        JwtUtil jwtUtil = newJwtUtil();
        JwtUtil other = new JwtUtil();
        ReflectionTestUtils.setField(other, "jwtSecret",
                "completely-different-secret-key-256-bits-long-xxxxxxxxx");
        ReflectionTestUtils.setField(other, "jwtExpiration", EXPIRY_MS);
        String foreign = other.generateToken("admin", "ADMIN");
        assertThat(jwtUtil.validateToken(foreign)).isFalse();
    }

    // ──────────────────────── utility ────────────────────────────

    @Test
    public void getExpirationMs_shouldReturnConfiguredValue() {
        JwtUtil jwtUtil = newJwtUtil();
        assertThat(jwtUtil.getExpirationMs()).isEqualTo(EXPIRY_MS);
    }

    private JwtUtil newJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", EXPIRY_MS);
        return jwtUtil;
    }
}
