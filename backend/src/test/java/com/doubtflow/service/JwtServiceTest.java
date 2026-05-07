package com.doubtflow.service;

import com.doubtflow.model.Role;
import com.doubtflow.model.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesAndParsesTokenWithRenderStylePlainSecret() {
        JwtService jwtService = jwtServiceWithSecret("render-generated-secret-with-symbols-12345");
        UserPrincipal user = new UserPrincipal(7L, "Test Student", "student@example.com", Role.STUDENT);

        String token = jwtService.generateToken(user);
        UserPrincipal parsedUser = jwtService.parseUser(token);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(parsedUser.getId()).isEqualTo(user.getId());
        assertThat(parsedUser.getName()).isEqualTo(user.getName());
        assertThat(parsedUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(parsedUser.getRole()).isEqualTo(user.getRole());
    }

    @Test
    void stillSupportsBase64EncodedSecrets() {
        String base64Secret = Base64.getEncoder()
                .encodeToString("a-32-byte-jwt-signing-secret-value".getBytes(StandardCharsets.UTF_8));
        JwtService jwtService = jwtServiceWithSecret(base64Secret);
        UserPrincipal user = new UserPrincipal(12L, "Faculty Admin", "admin@example.com", Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.parseUser(token).getRole()).isEqualTo(Role.ADMIN);
    }

    private JwtService jwtServiceWithSecret(String secret) {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);
        return jwtService;
    }
}
