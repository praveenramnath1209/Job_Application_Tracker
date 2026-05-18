package com.jobtracker.service;

import com.jobtracker.dto.request.CreateApplicationRequest;
import com.jobtracker.dto.response.ApplicationResponse;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock private JobApplicationRepository applicationRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ApplicationService applicationService;

    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed")
                .role(User.Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Should create application with APPLIED status")
    void shouldCreateApplicationWithAppliedStatus() {
        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setCompanyName("Google");
        request.setRole("Software Engineer");

        JobApplication savedApp = JobApplication.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .companyName("Google")
                .role("Software Engineer")
                .status("Applied")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(applicationRepository.save(any(JobApplication.class))).thenReturn(savedApp);

        ApplicationResponse response = applicationService.createApplication(request, userId);

        assertThat(response.getCompanyName()).isEqualTo("Google");
        assertThat(response.getRole()).isEqualTo("Software Engineer");
        assertThat(response.getStatus()).isEqualTo("Applied");
        verify(applicationRepository, times(1)).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setCompanyName("Netflix");
        request.setRole("Backend Engineer");

        assertThatThrownBy(() -> applicationService.createApplication(request, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when accessing another user's application")
    void shouldThrowExceptionWhenAccessingOtherUsersApplication() {
        UUID appId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        when(applicationRepository.findByIdAndUserId(appId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplicationById(appId, otherUserId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
