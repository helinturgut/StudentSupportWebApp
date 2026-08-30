package com.studentsupport;

import com.studentsupport.dto.AuthResponse;
import com.studentsupport.dto.RegisterRequest;
import com.studentsupport.dto.ReminderRequest;
import com.studentsupport.dto.ReminderResponse;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End to end coverage for the Reminders feature, exercising the real
 * HTTP layer, security and database against an isolated H2 instance.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReminderIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders authHeadersForNewUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Reminder Test User");
        request.setEmail("reminders+" + System.nanoTime() + "@example.com");
        request.setPassword("Password123");
        AuthResponse auth = restTemplate.postForEntity("/api/auth/register", request, AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.getToken());
        return headers;
    }

    @Test
    void fullReminderLifecycle_createListToggleDelete() {
        HttpHeaders headers = authHeadersForNewUser();

        ReminderRequest createRequest = new ReminderRequest();
        createRequest.setTitle("Apply to internship");
        createRequest.setDescription("Deadline for the program");
        createRequest.setDueDate(LocalDate.now().plusDays(7));

        ResponseEntity<ReminderResponse> createResponse = restTemplate.exchange(
                "/api/reminders", HttpMethod.POST, new HttpEntity<>(createRequest, headers), ReminderResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ReminderResponse created = createResponse.getBody();
        assertThat(created.isCompleted()).isFalse();

        ResponseEntity<ReminderResponse[]> listResponse = restTemplate.exchange(
                "/api/reminders", HttpMethod.GET, new HttpEntity<>(headers), ReminderResponse[].class);
        assertThat(listResponse.getBody()).hasSize(1);

        ResponseEntity<ReminderResponse> toggleResponse = restTemplate.exchange(
                "/api/reminders/" + created.getReminderId() + "/complete",
                HttpMethod.PATCH, new HttpEntity<>(headers), ReminderResponse.class);
        assertThat(toggleResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(toggleResponse.getBody().isCompleted()).isTrue();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/reminders/" + created.getReminderId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<ReminderResponse[]> listAfterDelete = restTemplate.exchange(
                "/api/reminders", HttpMethod.GET, new HttpEntity<>(headers), ReminderResponse[].class);
        assertThat(listAfterDelete.getBody()).isEmpty();
    }

    @Test
    void creatingReminder_withPastDueDate_isRejected() {
        HttpHeaders headers = authHeadersForNewUser();

        ReminderRequest request = new ReminderRequest();
        request.setTitle("Bad reminder");
        request.setDueDate(LocalDate.now().minusDays(1));

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reminders", HttpMethod.POST, new HttpEntity<>(request, headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reminders_areIsolatedPerUser() {
        HttpHeaders userA = authHeadersForNewUser();
        HttpHeaders userB = authHeadersForNewUser();

        ReminderRequest request = new ReminderRequest();
        request.setTitle("User A's reminder");
        request.setDueDate(LocalDate.now().plusDays(1));
        restTemplate.exchange("/api/reminders", HttpMethod.POST, new HttpEntity<>(request, userA), ReminderResponse.class);

        ResponseEntity<ReminderResponse[]> userBList = restTemplate.exchange(
                "/api/reminders", HttpMethod.GET, new HttpEntity<>(userB), ReminderResponse[].class);

        assertThat(userBList.getBody()).isEmpty();
    }
}
