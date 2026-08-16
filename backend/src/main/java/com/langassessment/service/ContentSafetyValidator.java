package com.langassessment.service;

import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class ContentSafetyValidator {

    private static final Set<String> HARMFUL_KEYWORDS = new HashSet<>(Arrays.asList(
        // Violence related
        "kill", "murder", "assassinate", "violence", "gore", "graphic", "stab", "shoot", "bomb",
        "torture", "rape", "abuse", "brutality", "bloodshed", "slaughter", "massacre",

        // Sexual content
        "porn", "nude", "sex", "xxx", "erotic", "adult", "orgasm", "vagina", "penis",
        "masturbate", "sexual intercourse", "incest", "pedophil",

        // Hate speech and discrimination
        "racist", "sexist", "homophobic", "transphobic", "islamophobic", "antisemitic",
        "nigger", "faggot", "terrorist", "subhuman", "inferior",

        // Dangerous activities
        "illegal drug", "heroin", "cocaine", "methamphetamine", "suicide", "self-harm",
        "suicide bomb", "chemical weapon", "bioweapon", "how to make explosives",

        // Toxic/Abusive
        "kys", "kms", "hate you", "die", "f*** you", "b****",

        // Extreme political/religious controversy
        "jihad", "infidel", "communist plot", "white supremacy", "neo-nazi"
    ));

    private static final Pattern VIOLENCE_PATTERN = Pattern.compile(
        "(?i)(kill|murder|stab|shoot|bomb|torture|violence|gore|attack|destroy|eliminate|execute)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SEXUAL_PATTERN = Pattern.compile(
        "(?i)(porn|sex|nude|xxx|erotic|sexual|adult|orgasm|incest|pedophil)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HATE_SPEECH_PATTERN = Pattern.compile(
        "(?i)(racist|sexist|nigger|faggot|terrorist|subhuman|inferior|hate|bigot)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "(?i)(illegal drug|heroin|cocaine|bomb|weapon|suicide|self-harm|how to make|manufacture)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TOXIC_PATTERN = Pattern.compile(
        "(?i)(kys|kms|die|fuck|bitch|asshole|idiot|stupid|dumb|worthless|loser)",
        Pattern.CASE_INSENSITIVE
    );

    public static class SafetyCheckResult {
        public boolean isSafe;
        public String category;
        public String reason;
        public String flaggedContent;

        public SafetyCheckResult(boolean isSafe, String category, String reason, String flaggedContent) {
            this.isSafe = isSafe;
            this.category = category;
            this.reason = reason;
            this.flaggedContent = flaggedContent;
        }
    }

    public static SafetyCheckResult validateQuestion(String questionText, List<String> options,
                                                      String correctAnswer, String explanation) {
        // Check question text
        SafetyCheckResult textCheck = checkContent(questionText, "Question Text");
        if (!textCheck.isSafe) {
            return textCheck;
        }

        // Check options
        if (options != null) {
            for (String option : options) {
                SafetyCheckResult optionCheck = checkContent(option, "Question Option");
                if (!optionCheck.isSafe) {
                    return optionCheck;
                }
            }
        }

        // Check correct answer
        if (correctAnswer != null) {
            SafetyCheckResult answerCheck = checkContent(correctAnswer, "Correct Answer");
            if (!answerCheck.isSafe) {
                return answerCheck;
            }
        }

        // Check explanation
        if (explanation != null) {
            SafetyCheckResult explanationCheck = checkContent(explanation, "Explanation");
            if (!explanationCheck.isSafe) {
                return explanationCheck;
            }
        }

        return new SafetyCheckResult(true, null, "Content passed all safety checks", null);
    }

    private static SafetyCheckResult checkContent(String content, String contentType) {
        if (content == null || content.trim().isEmpty()) {
            return new SafetyCheckResult(true, null, "Empty content", null);
        }

        String lowerContent = content.toLowerCase();

        // Check for violence
        if (VIOLENCE_PATTERN.matcher(content).find()) {
            String reason = String.format("%s contains violent content", contentType);
            log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
            return new SafetyCheckResult(false, "VIOLENCE", reason, extractFlaggedPortion(content, VIOLENCE_PATTERN));
        }

        // Check for sexual content
        if (SEXUAL_PATTERN.matcher(content).find()) {
            String reason = String.format("%s contains sexual content", contentType);
            log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
            return new SafetyCheckResult(false, "SEXUAL", reason, extractFlaggedPortion(content, SEXUAL_PATTERN));
        }

        // Check for hate speech
        if (HATE_SPEECH_PATTERN.matcher(content).find()) {
            String reason = String.format("%s contains hate speech or discriminatory language", contentType);
            log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
            return new SafetyCheckResult(false, "HATE_SPEECH", reason, extractFlaggedPortion(content, HATE_SPEECH_PATTERN));
        }

        // Check for dangerous content
        if (DANGEROUS_PATTERN.matcher(content).find()) {
            String reason = String.format("%s contains dangerous or illegal activity references", contentType);
            log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
            return new SafetyCheckResult(false, "DANGEROUS", reason, extractFlaggedPortion(content, DANGEROUS_PATTERN));
        }

        // Check for toxic content
        if (TOXIC_PATTERN.matcher(content).find()) {
            String reason = String.format("%s contains toxic or abusive language", contentType);
            log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
            return new SafetyCheckResult(false, "TOXIC", reason, extractFlaggedPortion(content, TOXIC_PATTERN));
        }

        // Check for harmful keywords
        for (String keyword : HARMFUL_KEYWORDS) {
            if (lowerContent.contains(keyword)) {
                String reason = String.format("%s contains potentially harmful keyword: '%s'", contentType, keyword);
                log.warn("SAFETY_VIOLATION: {} - {}", reason, content.substring(0, Math.min(50, content.length())));
                return new SafetyCheckResult(false, "HARMFUL_KEYWORD", reason, keyword);
            }
        }

        return new SafetyCheckResult(true, null, "Content passed safety checks", null);
    }

    private static String extractFlaggedPortion(String content, Pattern pattern) {
        var matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return content.substring(0, Math.min(30, content.length()));
    }
}
