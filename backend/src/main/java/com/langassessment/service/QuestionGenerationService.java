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

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
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

    private String callGeminiAPI(String prompt) {
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
            } else {
                log.error("Gemini API error: {}", response.getStatusCode());
                throw new RuntimeException("Failed to call Gemini API");
            }
        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate questions: " + e.getMessage());
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
