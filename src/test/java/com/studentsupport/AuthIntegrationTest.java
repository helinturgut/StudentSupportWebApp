package com.studentsupport;

import com.studentsupport.dto.AuthResponse;
import com.studentsupport.dto.LoginRequest;
import com.studentsupport.dto.RefreshTokenRequest;
import com.studentsupport.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end HTTP integration tests exercising the real Spring Security filter
 * chain, controllers, services, and an isolated in-memory H2 database.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String uniqueEmail(String prefix) {
        return prefix + "+" + System.nanoTime() + "@example.com";
    }

    private AuthResponse registerNewUser(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Integration Test User");
        request.setEmail(email);
        request.setPassword("Password123");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    @Test
    void register_thenAccessProtectedEndpoint_succeeds() {
        AuthResponse auth = registerNewUser(uniqueEmail("register-flow"));
        assertThat(auth).isNotNull();
        assertThat(auth.getToken()).isNotBlank();
        assertThat(auth.getRefreshToken()).isNotBlank();
        assertThat(auth.getRole()).isEqualTo("STUDENT");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getToken());
        ResponseEntity<String> dashboard = restTemplate.exchange(
                "/api/students/dashboard", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(dashboard.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void accessingProtectedEndpoint_withoutToken_isRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/students/dashboard", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void accessingProtectedEndpoint_withGarbageToken_isRejected() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("not-a-real-jwt");
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/students/dashboard", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registeringWithDuplicateEmail_isRejected() {
        String email = uniqueEmail("duplicate");
        registerNewUser(email);

        RegisterRequest secondAttempt = new RegisterRequest();
        secondAttempt.setFullName("Someone Else");
        secondAttempt.setEmail(email);
        secondAttempt.setPassword("Password123");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", secondAttempt, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_withWrongPassword_isRejected() {
        String email = uniqueEmail("wrong-password");
        registerNewUser(email);

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("TotallyWrongPassword");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", login, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void login_withCorrectPassword_returnsFreshTokens() {
        String email = uniqueEmail("correct-password");
        registerNewUser(email);

        LoginRequest login = new LoginRequest();
        login.setEmail(email);
        login.setPassword("Password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/login", login, AuthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void refreshToken_rotatesAndRejectsReuse() {
        AuthResponse auth = registerNewUser(uniqueEmail("refresh-flow"));

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(auth.getRefreshToken());

        ResponseEntity<AuthResponse> refreshResponse =
                restTemplate.postForEntity("/api/auth/refresh", refreshRequest, AuthResponse.class);

        assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse refreshed = refreshResponse.getBody();
        assertThat(refreshed.getToken()).isNotBlank();
        // The refresh token must always rotate — this is the real security property.
        // (The JWT access token itself may legitimately be byte-identical if issued
        // within the same second, since it carries no random/nonce claim.)
        assertThat(refreshed.getRefreshToken()).isNotEqualTo(auth.getRefreshToken());

        // Reusing the original (now-rotated) refresh token must be rejected.
        ResponseEntity<String> reuseResponse =
                restTemplate.postForEntity("/api/auth/refresh", refreshRequest, String.class);
        assertThat(reuseResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_revokesRefreshToken() {
        AuthResponse auth = registerNewUser(uniqueEmail("logout-flow"));

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(auth.getRefreshToken());

        ResponseEntity<Void> logoutResponse = restTemplate.postForEntity("/api/auth/logout", refreshRequest, Void.class);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> refreshAfterLogout =
                restTemplate.postForEntity("/api/auth/refresh", refreshRequest, String.class);
        assertThat(refreshAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void forgotPassword_returnsOkRegardlessOfWhetherEmailExists() {
        ResponseEntity<Void> existingEmailResponse = restTemplate.postForEntity(
                "/api/auth/forgot-password", java.util.Map.of("email", registerNewUser(uniqueEmail("forgot")).getEmail()),
                Void.class);
        assertThat(existingEmailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Void> unknownEmailResponse = restTemplate.postForEntity(
                "/api/auth/forgot-password", java.util.Map.of("email", "nobody-" + System.nanoTime() + "@example.com"),
                Void.class);
        assertThat(unknownEmailResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void resetPassword_withInvalidToken_isRejected() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/auth/reset-password",
                java.util.Map.of("token", "not-a-real-token", "newPassword", "NewPassword123"),
                String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
