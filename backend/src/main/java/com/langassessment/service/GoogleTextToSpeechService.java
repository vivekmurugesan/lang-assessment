package com.langassessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class GoogleTextToSpeechService implements TextToSpeechService {

    private final RestTemplate restTemplate;
    private final MinIOService minIOService;

    @Value("${tts.google.api.key:}")
    private String googleApiKey;

    @Value("${tts.google.api.url:https://texttospeech.googleapis.com/v1/text:synthesize}")
    private String googleApiUrl;

    @Override
    public String synthesizeAndStore(String text, String languageCode, Optional<String> voiceName) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("Google TTS API key not configured");
        }

        String voice = voiceName.orElseGet(() -> getDefaultVoiceForLanguage(languageCode));

        // Call Google TTS API
        byte[] audioBytes = callGoogleTTS(text, languageCode, voice);

        // Upload to MinIO
        String audioUrl = uploadAudioToMinIO(audioBytes, languageCode);

        log.info("Generated and stored audio for text (length: {}): {}", text.length(), audioUrl);
        return audioUrl;
    }

    @Override
    public boolean isAvailable() {
        return googleApiKey != null && !googleApiKey.isEmpty() && !googleApiKey.equals("DEMO");
    }

    private byte[] callGoogleTTS(String text, String languageCode, String voiceName) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("input", Map.of("text", text));
        request.put("voice", Map.of(
            "languageCode", languageCode,
            "name", voiceName
        ));
        request.put("audioConfig", Map.of("audioEncoding", "MP3"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
            new ObjectMapper().writeValueAsString(request),
            headers
        );

        try {
            String url = googleApiUrl + "?key=" + googleApiKey;
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("audioContent")) {
                String audioContent = (String) response.getBody().get("audioContent");
                return Base64.getDecoder().decode(audioContent);
            } else {
                throw new Exception("No audio content in Google TTS response");
            }
        } catch (Exception e) {
            log.error("Failed to call Google TTS API: {}", e.getMessage(), e);
            throw new Exception("Google TTS API error: " + e.getMessage(), e);
        }
    }

    private String uploadAudioToMinIO(byte[] audioBytes, String languageCode) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + languageCode + ".mp3";
        return minIOService.uploadAudio(audioBytes, fileName);
    }

    private String getDefaultVoiceForLanguage(String languageCode) {
        return switch (languageCode) {
            case "en-US" -> "en-US-Journey-F";
            case "en-GB" -> "en-GB-Standard-A";
            case "es-ES" -> "es-ES-Standard-A";
            case "es-MX" -> "es-MX-Standard-B";
            case "fr-FR" -> "fr-FR-Standard-C";
            case "de-DE" -> "de-DE-Standard-A";
            case "pt-BR" -> "pt-BR-Standard-A";
            case "it-IT" -> "it-IT-Standard-A";
            case "ja-JP" -> "ja-JP-Standard-A";
            case "zh-CN" -> "zh-CN-Standard-A";
            default -> "en-US-Journey-F";
        };
    }
}
