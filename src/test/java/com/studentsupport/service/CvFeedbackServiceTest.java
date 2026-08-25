package com.studentsupport.service;

import com.studentsupport.dto.CvFeedbackResponse;
import com.studentsupport.entity.CvFeedback;
import com.studentsupport.entity.User;
import com.studentsupport.exception.ResourceNotFoundException;
import com.studentsupport.repository.CvFeedbackRepository;
import com.studentsupport.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CvFeedbackServiceTest {

    @Mock
    private CvFeedbackRepository cvFeedbackRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GeminiService geminiService;
    @Mock
    private CvFileParsingService cvFileParsingService;

    @InjectMocks
    private CvFeedbackService cvFeedbackService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().userId(1L).fullName("Jane Doe").email("jane@example.com").build();
    }

    @Test
    void submit_savesAiGeneratedFeedback() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(geminiService.generateCvFeedback("my cv text", false))
                .thenReturn(new GeminiService.AiResult("Great CV, add metrics.", false));
        when(cvFeedbackRepository.save(any(CvFeedback.class))).thenAnswer(inv -> {
            CvFeedback cv = inv.getArgument(0);
            cv.setCvFeedbackId(42L);
            return cv;
        });

        CvFeedbackResponse response = cvFeedbackService.submit(1L, "my cv text", false);

        assertThat(response.getCvFeedbackId()).isEqualTo(42L);
        assertThat(response.getInputText()).isEqualTo("my cv text");
        assertThat(response.getFeedbackText()).isEqualTo("Great CV, add metrics.");
        assertThat(response.isDetailed()).isFalse();
    }

    @Test
    void submit_detailed_passesFlagToGeminiAndPersistsIt() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(geminiService.generateCvFeedback("my cv text", true))
                .thenReturn(new GeminiService.AiResult("Original: ... Rewrite: ...", false));
        when(cvFeedbackRepository.save(any(CvFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        CvFeedbackResponse response = cvFeedbackService.submit(1L, "my cv text", true);

        assertThat(response.isDetailed()).isTrue();
        verify(geminiService).generateCvFeedback("my cv text", true);
    }

    @Test
    void submit_unknownUser_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cvFeedbackService.submit(99L, "text", false))
                .isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(geminiService);
    }

    @Test
    void submitFromFile_extractsTextThenSubmits() {
        MockMultipartFile file = new MockMultipartFile("file", "cv.pdf", "application/pdf", "irrelevant".getBytes());
        when(cvFileParsingService.extractText(file)).thenReturn("extracted cv text");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(geminiService.generateCvFeedback("extracted cv text", false))
                .thenReturn(new GeminiService.AiResult("Feedback here", false));
        when(cvFeedbackRepository.save(any(CvFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        CvFeedbackResponse response = cvFeedbackService.submitFromFile(1L, file, false);

        assertThat(response.getInputText()).isEqualTo("extracted cv text");
        verify(geminiService).generateCvFeedback("extracted cv text", false);
    }

    @Test
    void listForUser_returnsMostRecentFirst() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        CvFeedback older = CvFeedback.builder().cvFeedbackId(1L).user(user).inputText("a").feedbackText("fa").build();
        CvFeedback newer = CvFeedback.builder().cvFeedbackId(2L).user(user).inputText("b").feedbackText("fb").build();
        when(cvFeedbackRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(newer, older));

        List<CvFeedbackResponse> result = cvFeedbackService.listForUser(1L);

        assertThat(result).extracting(CvFeedbackResponse::getCvFeedbackId).containsExactly(2L, 1L);
    }

    @Test
    void getById_notOwnedByUser_throwsResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cvFeedbackRepository.findByCvFeedbackIdAndUser(5L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cvFeedbackService.getById(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
