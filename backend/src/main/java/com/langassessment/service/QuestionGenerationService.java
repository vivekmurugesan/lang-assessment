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
    private final AudioGenerationService audioGenerationService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent}")
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

        // Trigger async audio generation for LISTENING questions
        log.info("🎵 Triggering ASYNC audio generation for LISTENING questions...");
        long listeningCount = generatedQuestions.stream()
            .filter(q -> q.getModuleType().toString().equals("LISTENING"))
            .count();
        log.info("🎵 Total LISTENING questions: {}", listeningCount);

        audioGenerationService.generateAudioForQuestions(generatedQuestions);
        log.info("🎵 Audio generation task submitted (async - will run in background)");

        return generatedQuestions;
    }

    private List<Question> generateQuestionsForModule(Assessment assessment, String moduleType, Integer count, Language language, String createdBy) {
        String prompt = buildPrompt(assessment, moduleType, count, language.getName());
        String response = callGeminiAPI(prompt);
        return parseAndCreateQuestions(response, moduleType, language, createdBy, count);
    }

    private String buildPrompt(Assessment assessment, String moduleType, Integer count, String languageName) {
        String cefrLevel = "B1";
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

                IMPORTANT FORMATTING RULES:
                - DO NOT include CEFR level (like "B1", "A2") anywhere in the question text
                - DO NOT mention the question type or CEFR level in the question
                - For WRITING and SPOKEN modules: DO NOT include multiple-choice options - these are open-ended
                - For READING and LISTENING modules: Include exactly 4 multiple-choice options (A, B, C, D)

                IMPORTANT - CONTENT SAFETY GUIDELINES:
                Questions MUST be:
                ✓ Educational and appropriate for language learners
                ✓ Non-violent and free from graphic content
                ✓ Non-sexual and professional in nature
                ✓ Respectful to all cultures, religions, and groups
                ✓ Free from hate speech or discriminatory language
                ✓ Not promoting dangerous, illegal, or harmful activities
                ✓ Non-controversial and not pushing political agendas
                ✓ Free from toxic or abusive language
                ✓ Not containing explicit, profane, or offensive content

                Questions MUST NOT:
                ✗ Contain violence, gore, or graphic descriptions
                ✗ Contain sexual, erotic, or adult content
                ✗ Promote harmful, illegal, or dangerous activities
                ✗ Contain hate speech, slurs, or discriminatory content
                ✗ Discuss sensitive political or religious controversies
                ✗ Contain toxic, abusive, or bullying language
                ✗ Reference hard drugs, weapons, or illegal activities
                ✗ Contain explicit or profane language

                Return ONLY a JSON array with format:
                [
                  {
                    "questionText": "Question here",
                    "type": "multiple-choice|short-answer|essay",
                    "options": ["Full text of option A", "Full text of option B", "Full text of option C", "Full text of option D"] (if multiple choice),
                    "correctAnswer": "A" (if multiple choice, use letter; for short-answer/essay: the full correct answer text),
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
            case "LISTENING" -> "- Each question should have corresponding audio content\n- Create 4 multiple-choice options (A, B, C, D)";
            case "READING" -> "- Base questions on short text passages\n- Create 4 multiple-choice options (A, B, C, D)";
            case "WRITING" -> "- Provide writing prompts (NOT multiple-choice) that ask for open-ended text responses\n- Include context and word count guidance if appropriate\n- Mark type as 'essay', NOT 'multiple-choice'\n- Leave options array empty";
            case "SPOKEN_INTERACTION" -> "- Create realistic conversational scenarios where candidate responds to a prompt\n- NOT multiple-choice - candidate will record their response\n- Give clear context and what the candidate should do\n- Mark type as 'short-answer', NOT 'multiple-choice'\n- Leave options array empty";
            case "SPOKEN_PRODUCTION" -> "- Create speaking prompts for monologue or presentation\n- NOT multiple-choice - candidate will record their response\n- Give clear topic and time limit if appropriate\n- Mark type as 'essay', NOT 'multiple-choice'\n- Leave options array empty";
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

        int maxRetries = 5;
        int retryCount = 0;
        long initialWait = 2000; // 2 seconds for timeouts, 1 second for rate limits

        log.info("Attempting to generate questions using Gemini API");
        log.info("API URL: {}", geminiApiUrl);
        log.info("API Key present: {}", geminiApiKey != null && !geminiApiKey.isEmpty());
        log.info("Max retries: {}, Initial wait: {}ms", maxRetries, initialWait);

        while (retryCount < maxRetries) {
            try {
                log.debug("Attempt {} of {}", retryCount + 1, maxRetries);

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

                long startTime = System.currentTimeMillis();
                log.info("API call started...");

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                long duration = System.currentTimeMillis() - startTime;
                log.info("API call completed in {}ms", duration);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    log.info("Successfully received response from Gemini API");
                    return extractTextFromResponse(response.getBody());
                } else {
                    log.error("Gemini API error: {}", response.getStatusCode());
                    throw new RuntimeException("Failed to call Gemini API: " + response.getStatusCode());
                }
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                boolean isTimeout = errorMessage.contains("timeout") || errorMessage.contains("Timeout");
                boolean isRateLimit = errorMessage.contains("429") || errorMessage.contains("Too Many Requests") ||
                    errorMessage.contains("quota") || errorMessage.contains("RESOURCE_EXHAUSTED");
                boolean isServiceUnavailable = errorMessage.contains("503") || errorMessage.contains("Service Unavailable");

                if (isTimeout) {
                    if (retryCount < maxRetries - 1) {
                        long waitTime = initialWait * (long) Math.pow(2, retryCount);
                        log.warn("Timeout on Gemini API call ({}ms). Retry {} of {} after {}ms",
                            initialWait * (long) Math.pow(2, retryCount),
                            retryCount + 1, maxRetries, waitTime);

                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Question generation interrupted", ie);
                        }
                        retryCount++;
                        continue;
                    } else {
                        log.error("Timeout exceeded after {} retries. API is taking too long to respond.", maxRetries);
                        throw new RuntimeException("Gemini API request timed out after " + maxRetries + " retries. Please try again.");
                    }
                } else if (isRateLimit) {
                    // Check if this is a quota exhaustion (daily limit) rather than transient rate limit
                    if (errorMessage.contains("quota") && errorMessage.contains("free_tier")) {
                        log.error("=== QUOTA EXHAUSTION DETECTED ===");
                        log.error("Free tier daily quota (20 requests/day) exhausted.");
                        log.error("Skipping retry attempts - quota won't reset until next UTC day.");
                        log.error("To resolve, either:");
                        log.error("  1. Wait until tomorrow (quota resets daily at midnight UTC)");
                        log.error("  2. Use a paid API key (Google Cloud billing)");
                        log.error("  3. Create a new project with fresh quota");
                        log.error("=================================");
                        throw new RuntimeException("Gemini API free tier daily quota (20 requests/day) has been exceeded. Please try again tomorrow or upgrade to a paid plan.");
                    }

                    if (retryCount < maxRetries - 1) {
                        // Try to extract retry delay from API response (e.g., "Please retry in 4.615s")
                        long waitTime = extractRetryDelayFromError(errorMessage);

                        if (waitTime == 0) {
                            // Fallback to exponential backoff if no retry delay found
                            waitTime = 1000 * (long) Math.pow(2, retryCount); // 1s, 2s, 4s, 8s, 16s
                        }

                        log.warn("Rate limit hit on Gemini API. Retry {} of {} after {}ms. Error: {}",
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
                } else if (isServiceUnavailable) {
                    if (retryCount < maxRetries - 1) {
                        long waitTime = 2000 * (long) Math.pow(2, retryCount); // 2s, 4s, 8s, 16s, 32s
                        log.warn("Gemini API temporarily unavailable (503). Retry {} of {} after {}ms",
                            retryCount + 1, maxRetries, waitTime);

                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Question generation interrupted", ie);
                        }
                        retryCount++;
                        continue;
                    } else {
                        log.error("Service unavailable after {} retries. Please try again later.", maxRetries);
                        throw new RuntimeException("Gemini API is temporarily unavailable. Please try again later.");
                    }
                }

                log.error("Error calling Gemini API: {}", errorMessage, e);
                throw new RuntimeException("Failed to generate questions: " + errorMessage);
            }
        }

        throw new RuntimeException("Failed to generate questions after " + maxRetries + " retries");
    }

    private long extractRetryDelayFromError(String errorMessage) {
        try {
            // Try to extract "Please retry in X.XXs" or "retryDelay": "Xs" from error message
            // Format 1: "Please retry in 4.615208387s"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("Please retry in ([0-9.]+)s");
            java.util.regex.Matcher matcher = pattern.matcher(errorMessage);
            if (matcher.find()) {
                double seconds = Double.parseDouble(matcher.group(1));
                // Add 500ms buffer to ensure we wait long enough
                return (long) (seconds * 1000 + 500);
            }

            // Format 2: Look for "retryDelay": "4s" in JSON
            if (errorMessage.contains("\"retryDelay\"")) {
                java.util.regex.Pattern pattern2 = java.util.regex.Pattern.compile("\"retryDelay\"\\s*:\\s*\"([0-9]+)s\"");
                java.util.regex.Matcher matcher2 = pattern2.matcher(errorMessage);
                if (matcher2.find()) {
                    long seconds = Long.parseLong(matcher2.group(1));
                    return seconds * 1000 + 500; // Add 500ms buffer
                }
            }
        } catch (Exception e) {
            log.debug("Could not extract retry delay from error message: {}", e.getMessage());
        }
        return 0; // Return 0 to trigger exponential backoff fallback
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
        int rejectedCount = 0;

        try {
            String cleanJson = jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> questionsList = mapper.readValue(cleanJson, List.class);

            int questionNumber = 1;
            for (Map<String, Object> q : questionsList) {
                String questionText = (String) q.get("questionText");
                List<String> options = (List<String>) q.get("options");
                String correctAnswer = (String) q.get("correctAnswer");
                String explanation = (String) q.get("explanation");

                // Perform safety validation
                ContentSafetyValidator.SafetyCheckResult safetyResult =
                    ContentSafetyValidator.validateQuestion(questionText, options, correctAnswer, explanation);

                if (!safetyResult.isSafe) {
                    rejectedCount++;
                    log.error("Question rejected due to safety violation: Category={}, Reason={}, FlaggedContent={}",
                        safetyResult.category, safetyResult.reason, safetyResult.flaggedContent);
                    continue;
                }

                Question question = Question.builder()
                        .language(language)
                        .moduleType(Question.ModuleType.valueOf(moduleType))
                        .cefrLevel("B1")
                        .questionText(questionText)
                        .questionNumber(questionNumber++)
                        .status(Question.QuestionStatus.PENDING_REVIEW)
                        .generatedAt(LocalDateTime.now())
                        .generatedBy("GEMINI_API")
                        .catalogCategory("GENERAL")
                        .approvalStatus(Question.ApprovalStatus.PENDING_REVIEW)
                        .audioGenerationStatus(Question.ContentGenerationStatus.NOT_REQUIRED)
                        .optionsGenerationStatus(Question.ContentGenerationStatus.NOT_REQUIRED)
                        .build();

                // Determine if audio generation is needed for LISTENING module
                if ("LISTENING".equals(moduleType)) {
                    question.setAudioGenerationStatus(Question.ContentGenerationStatus.PENDING);
                }

                // Determine if options generation is needed for LISTENING and READING
                if ("LISTENING".equals(moduleType) || "READING".equals(moduleType)) {
                    // Store question options in MinIO
                    if (options != null && !options.isEmpty()) {
                        String optionsJson = mapper.writeValueAsString(options);
                        String optionsUri = uploadLargeContentToMinIO(optionsJson, "question-options");
                        if (optionsUri != null) {
                            question.setQuestionOptionsUri(optionsUri);
                            question.setOptionsGenerationStatus(Question.ContentGenerationStatus.GENERATED);
                        } else {
                            question.setOptionsGenerationStatus(Question.ContentGenerationStatus.FAILED);
                            question.setOptionsGenerationError("Failed to upload options to MinIO");
                        }
                    } else {
                        question.setOptionsGenerationStatus(Question.ContentGenerationStatus.PENDING);
                    }
                } else {
                    // Store question options in MinIO for other modules too
                    if (options != null && !options.isEmpty()) {
                        String optionsJson = mapper.writeValueAsString(options);
                        String optionsUri = uploadLargeContentToMinIO(optionsJson, "question-options");
                        if (optionsUri != null) {
                            question.setQuestionOptionsUri(optionsUri);
                        }
                    }
                }

                if (correctAnswer != null && !correctAnswer.isEmpty()) {
                    question.setCorrectAnswer(correctAnswer);
                }

                // Store explanation in MinIO
                if (explanation != null && !explanation.isEmpty()) {
                    String explanationUri = uploadLargeContentToMinIO(explanation, "question-explanation");
                    if (explanationUri != null) {
                        question.setExplanationUri(explanationUri);
                    }
                }

                questions.add(question);
            }

            if (rejectedCount > 0) {
                log.warn("Question generation complete: {} questions passed safety checks, {} rejected",
                    questions.size(), rejectedCount);
            }
        } catch (Exception e) {
            log.error("Error parsing generated questions: {}", e.getMessage(), e);
        }

        return questions;
    }

    private String uploadLargeContentToMinIO(String content, String contentType) {
        try {
            String objectName = String.format("questions/%s/%d-%s.json",
                    contentType,
                    System.currentTimeMillis(),
                    java.util.UUID.randomUUID().toString().substring(0, 8));

            byte[] contentBytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            java.io.InputStream inputStream = new java.io.ByteArrayInputStream(contentBytes);

            minIOService.uploadFile("questions", objectName, inputStream, contentBytes.length);
            String uri = minIOService.getObjectUrl("questions", objectName);

            log.info("Uploaded {} content to MinIO: {}", contentType, uri);
            return uri;
        } catch (Exception e) {
            log.error("Failed to upload {} content to MinIO: {}", contentType, e.getMessage(), e);
            return null;
        }
    }
}
