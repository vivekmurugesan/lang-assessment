package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.QuestionDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.Question;
import com.langassessment.repository.AssessmentRepository;
import com.langassessment.service.QuestionCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/questions/catalog")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'EVALUATOR')")
public class QuestionCatalogController {

    private final QuestionCatalogService catalogService;
    private final AssessmentRepository assessmentRepository;

    @GetMapping
    public ResponseEntity<AuthDTO.ApiResponse<Page<QuestionDTO>>> getCatalogQuestions(
            @RequestParam Integer languageId,
            @RequestParam(required = false) String moduleType,
            @RequestParam(required = false, defaultValue = "PENDING_REVIEW") String approvalStatus,
            @RequestParam(required = false) String catalogCategory,
            Pageable pageable) {
        try {
            Page<Question> questions = catalogService.getCatalogQuestions(
                languageId, moduleType, approvalStatus, catalogCategory, pageable
            );

            Page<QuestionDTO> dtos = questions.map(this::convertToDTO);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Catalog questions retrieved", dtos));
        } catch (Exception e) {
            log.error("Failed to retrieve catalog questions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<AuthDTO.ApiResponse<Map<String, Object>>> getCatalogStatistics(
            @RequestParam Integer languageId) {
        try {
            Map<String, Object> stats = catalogService.getCatalogStatistics(languageId);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Statistics retrieved", stats));
        } catch (Exception e) {
            log.error("Failed to retrieve catalog statistics: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/{questionId}/approve")
    public ResponseEntity<AuthDTO.ApiResponse<QuestionDTO>> approveQuestion(
            @PathVariable Integer questionId,
            @RequestParam(required = false) String approvedBy) {
        try {
            Question question = catalogService.approveQuestion(questionId, approvedBy);
            QuestionDTO dto = convertToDTO(question);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question approved", dto));
        } catch (Exception e) {
            log.error("Failed to approve question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/{questionId}/reject")
    public ResponseEntity<AuthDTO.ApiResponse<QuestionDTO>> rejectQuestion(
            @PathVariable Integer questionId,
            @RequestParam String reason,
            @RequestParam(required = false) String rejectedBy) {
        try {
            Question question = catalogService.rejectQuestion(questionId, reason, rejectedBy);
            QuestionDTO dto = convertToDTO(question);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Question rejected", dto));
        } catch (Exception e) {
            log.error("Failed to reject question: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/assessments/{assessmentId}/select-questions")
    public ResponseEntity<AuthDTO.ApiResponse<Map<String, Object>>> selectQuestionsForAssessment(
            @PathVariable Integer assessmentId,
            @RequestBody Map<String, Integer> moduleCounts) {
        try {
            Assessment assessment = assessmentRepository.findById(assessmentId)
                    .orElseThrow(() -> new RuntimeException("Assessment not found"));

            List<?> selections = catalogService.selectQuestionsForAssessment(assessment, moduleCounts);

            Map<String, Object> result = Map.of(
                "assessment_id", assessmentId,
                "selected_question_count", selections.size(),
                "message", "Questions selected from catalog successfully"
            );

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Questions selected", result));
        } catch (Exception e) {
            log.error("Failed to select questions for assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    private QuestionDTO convertToDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .moduleType(question.getModuleType().toString())
                .cefrLevel(question.getCefrLevel())
                .questionText(question.getQuestionText())
                .status(question.getStatus().toString())
                .approvalStatus(question.getApprovalStatus() != null ? question.getApprovalStatus().toString() : "PENDING_REVIEW")
                .catalogCategory(question.getCatalogCategory())
                .approvalNotes(question.getApprovalNotes())
                .approvedBy(question.getApprovedBy())
                .approvedAt(question.getApprovedAt())
                .createdAt(question.getCreatedAt())
                .build();
    }
}
