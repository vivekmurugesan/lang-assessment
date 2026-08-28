package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.GeneratedQuestionDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentModule;
import com.langassessment.entity.Question;
import com.langassessment.repository.AssessmentModuleRepository;
import com.langassessment.repository.AssessmentRepository;
import com.langassessment.repository.QuestionRepository;
import com.langassessment.repository.AssessmentQuestionSelectionRepository;
import com.langassessment.service.QuestionGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/questions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'EVALUATOR')")
public class QuestionGenerationController {

    private final QuestionGenerationService questionGenerationService;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentModuleRepository moduleRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentQuestionSelectionRepository selectionRepository;

    @PostMapping("/generate/{assessmentId}")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<List<GeneratedQuestionDTO>>> generateQuestions(
            @PathVariable Integer assessmentId,
            Authentication authentication) {
        try {
            log.info("Generating questions for assessment: {}", assessmentId);

            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            List<AssessmentModule> modules = moduleRepository.findByAssessment(assessment);
            if (modules.isEmpty()) {
                throw new RuntimeException("Assessment has no modules configured");
            }

            String adminEmail = authentication.getName();
            List<Question> generatedQuestions = questionGenerationService.generateQuestionsForAssessment(
                    assessment, modules, adminEmail
            );

            List<GeneratedQuestionDTO> dtos = generatedQuestions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            log.info("Generated {} questions for assessment: {}", dtos.size(), assessmentId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(
                    true,
                    "Generated " + dtos.size() + " questions successfully",
                    dtos
            ));
        } catch (Exception e) {
            log.error("Failed to generate questions: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, "Generation failed: " + e.getMessage()));
        }
    }

    @GetMapping("/review/{assessmentId}")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<List<GeneratedQuestionDTO>>> reviewPendingQuestions(
            @PathVariable Integer assessmentId) {
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            List<Question> pendingQuestions = questionRepository.findByStatusOrderByModuleTypeAsc(
                    Question.QuestionStatus.PENDING_REVIEW
            );

            // Filter by assessment's language and modules
            List<AssessmentModule> modules = moduleRepository.findByAssessment(assessment);
            List<String> moduleTypes = modules.stream()
                    .map(m -> m.getModuleType().toString())
                    .collect(Collectors.toList());

            List<GeneratedQuestionDTO> dtos = pendingQuestions.stream()
                    .filter(q -> q.getLanguage().getId().equals(assessment.getLanguage().getId()))
                    .filter(q -> moduleTypes.contains(q.getModuleType().toString()))
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Questions retrieved", dtos));
        } catch (Exception e) {
            log.error("Failed to retrieve questions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/assessments/{assessmentId}/status")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<Map<String, Object>>> getAssessmentQuestionStatus(
            @PathVariable Integer assessmentId) {
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            // Get all questions for this assessment's language and modules
            List<AssessmentModule> modules = moduleRepository.findByAssessment(assessment);
            List<String> moduleTypes = modules.stream()
                    .map(m -> m.getModuleType().toString())
                    .collect(Collectors.toList());

            List<Question> allQuestions = questionRepository.findByLanguageId(assessment.getLanguage().getId()).stream()
                    .filter(q -> moduleTypes.contains(q.getModuleType().toString()))
                    .collect(Collectors.toList());

            long generated = allQuestions.size();
            long approved = allQuestions.stream()
                    .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.APPROVED)
                    .count();
            long rejected = allQuestions.stream()
                    .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.REJECTED)
                    .count();
            long pending = allQuestions.stream()
                    .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.PENDING_REVIEW)
                    .count();

            // Count selected questions for this assessment
            long selected = selectionRepository.findByAssessmentOrderBySequenceNumber(assessment).size();

            Map<String, Object> status = new HashMap<>();
            status.put("generated", generated);
            status.put("approved", approved);
            status.put("rejected", rejected);
            status.put("pending", pending);
            status.put("selected", selected);

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment status retrieved", status));
        } catch (Exception e) {
            log.error("Failed to get assessment status: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/{questionId}/approve")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<String>> approveQuestion(
            @PathVariable Integer questionId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            String notes = body != null ? body.get("notes") : null;

            question.setStatus(Question.QuestionStatus.ACTIVE);
            question.setReviewNotes(notes);
            question.setAssessedBy(authentication.getName());
            questionRepository.save(question);

            log.info("Question approved: {}", questionId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question approved"));
        } catch (Exception e) {
            log.error("Failed to approve question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/{questionId}/reject")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<String>> rejectQuestion(
            @PathVariable Integer questionId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            String reason = body.get("reason");

            question.setStatus(Question.QuestionStatus.REJECTED);
            question.setReviewNotes(reason);
            question.setAssessedBy(authentication.getName());
            questionRepository.save(question);

            log.info("Question rejected: {}", questionId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question rejected"));
        } catch (Exception e) {
            log.error("Failed to reject question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    private GeneratedQuestionDTO convertToDTO(Question question) {
        return GeneratedQuestionDTO.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .moduleType(question.getModuleType().toString())
                .cefrLevel(question.getCefrLevel())
                .options(null)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(null)
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .status(question.getStatus().toString())
                .generatedAt(question.getGeneratedAt())
                .generatedBy(question.getGeneratedBy())
                .reviewNotes(question.getReviewNotes())
                .build();
    }
}
