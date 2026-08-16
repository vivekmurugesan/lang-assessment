package com.langassessment.service;

import com.langassessment.dto.GeneratedQuestionDTO;
import com.langassessment.entity.Assessment;
import com.langassessment.entity.AssessmentModule;
import com.langassessment.entity.Language;
import com.langassessment.entity.Question;
import com.langassessment.repository.QuestionRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionGenerationService {

    private final QuestionRepository questionRepository;
    private final MinIOService minIOService;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent}")
    private String geminiApiUrl;

    @Transactional
    public List<Question> generateQuestionsForAssessment(Assessment assessment, List<AssessmentModule> modules, String createdBy) {
        List<Question> generatedQuestions = new ArrayList<>();

        for (AssessmentModule module : modules) {
            log.info("Generating {} questions for module: {}", module.getNumQuestions(), module.getModuleType());

            List<Question> moduleQuestions = generateQuestionsForModule(
                    assessment,
                    module.getModuleType().toString(),
                    module.getNumQuestions(),
                    assessment.getLanguage(),
                    createdBy
            );

            generatedQuestions.addAll(moduleQuestions);
            questionRepository.saveAll(moduleQuestions);
        }

        log.info("Generated {} questions for assessment: {}", generatedQuestions.size(), assessment.getId());
        return generatedQuestions;
    }

    private List<Question> generateQuestionsForModule(Assessment assessment, String moduleType, Integer count, Language language, String createdBy) {
        String prompt = buildPrompt(assessment, moduleType, count, language.getName());
        String response = callGeminiAPI(prompt);
        return parseAndCreateQuestions(response, moduleType, language, createdBy, count);
    }

    private String buildPrompt(Assessment assessment, String moduleType, Integer count, String languageName) {
        String cefrLevel = "INTERMEDIATE";
        return String.format("""
                Generate %d high-quality language assessment questions for the %s module in %s.
                Assessment: %s
                Description: %s
                Language Level: %s

                For %s module, create questions that:
                - Are appropriate for %s learners
                - Test comprehension and language skills
                - Have clear answers
                %s

                Return ONLY a JSON array with format:
                [
                  {
                    "questionText": "Question here",
                    "type": "multiple-choice|short-answer|essay",
                    "options": ["A", "B", "C", "D"] (if multiple choice),
                    "correctAnswer": "A" (if multiple choice),
                    "explanation": "Why this answer is correct"
                  }
                ]
                """,
                count,
                moduleType,
                languageName,
                assessment.getTitle(),
                assessment.getDescription() != null ? assessment.getDescription() : "Not provided",
                cefrLevel,
                moduleType,
                cefrLevel,
                getModuleSpecificGuidance(moduleType)
        );
    }

    private String getModuleSpecificGuidance(String moduleType) {
        return switch (moduleType) {
            case "LISTENING" -> "- Each question should have corresponding audio content";
            case "READING" -> "- Base questions on short text passages";
            case "WRITING" -> "- Provide writing prompts with clear requirements";
            case "SPOKEN_INTERACTION" -> "- Create conversational scenarios";
            case "SPOKEN_PRODUCTION" -> "- Create reading/speaking prompts";
            default -> "";
        };
    }

    private void listAvailableModels() {
        try {
            log.info("=== DEBUGGING: Listing available Gemini API models ===");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + geminiApiKey;
            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("models")) {
                    List<Map<String, Object>> models = (List<Map<String, Object>>) body.get("models");
                    log.info("Available Gemini models:");
                    for (Map<String, Object> model : models) {
                        String name = (String) model.get("name");
                        String displayName = (String) model.get("displayName");
                        List<String> supportedMethods = (List<String>) model.get("supportedGenerationMethods");
                        log.info("  - {} ({}): {}", name, displayName, supportedMethods);
                    }
                } else {
                    log.warn("No 'models' field in response");
                    log.debug("Response body: {}", body);
                }
            } else {
                log.error("Failed to list models: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error listing available models: {}", e.getMessage(), e);
        }
        log.info("=== END MODEL LISTING DEBUG ===");
    }

    private String callGeminiAPI(String prompt) {
        // Debug: List available models on first call
        listAvailableModels();

        int maxRetries = 3;
        int retryCount = 0;
        long initialWait = 1000; // 1 second

        log.info("Attempting to generate questions using Gemini API");
        log.info("API URL: {}", geminiApiUrl);
        log.info("API Key present: {}", geminiApiKey != null && !geminiApiKey.isEmpty());

        while (retryCount < maxRetries) {
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
                log.debug("Calling Gemini API endpoint: {}", geminiApiUrl.replaceAll(":generateContent.*", ":generateContent"));

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    return extractTextFromResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to call Gemini API: " + response.getStatusCode());
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();

                // Check for rate limit errors
                if (errorMessage.contains("429") || errorMessage.contains("Too Many Requests") ||
                    errorMessage.contains("quota") || errorMessage.contains("RESOURCE_EXHAUSTED")) {

                    if (retryCount < maxRetries - 1) {
                        long waitTime = initialWait * (long) Math.pow(2, retryCount);
                        log.warn("Rate limit hit. Retry {} of {} after {}ms. Error: {}",
                            retryCount + 1, maxRetries, waitTime, errorMessage);

                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Question generation interrupted", ie);
                        }
                        retryCount++;
                        continue;
                    } else {
                        log.error("Rate limit exceeded after {} retries. Please try again later.", maxRetries);
                        throw new RuntimeException("Gemini API rate limit exceeded. Please wait a moment and try again.");
                    }
                } else if (errorMessage.contains("503") || errorMessage.contains("Service Unavailable")) {
                    // Service temporarily unavailable, retry with backoff
                    if (retryCount < maxRetries - 1) {
                        long waitTime = initialWait * (long) Math.pow(2, retryCount);
                        log.warn("Gemini API temporarily unavailable. Retry {} of {} after {}ms",
                            retryCount + 1, maxRetries, waitTime);

                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Question generation interrupted", ie);
                        }
                        retryCount++;
                        continue;
                    }
                }

                log.error("Error calling Gemini API: {}", errorMessage, e);
                throw new RuntimeException("Failed to generate questions: " + errorMessage);
            }
        }

        throw new RuntimeException("Failed to generate questions after " + maxRetries + " retries");
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
        return "";
    }

    private List<Question> parseAndCreateQuestions(String jsonResponse, String moduleType, Language language, String createdBy, Integer count) {
        List<Question> questions = new ArrayList<>();
        try {
            String cleanJson = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> questionsList = mapper.readValue(cleanJson, List.class);

            int questionNumber = 1;
            for (Map<String, Object> q : questionsList) {
                Question question = Question.builder()
                        .language(language)
                        .moduleType(Question.ModuleType.valueOf(moduleType))
                        .cefrLevel("INTERMEDIATE")
                        .questionText((String) q.get("questionText"))
                        .questionNumber(questionNumber++)
                        .status(Question.QuestionStatus.PENDING_REVIEW)
                        .generatedAt(LocalDateTime.now())
                        .generatedBy("GEMINI_API")
                        .build();

                if (q.containsKey("options")) {
                    List<String> options = (List<String>) q.get("options");
                    question.setQuestionOptions(String.join(",", options));
                }

                if (q.containsKey("correctAnswer")) {
                    question.setCorrectAnswer((String) q.get("correctAnswer"));
                }

                if (q.containsKey("explanation")) {
                    question.setExplanation((String) q.get("explanation"));
                }

                questions.add(question);
            }
        } catch (Exception e) {
            log.error("Error parsing generated questions: {}", e.getMessage(), e);
        }

        return questions;
    }
}
