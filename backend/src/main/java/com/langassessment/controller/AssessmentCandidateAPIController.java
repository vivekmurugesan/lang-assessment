package com.langassessment.controller;

import com.langassessment.dto.*;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentCandidate;
import com.langassessment.entity.AssessmentModule;
import com.langassessment.entity.Question;
import com.langassessment.entity.QuestionResponse;
import com.langassessment.entity.User;
import com.langassessment.repository.AssessmentModuleRepository;
import com.langassessment.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
@Slf4j
public class AssessmentCandidateAPIController {

    private final CandidateService candidateService;
    private final AssessmentService assessmentService;
    private final SubmissionService submissionService;
    private final QuestionService questionService;
    private final AssessmentModuleService moduleService;
    private final AssessmentModuleRepository moduleRepository;
    private final QuestionCatalogService catalogService;

    @GetMapping("/my-assessments")
    public ResponseEntity<AuthDTO.ApiResponse<List<CandidateDTO>>> getMyAssessments(
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<CandidateDTO> assessments = candidateService.getCandidateAssessments(user);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessments retrieved", assessments));
        } catch (Exception e) {
            log.error("Failed to retrieve candidate assessments: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/assessments/{secureLink}")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentWithModulesDTO>> getAssessment(
            @PathVariable String secureLink) {
        try {
            AssessmentCandidate candidate = candidateService.getCandidateBySecureLinkEntity(secureLink);
            Assessment assessment = candidate.getAssessment();

            AssessmentWithModulesDTO dto = AssessmentWithModulesDTO.builder()
                    .id(assessment.getId())
                    .title(assessment.getTitle())
                    .description(assessment.getDescription())
                    .languageId(assessment.getLanguage().getId())
                    .languageName(assessment.getLanguage().getName())
                    .status(assessment.getStatus().toString())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment retrieved", dto));
        } catch (Exception e) {
            log.error("Failed to retrieve assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/assessments/{secureLink}/start")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> startAssessment(
            @PathVariable String secureLink) {
        try {
            AssessmentCandidate candidate = candidateService.getCandidateBySecureLinkEntity(secureLink);
            var submission = submissionService.startAssessment(candidate.getId());

            AssessmentSubmissionDTO dto = AssessmentSubmissionDTO.builder()
                    .id(submission.getId())
                    .assessmentCandidateId(submission.getAssessmentCandidate().getId())
                    .status(submission.getStatus().toString())
                    .createdAt(submission.getCreatedAt())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment started", dto));
        } catch (Exception e) {
            log.error("Failed to start assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/assessments/{secureLink}/questions")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<List<QuestionWithOptionsDTO>>> getAssessmentQuestions(
            @PathVariable String secureLink) {
        try {
            AssessmentCandidate candidate = candidateService.getCandidateBySecureLinkEntity(secureLink);
            Assessment assessment = candidate.getAssessment();

            // Try to get pre-selected questions from catalog
            List<Question> selectedQuestions = catalogService.getAssessmentQuestions(assessment);

            if (selectedQuestions.isEmpty()) {
                log.warn("No pre-selected questions found for assessment: {}", assessment.getId());
                return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "No questions available", List.of()));
            }

            List<QuestionWithOptionsDTO> questionDtos = selectedQuestions.stream()
                    .map(q -> convertToQuestionDTO(q, q.getModuleType().toString()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Questions retrieved", questionDtos));
        } catch (Exception e) {
            log.error("Failed to retrieve questions: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/submissions/{submissionId}/responses")
    public ResponseEntity<AuthDTO.ApiResponse<QuestionResponseDTO>> saveResponse(
            @PathVariable Integer submissionId,
            @RequestParam Integer questionId,
            @RequestBody QuestionResponseDTO dto) {
        try {
            var response = submissionService.saveQuestionResponse(submissionId, questionId, dto);
            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Response saved", response));
        } catch (Exception e) {
            log.error("Failed to save response: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @PostMapping("/submissions/{submissionId}/submit")
    @Transactional
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> submitAssessment(
            @PathVariable Integer submissionId) {
        try {
            var submission = submissionService.submitAssessment(submissionId);

            AssessmentSubmissionDTO dto = AssessmentSubmissionDTO.builder()
                    .id(submission.getId())
                    .assessmentCandidateId(submission.getAssessmentCandidate().getId())
                    .status(submission.getStatus().toString())
                    .submittedAt(submission.getSubmittedAt())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Assessment submitted", dto));
        } catch (Exception e) {
            log.error("Failed to submit assessment: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    @GetMapping("/submissions/{secureLink}/results")
    @Transactional(readOnly = true)
    public ResponseEntity<AuthDTO.ApiResponse<AssessmentSubmissionDTO>> getSubmissionResults(
            @PathVariable String secureLink) {
        try {
            AssessmentCandidate candidate = candidateService.getCandidateBySecureLinkEntity(secureLink);
            var submission = submissionService.getSubmissionByCandidate(candidate.getId());

            // Calculate total questions and correct answers
            List<QuestionResponse> responses = submission.getResponses();
            int totalQuestions = responses.size();
            int correctAnswers = (int) responses.stream()
                    .filter(r -> r.getScore() != null && r.getScore() > 0)
                    .count();

            // Calculate module performance
            Assessment assessment = candidate.getAssessment();
            List<AssessmentModule> modules = moduleRepository.findByAssessment(assessment);

            List<AssessmentSubmissionDTO.ModulePerformanceDTO> modulePerformance = modules.stream()
                    .map(module -> {
                        List<QuestionResponse> moduleResponses = responses.stream()
                                .filter(r -> r.getQuestion().getModuleType().toString()
                                        .equals(module.getModuleType().toString()))
                                .collect(Collectors.toList());

                        int moduleTotal = moduleResponses.size();
                        int moduleCorrect = (int) moduleResponses.stream()
                                .filter(r -> r.getScore() != null && r.getScore() > 0)
                                .count();
                        double moduleScore = moduleTotal > 0 ? (moduleCorrect * 100.0 / moduleTotal) : 0;

                        return AssessmentSubmissionDTO.ModulePerformanceDTO.builder()
                                .moduleType(module.getModuleType().toString())
                                .total(moduleTotal)
                                .correct(moduleCorrect)
                                .score(moduleScore)
                                .build();
                    })
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
                    .modulePerformance(modulePerformance)
                    .submittedAt(submission.getSubmittedAt())
                    .evaluatedAt(submission.getEvaluatedAt())
                    .build();

            return ResponseEntity.ok(new AuthDTO.ApiResponse<>(true, "Results retrieved", dto));
        } catch (Exception e) {
            log.error("Failed to retrieve results: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthDTO.ApiResponse<>(false, e.getMessage()));
        }
    }

    private QuestionWithOptionsDTO convertToQuestionDTO(Question question, String moduleType) {
        return QuestionWithOptionsDTO.builder()
                .id(question.getId())
                .moduleType(moduleType)
                .cefrLevel(question.getCefrLevel())
                .questionText(question.getQuestionText())
                .questionNumber(question.getQuestionNumber())
                .audioUrl(question.getAudioUrl())
                .imageUrl(question.getImageUrl())
                .questionOptionsUri(question.getQuestionOptionsUri())
                .explanationUri(question.getExplanationUri())
                .build();
    }
}
