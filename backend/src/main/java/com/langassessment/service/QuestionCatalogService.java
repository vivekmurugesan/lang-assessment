package com.langassessment.service;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentQuestionSelection;
import com.langassessment.entity.Question;
import com.langassessment.repository.AssessmentQuestionSelectionRepository;
import com.langassessment.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionCatalogService {

    private final QuestionRepository questionRepository;
    private final AssessmentQuestionSelectionRepository selectionRepository;

    /**
     * Get paginated list of questions in catalog with filter options
     */
    @Transactional(readOnly = true)
    public Page<Question> getCatalogQuestions(Integer languageId, String moduleType,
                                             String approvalStatus, String catalogCategory, Pageable pageable) {
        if (approvalStatus == null) {
            approvalStatus = "PENDING_REVIEW";
        }

        // Find questions by filters
        if (moduleType != null && catalogCategory != null) {
            return questionRepository.findByLanguageIdAndModuleTypeAndApprovalStatusAndCatalogCategory(
                languageId,
                Question.ModuleType.valueOf(moduleType),
                Question.ApprovalStatus.valueOf(approvalStatus),
                catalogCategory,
                pageable
            );
        } else if (moduleType != null) {
            return questionRepository.findByLanguageIdAndModuleTypeAndApprovalStatus(
                languageId,
                Question.ModuleType.valueOf(moduleType),
                Question.ApprovalStatus.valueOf(approvalStatus),
                pageable
            );
        } else if (catalogCategory != null) {
            return questionRepository.findByLanguageIdAndCatalogCategoryAndApprovalStatus(
                languageId,
                catalogCategory,
                Question.ApprovalStatus.valueOf(approvalStatus),
                pageable
            );
        } else {
            return questionRepository.findByLanguageIdAndApprovalStatus(
                languageId,
                Question.ApprovalStatus.valueOf(approvalStatus),
                pageable
            );
        }
    }

    /**
     * Approve a question for use in assessments
     */
    @Transactional
    public Question approveQuestion(Integer questionId, String approvedBy) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

        question.setApprovalStatus(Question.ApprovalStatus.APPROVED);
        question.setApprovedBy(approvedBy);
        question.setApprovedAt(LocalDateTime.now());
        question.setApprovalNotes(null);

        return questionRepository.save(question);
    }

    /**
     * Reject a question with optional notes
     */
    @Transactional
    public Question rejectQuestion(Integer questionId, String reason, String rejectedBy) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found: " + questionId));

        question.setApprovalStatus(Question.ApprovalStatus.REJECTED);
        question.setApprovedBy(rejectedBy);
        question.setApprovalNotes(reason);
        question.setApprovedAt(LocalDateTime.now());

        return questionRepository.save(question);
    }

    /**
     * Get catalog statistics by language and module
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCatalogStatistics(Integer languageId) {
        List<Question> allQuestions = questionRepository.findByLanguageId(languageId);

        Map<String, Object> stats = new HashMap<>();

        // Count by approval status
        Map<String, Long> statusCounts = allQuestions.stream()
                .collect(Collectors.groupingBy(q -> q.getApprovalStatus().toString(), Collectors.counting()));
        stats.put("byApprovalStatus", statusCounts);

        // Count by module type (only approved questions)
        Map<String, Long> moduleCounts = allQuestions.stream()
                .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.APPROVED)
                .collect(Collectors.groupingBy(q -> q.getModuleType().toString(), Collectors.counting()));
        stats.put("byModule", moduleCounts);

        // Count by category (only approved questions)
        Map<String, Long> categoryCounts = allQuestions.stream()
                .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.APPROVED)
                .collect(Collectors.groupingBy(q -> q.getCatalogCategory() != null ? q.getCatalogCategory() : "GENERAL", Collectors.counting()));
        stats.put("byCategory", categoryCounts);

        stats.put("totalQuestions", allQuestions.size());
        stats.put("approvedQuestions", allQuestions.stream()
                .filter(q -> q.getApprovalStatus() == Question.ApprovalStatus.APPROVED)
                .count());

        return stats;
    }

    /**
     * Select random approved questions for an assessment
     */
    @Transactional
    public List<AssessmentQuestionSelection> selectQuestionsForAssessment(Assessment assessment,
                                                                          Map<String, Integer> moduleCounts) {
        // Clear existing selections
        selectionRepository.deleteByAssessment(assessment);

        List<AssessmentQuestionSelection> selections = new ArrayList<>();
        int sequenceNumber = 1;

        for (Map.Entry<String, Integer> entry : moduleCounts.entrySet()) {
            String moduleType = entry.getKey();
            int count = entry.getValue();

            // Get approved questions for this module
            List<Question> approvedQuestions = questionRepository.findByLanguageIdAndModuleTypeAndApprovalStatus(
                assessment.getLanguage().getId(),
                Question.ModuleType.valueOf(moduleType),
                Question.ApprovalStatus.APPROVED
            );

            if (approvedQuestions.isEmpty()) {
                throw new RuntimeException("No approved questions available for module: " + moduleType);
            }

            if (approvedQuestions.size() < count) {
                log.warn("Requested {} questions for module {} but only {} approved questions available",
                        count, moduleType, approvedQuestions.size());
            }

            // Shuffle and select random questions
            Collections.shuffle(approvedQuestions);
            int selectCount = Math.min(count, approvedQuestions.size());

            for (int i = 0; i < selectCount; i++) {
                AssessmentQuestionSelection selection = AssessmentQuestionSelection.builder()
                        .assessment(assessment)
                        .question(approvedQuestions.get(i))
                        .sequenceNumber(sequenceNumber++)
                        .build();
                selections.add(selection);
            }
        }

        return selectionRepository.saveAll(selections);
    }

    /**
     * Get questions selected for an assessment (in order)
     */
    @Transactional(readOnly = true)
    public List<Question> getAssessmentQuestions(Assessment assessment) {
        return selectionRepository.findByAssessmentOrderBySequenceNumber(assessment).stream()
                .map(AssessmentQuestionSelection::getQuestion)
                .collect(Collectors.toList());
    }
}
