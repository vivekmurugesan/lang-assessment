package com.langassessment.service;

import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentSubmission;
import com.langassessment.entity.Question;
import com.langassessment.entity.QuestionResponse;
import com.langassessment.repository.AssessmentSubmissionRepository;
import com.langassessment.repository.QuestionRepository;
import com.langassessment.repository.QuestionResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final AssessmentSubmissionRepository submissionRepository;
    private final QuestionResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    @Transactional
    public void evaluateSubmission(AssessmentSubmission submission) {
        try {
            log.info("Starting evaluation for submission: {}", submission.getId());

            List<QuestionResponse> responses = responseRepository.findBySubmission(submission);

            // Score multiple-choice and reading questions
            int totalScore = 0;
            Map<String, Integer> moduleScores = new HashMap<>();

            for (QuestionResponse response : responses) {
                Question question = response.getQuestion();
                String moduleType = question.getModuleType().toString();

                if (isMultipleChoice(question)) {
                    int score = evaluateMultipleChoice(response, question);
                    response.setScore((double) score);
                    totalScore += score;
                    moduleScores.put(moduleType, moduleScores.getOrDefault(moduleType, 0) + score);
                } else if (isOpenEnded(question)) {
                    // Call Gemini for evaluation of open-ended responses
                    String feedback = evaluateOpenEndedResponse(response, question);
                    response.setFeedback(feedback);
                }

                responseRepository.save(response);
            }

            // Calculate average score
            int maxScore = responses.size() * 100;
            double finalScore = (double) totalScore / responses.size();

            submission.setTotalScore(finalScore);
            submission.setCefrLevel(determineCEFRLevel(finalScore));
            submission.setStatus(AssessmentSubmission.SubmissionStatus.EVALUATED);
            submission.setEvaluatedAt(LocalDateTime.now());

            submissionRepository.save(submission);

            log.info("Evaluation completed for submission: {} - Score: {}, CEFR: {}",
                    submission.getId(), finalScore, submission.getCefrLevel());
        } catch (Exception e) {
            log.error("Error evaluating submission: {}", e.getMessage(), e);
            submission.setStatus(AssessmentSubmission.SubmissionStatus.EVALUATING);
            submissionRepository.save(submission);
        }
    }

    private int evaluateMultipleChoice(QuestionResponse response, Question question) {
        String correctAnswer = question.getCorrectAnswer();
        if (correctAnswer != null && correctAnswer.equals(response.getSelectedOption())) {
            return 100;
        }
        return 0;
    }

    private String evaluateOpenEndedResponse(QuestionResponse response, Question question) {
        try {
            String prompt = buildEvaluationPrompt(question, response);
            String evaluation = callGeminiForEvaluation(prompt);
            return evaluation;
        } catch (Exception e) {
            log.error("Error evaluating open-ended response: {}", e.getMessage());
            return "Evaluation pending";
        }
    }

    private String buildEvaluationPrompt(Question question, QuestionResponse response) {
        return String.format("""
                Evaluate this candidate's response to a language assessment question.

                Question: %s
                Question Type: %s
                CEFR Level: %s

                Candidate's Response: %s

                Provide a brief evaluation (2-3 sentences) covering:
                1. Correctness and completeness
                2. Grammar and language use
                3. Strengths and areas for improvement

                Format as: "Assessment: [your evaluation]"
                """,
                question.getQuestionText(),
                question.getModuleType(),
                question.getCefrLevel(),
                response.getResponseText() != null ? response.getResponseText() : "[Audio/Speaking response]"
        );
    }

    private String callGeminiForEvaluation(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, Object> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String url = geminiApiUrl + "?key=" + geminiApiKey;

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }
            return "Evaluation completed";
        } catch (Exception e) {
            log.error("Error calling Gemini for evaluation: {}", e.getMessage());
            return "Evaluation pending";
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            log.error("Error extracting text from Gemini response: {}", e.getMessage());
        }
        return "Evaluation completed";
    }

    private boolean isMultipleChoice(Question question) {
        return question.getQuestionOptionsUri() != null &&
                (question.getModuleType() == Question.ModuleType.LISTENING ||
                 question.getModuleType() == Question.ModuleType.READING);
    }

    private boolean isOpenEnded(Question question) {
        return question.getModuleType() == Question.ModuleType.WRITING ||
                question.getModuleType() == Question.ModuleType.SPOKEN_INTERACTION ||
                question.getModuleType() == Question.ModuleType.SPOKEN_PRODUCTION;
    }

    private String determineCEFRLevel(double score) {
        if (score >= 90) return "C2";
        if (score >= 80) return "C1";
        if (score >= 70) return "B2";
        if (score >= 60) return "B1";
        if (score >= 50) return "A2";
        return "A1";
    }
}
