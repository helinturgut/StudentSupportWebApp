package com.studentsupport.service;

import com.studentsupport.dto.AuthResponse;
import com.studentsupport.dto.LoginRequest;
import com.studentsupport.dto.RegisterRequest;
import com.studentsupport.entity.PasswordResetToken;
import com.studentsupport.entity.RefreshToken;
import com.studentsupport.entity.Role;
import com.studentsupport.entity.RoleName;
import com.studentsupport.entity.User;
import com.studentsupport.entity.UserStatus;
import com.studentsupport.exception.BadRequestException;
import com.studentsupport.exception.UnauthorizedException;
import com.studentsupport.repository.PasswordResetTokenRepository;
import com.studentsupport.repository.RefreshTokenRepository;
import com.studentsupport.repository.RoleRepository;
import com.studentsupport.repository.StudentProfileRepository;
import com.studentsupport.repository.UserRepository;
import com.studentsupport.security.JwtService;
import com.studentsupport.security.TokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private StudentProfileRepository studentProfileRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private EmailService emailService;
    @Mock
    private TokenHasher tokenHasher;

    private AuthService authService;

    private Role studentRole;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository, roleRepository, studentProfileRepository, passwordResetTokenRepository,
                refreshTokenRepository, passwordEncoder, jwtService, emailService, tokenHasher);
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 2592000000L);

        studentRole = Role.builder().roleId(1L).roleName(RoleName.STUDENT).build();
        user = User.builder()
                .userId(1L)
                .fullName("Jane Doe")
                .email("jane@example.com")
                .passwordHash("hashed-password")
                .role(studentRole)
                .status(UserStatus.ACTIVE)
                .build();
    }

    private void stubTokenIssuance() {
        // register()'s current implementation discards the userRepository.save() return
        // value, so the entity passed to token generation may still have a null userId
        // at this point (real Hibernate mutates the entity in place; a mock does not).
        when(jwtService.generateToken(nullable(Long.class), anyString(), any())).thenReturn("access-token");
        when(tokenHasher.hash(anyString())).thenReturn("hashed-value");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_savesNewStudentAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(RoleName.STUDENT)).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        stubTokenIssuance();

        AuthResponse response = authService.register(request);

        assertThat(response.getMessage()).isEqualTo("Registration successful");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isNotBlank();
        verify(studentProfileRepository).save(any());
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("Password123");

        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_correctCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed-password")).thenReturn(true);
        stubTokenIssuance();

        AuthResponse response = authService.login(request);

        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(response.getToken()).isEqualTo("access-token");
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_unknownEmail_throwsUnauthorized_withoutRevealingWhichPart() {
        LoginRequest request = new LoginRequest();
        request.setEmail("nobody@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_inactiveAccount_throwsUnauthorized() {
        user.setStatus(UserStatus.INACTIVE);
        LoginRequest request = new LoginRequest();
        request.setEmail("jane@example.com");
        request.setPassword("Password123");

        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "hashed-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void forgotPassword_existingUser_storesHashedTokenAndEmailsRawToken() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(tokenHasher.hash(anyString())).thenReturn("sha256-hash-of-token");

        authService.forgotPassword("jane@example.com");

        verify(passwordResetTokenRepository).deleteByUser(user);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("sha256-hash-of-token");

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(eq("jane@example.com"), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:5173/reset-password?token=");
        // The link must contain the raw token, not the hash that got persisted.
        assertThat(linkCaptor.getValue()).doesNotContain("sha256-hash-of-token");
    }

    @Test
    void forgotPassword_unknownEmail_doesNothingSilently() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword("nobody@example.com");

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_validToken_updatesPasswordAndMarksUsed() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenId(1L)
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        when(tokenHasher.hash("raw-token")).thenReturn("hashed-value");
        when(passwordResetTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hashed-password");

        authService.resetPassword("raw-token", "NewPassword123");

        assertThat(user.getPasswordHash()).isEqualTo("new-hashed-password");
        assertThat(resetToken.isUsed()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_unknownToken_throwsBadRequest() {
        when(tokenHasher.hash("bad-token")).thenReturn("bad-hash");
        when(passwordResetTokenRepository.findByTokenHash("bad-hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "NewPassword123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid or has expired");
    }

    @Test
    void resetPassword_expiredToken_throwsBadRequest() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .used(false)
                .build();

        when(tokenHasher.hash("raw-token")).thenReturn("hashed-value");
        when(passwordResetTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "NewPassword123"))
                .isInstanceOf(BadRequestException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_alreadyUsedToken_throwsBadRequest() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(true)
                .build();

        when(tokenHasher.hash("raw-token")).thenReturn("hashed-value");
        when(passwordResetTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(resetToken));

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "NewPassword123"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void refresh_validToken_rotatesAndIssuesNewTokens() {
        RefreshToken storedToken = RefreshToken.builder()
                .refreshTokenId(1L)
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().plusDays(10))
                .revoked(false)
                .build();

        when(tokenHasher.hash("raw-refresh-token")).thenReturn("hashed-value");
        when(refreshTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(storedToken));
        when(jwtService.generateToken(anyLong(), anyString(), any())).thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = authService.refresh("raw-refresh-token");

        assertThat(storedToken.isRevoked()).isTrue();
        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotEqualTo("raw-refresh-token");
    }

    @Test
    void refresh_revokedToken_throwsUnauthorized() {
        RefreshToken storedToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().plusDays(10))
                .revoked(true)
                .build();

        when(tokenHasher.hash("raw-refresh-token")).thenReturn("hashed-value");
        when(refreshTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_expiredToken_throwsUnauthorized() {
        RefreshToken storedToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .revoked(false)
                .build();

        when(tokenHasher.hash("raw-refresh-token")).thenReturn("hashed-value");
        when(refreshTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh("raw-refresh-token"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void logout_revokesMatchingToken() {
        RefreshToken storedToken = RefreshToken.builder()
                .user(user)
                .tokenHash("hashed-value")
                .expiresAt(LocalDateTime.now().plusDays(10))
                .revoked(false)
                .build();

        when(tokenHasher.hash("raw-refresh-token")).thenReturn("hashed-value");
        when(refreshTokenRepository.findByTokenHash("hashed-value")).thenReturn(Optional.of(storedToken));

        authService.logout("raw-refresh-token");

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    void logout_unknownToken_doesNotThrow() {
        when(tokenHasher.hash("bad-token")).thenReturn("bad-hash");
        when(refreshTokenRepository.findByTokenHash("bad-hash")).thenReturn(Optional.empty());

        authService.logout("bad-token");

        verify(refreshTokenRepository, never()).save(any());
    }
}
