package com.langassessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class EdgeTextToSpeechService implements TextToSpeechService {

    private final RestTemplate restTemplate;
    private final MinIOService minIOService;

    // EdgeTTS Python backend - can be run locally or on a separate server
    private static final String EDGE_TTS_API_URL = "http://localhost:5001/tts";

    @Override
    public String synthesizeAndStore(String text, String languageCode, Optional<String> voiceName) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("EdgeTTS API not available. Start EdgeTTS server or use Google TTS");
        }

        try {
            String voice = voiceName.orElseGet(() -> getDefaultVoiceForLanguage(languageCode));

            // Call EdgeTTS API
            byte[] audioBytes = callEdgeTTS(text, voice);

            // Upload to MinIO
            String audioUrl = uploadAudioToMinIO(audioBytes, languageCode);

            log.info("Generated and stored audio via EdgeTTS for text (length: {}): {}", text.length(), audioUrl);
            return audioUrl;
        } catch (Exception e) {
            log.error("EdgeTTS failed, falling back or retrying: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Quick health check to see if EdgeTTS server is running
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> request = new HashMap<>();
            request.put("text", "test");
            request.put("voice", "en-US-AriaNeural");

            HttpEntity<String> entity = new HttpEntity<>(
                new ObjectMapper().writeValueAsString(request),
                headers
            );

            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                EDGE_TTS_API_URL,
                entity,
                byte[].class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("EdgeTTS service not available: {}", e.getMessage());
            return false;
        }
    }

    private byte[] callEdgeTTS(String text, String voiceName) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("text", text);
        request.put("voice", voiceName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(
            new ObjectMapper().writeValueAsString(request),
            headers
        );

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(
                EDGE_TTS_API_URL,
                entity,
                byte[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new Exception("EdgeTTS API returned non-successful status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to call EdgeTTS API: {}", e.getMessage(), e);
            throw new Exception("EdgeTTS API error: " + e.getMessage(), e);
        }
    }

    private String uploadAudioToMinIO(byte[] audioBytes, String languageCode) throws Exception {
        String fileName = System.currentTimeMillis() + "_" + languageCode + ".mp3";
        return minIOService.uploadAudio(audioBytes, fileName);
    }

    private String getDefaultVoiceForLanguage(String languageCode) {
        return switch (languageCode) {
            case "en-US" -> "en-US-AriaNeural";
            case "en-GB" -> "en-GB-SoniaNeural";
            case "es-ES" -> "es-ES-AlvaroNeural";
            case "es-MX" -> "es-MX-JorgeNeural";
            case "fr-FR" -> "fr-FR-DeniseNeural";
            case "de-DE" -> "de-DE-ConradNeural";
            case "pt-BR" -> "pt-BR-AntonioNeural";
            case "it-IT" -> "it-IT-DiegoNeural";
            case "ja-JP" -> "ja-JP-NanamiNeural";
            case "zh-CN" -> "zh-CN-XiaoxiaoNeural";
            default -> "en-US-AriaNeural";
        };
    }
}
