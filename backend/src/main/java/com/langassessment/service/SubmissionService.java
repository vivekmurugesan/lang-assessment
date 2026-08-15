package com.langassessment.service;

import com.langassessment.dto.QuestionResponseDTO;
import com.langassessment.entity.*;
import com.langassessment.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final AssessmentSubmissionRepository submissionRepository;
    private final QuestionResponseRepository responseRepository;
    private final AssessmentCandidateRepository candidateRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public AssessmentSubmission startAssessment(Integer candidateId) {
        AssessmentCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        if (candidate.getStartedAt() == null) {
            candidate.setStartedAt(LocalDateTime.now());
            candidate.setStatus(AssessmentCandidate.CandidateStatus.STARTED);
            candidateRepository.save(candidate);
        }

        return submissionRepository.findByAssessmentCandidate(candidate)
                .orElseGet(() -> {
                    AssessmentSubmission submission = AssessmentSubmission.builder()
                            .assessmentCandidate(candidate)
                            .status(AssessmentSubmission.SubmissionStatus.IN_PROGRESS)
                            .responses(new ArrayList<>())
                            .build();
                    return submissionRepository.save(submission);
                });
    }

    @Transactional
    public QuestionResponseDTO saveQuestionResponse(Integer submissionId, Integer questionId, QuestionResponseDTO dto) {
        AssessmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionResponse response = responseRepository.findBySubmission(submission).stream()
                .filter(r -> r.getQuestion().getId().equals(questionId))
                .findFirst()
                .orElse(QuestionResponse.builder()
                        .submission(submission)
                        .question(question)
                        .build());

        if (dto.getResponseText() != null) {
            response.setResponseText(dto.getResponseText());
        }
        if (dto.getSelectedOption() != null) {
            response.setSelectedOption(dto.getSelectedOption());
        }
        if (dto.getAudioFilePath() != null) {
            response.setAudioFilePath(dto.getAudioFilePath());
        }

        QuestionResponse savedResponse = responseRepository.save(response);
        return convertToDTO(savedResponse);
    }

    @Transactional
    public AssessmentSubmission submitAssessment(Integer submissionId) {
        AssessmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        submission.setStatus(AssessmentSubmission.SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());

        AssessmentCandidate candidate = submission.getAssessmentCandidate();
        candidate.setCompletedAt(LocalDateTime.now());
        candidate.setStatus(AssessmentCandidate.CandidateStatus.COMPLETED);
        candidateRepository.save(candidate);

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public AssessmentSubmission getSubmissionByCandidate(Integer candidateId) {
        AssessmentCandidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        return submissionRepository.findByAssessmentCandidate(candidate)
                .orElseThrow(() -> new RuntimeException("Submission not found"));
    }

    @Transactional(readOnly = true)
    public List<AssessmentSubmission> getSubmissionsByAssessment(Integer assessmentId) {
        return submissionRepository.findByAssessmentCandidateAssessmentId(assessmentId);
    }

    private QuestionResponseDTO convertToDTO(QuestionResponse response) {
        return QuestionResponseDTO.builder()
                .id(response.getId())
                .questionId(response.getQuestion().getId())
                .responseText(response.getResponseText())
                .audioFilePath(response.getAudioFilePath())
                .selectedOption(response.getSelectedOption())
                .score(response.getScore())
                .feedback(response.getFeedback())
                .createdAt(response.getCreatedAt())
                .build();
    }
}
