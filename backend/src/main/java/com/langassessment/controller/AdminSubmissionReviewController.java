package com.langassessment.controller;

import com.langassessment.dto.AuthDTO;
import com.langassessment.dto.AssessmentSubmissionDTO;
import com.langassessment.dto.QuestionResponseDTO;
import com.langassessment.entity.AssessmentSubmission;
import com.langassessment.entity.QuestionResponse;
import com.langassessment.repository.AssessmentSubmissionRepository;
import com.langassessment.repository.QuestionResponseRepository;
import com.langassessment.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/submissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'EVALUATOR')")
public class AdminSubmissionReviewController {

    private final AssessmentSubmissionRepository submissionRepository;
    private final QuestionResponseRepository responseRepository;
    private final SubmissionService submissionService;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<Page<AssessmentSubmissionDTO>>> getPendingSubmissions(
            Pageable pageable) {
        try {
            Page<AssessmentSubmission> submissions = submissionRepository.findAll(pageable);

            Page<AssessmentSubmissionDTO> dtos = submissions.map(submission -> {
                List<QuestionResponse> responses = responseRepository.findBySubmission(submission);
                int totalQuestions = responses.size();
                int correctAnswers = (int) responses.stream()
                        .filter(r -> r.getScore() != null && r.getScore() > 0)
                        .count();

                return AssessmentSubmissionDTO.builder()
                        .id(submission.getId())
                        .assessmentCandidateId(submission.getAssessmentCandidate().getId())
                        .status(submission.getStatus().toString())
                        .totalScore(submission.getTotalScore())
                        .cefrLevel(submission.getCefrLevel())
                        .totalQuestions(totalQuestions)
                        .correctAnswers(correctAnswers)
                        .submittedAt(submission.getSubmittedAt())
                        .evaluatedAt(submission.getEvaluatedAt())
                        .build();
            });

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Submissions retrieved", dtos));
        } catch (Exception e) {
            log.error("Failed to retrieve submissions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/candidate/{candidateId}")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> getSubmissionByCandidate(
            @PathVariable Integer candidateId) {
        try {
            List<AssessmentSubmission> submissions = submissionRepository.findAll().stream()
                    .filter(s -> s.getAssessmentCandidate().getId().equals(candidateId))
                    .toList();

            if (submissions.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new AuthDTO.ApiResponse<>(false, "No submission found for this candidate"));
            }

            AssessmentSubmission submission = submissions.get(0);
            List<QuestionResponse> responses = responseRepository.findBySubmission(submission);
            int totalQuestions = responses.size();
            int correctAnswers = (int) responses.stream()
                    .filter(r -> r.getScore() != null && r.getScore() > 0)
                    .count();

            List<QuestionResponseDTO> responsesDtos = responses.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            AssessmentSubmissionDTO dto = AssessmentSubmissionDTO.builder()
                    .id(submission.getId())
                    .assessmentCandidateId(submission.getAssessmentCandidate().getId())
                    .status(submission.getStatus().toString())
                    .totalScore(submission.getTotalScore())
                    .cefrLevel(submission.getCefrLevel())
                    .evaluatorNotes(submission.getEvaluatorNotes())
                    .totalQuestions(totalQuestions)
                    .correctAnswers(correctAnswers)
                    .responses(responsesDtos)
                    .submittedAt(submission.getSubmittedAt())
                    .evaluatedAt(submission.getEvaluatedAt())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Submission details retrieved", dto));
        } catch (Exception e) {
            log.error("Failed to retrieve submission: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/{submissionId}")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> getSubmissionDetails(
            @PathVariable Integer submissionId) {
        try {
            AssessmentSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found"));

            List<QuestionResponse> responses = responseRepository.findBySubmission(submission);
            int totalQuestions = responses.size();
            int correctAnswers = (int) responses.stream()
                    .filter(r -> r.getScore() != null && r.getScore() > 0)
                    .count();

            List<QuestionResponseDTO> responsesDtos = responses.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            AssessmentSubmissionDTO dto = AssessmentSubmissionDTO.builder()
                    .id(submission.getId())
                    .assessmentCandidateId(submission.getAssessmentCandidate().getId())
                    .status(submission.getStatus().toString())
                    .totalScore(submission.getTotalScore())
                    .cefrLevel(submission.getCefrLevel())
                    .evaluatorNotes(submission.getEvaluatorNotes())
                    .totalQuestions(totalQuestions)
                    .correctAnswers(correctAnswers)
                    .responses(responsesDtos)
                    .submittedAt(submission.getSubmittedAt())
                    .evaluatedAt(submission.getEvaluatedAt())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Submission details retrieved", dto));
        } catch (Exception e) {
            log.error("Failed to retrieve submission: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/{submissionId}/evaluate")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> evaluateSubmission(
            @PathVariable Integer submissionId,
            @RequestBody Map<String, Object> evaluationData,
            Authentication authentication) {
        try {
            AssessmentSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found"));

            // Update manual scores if provided
            if (evaluationData.containsKey("responseScores")) {
                Map<Integer, Double> responseScores = (Map<Integer, Double>) evaluationData.get("responseScores");
                List<QuestionResponse> responses = responseRepository.findBySubmission(submission);

                responses.forEach(response -> {
                    if (responseScores.containsKey(response.getId())) {
                        response.setScore(responseScores.get(response.getId()));
                        responseRepository.save(response);
                    }
                });
            }

            // Update evaluator notes
            if (evaluationData.containsKey("evaluatorNotes")) {
                submission.setEvaluatorNotes((String) evaluationData.get("evaluatorNotes"));
            }

            // Recalculate total score
            List<QuestionResponse> responses = responseRepository.findBySubmission(submission);
            double totalScore = responses.stream()
                    .mapToDouble(r -> r.getScore() != null ? r.getScore() : 0)
                    .average()
                    .orElse(0);

            submission.setTotalScore(totalScore);
            submission.setCefrLevel(determineCEFRLevel(totalScore));
            submission.setStatus(AssessmentSubmission.SubmissionStatus.EVALUATED);
            submission.setEvaluatedAt(LocalDateTime.now());

            AssessmentSubmission saved = submissionRepository.save(submission);

            int correctAnswers = (int) responses.stream()
                    .filter(r -> r.getScore() != null && r.getScore() > 0)
                    .count();

            AssessmentSubmissionDTO dto = AssessmentSubmissionDTO.builder()
                    .id(saved.getId())
                    .assessmentCandidateId(saved.getAssessmentCandidate().getId())
                    .status(saved.getStatus().toString())
                    .totalScore(saved.getTotalScore())
                    .cefrLevel(saved.getCefrLevel())
                    .evaluatorNotes(saved.getEvaluatorNotes())
                    .totalQuestions(responses.size())
                    .correctAnswers(correctAnswers)
                    .build();

            log.info("Submission {} evaluated by {} - Score: {}", submissionId, authentication.getName(), totalScore);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Submission evaluated successfully", dto));
        } catch (Exception e) {
            log.error("Failed to evaluate submission: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    private String determineCEFRLevel(double score) {
        if (score >= 90) return "C2";
        if (score >= 80) return "C1";
        if (score >= 70) return "B2";
        if (score >= 60) return "B1";
        if (score >= 50) return "A2";
        return "A1";
    }

    private QuestionResponseDTO convertToDTO(QuestionResponse response) {
        return QuestionResponseDTO.builder()
                .id(response.getId())
                .questionId(response.getQuestion().getId())
                .questionText(response.getQuestion().getQuestionText())
                .moduleType(response.getQuestion().getModuleType().toString())
                .responseText(response.getResponseText())
                .selectedOption(response.getSelectedOption())
                .score(response.getScore())
                .feedback(response.getFeedback())
                .build();
    }
}
