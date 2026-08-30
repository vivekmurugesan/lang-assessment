package com.langassessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${tts.edge.api.url:http://edge-tts:5001/tts}")
    private String edgeTtsApiUrl;

    @Override
    public String synthesizeAndStore(String text, String languageCode, Optional<String> voiceName) throws Exception {
        log.info("🎵 EdgeTTS synthesizeAndStore called for language: {}", languageCode);

        if (!isAvailable()) {
            log.error("❌ EdgeTTS API not available. Check if edge-tts service is running at {}", edgeTtsApiUrl);
            throw new IllegalStateException("EdgeTTS API not available. Start EdgeTTS server or use Google TTS");
        }

        try {
            String voice = voiceName.orElseGet(() -> getDefaultVoiceForLanguage(languageCode));
            log.info("🎤 Using voice: {}", voice);

            // Call EdgeTTS API
            log.info("📞 Calling EdgeTTS API for text (length: {})", text.length());
            byte[] audioBytes = callEdgeTTS(text, voice);
            log.info("✅ Received {} bytes from EdgeTTS", audioBytes.length);

            // Upload to MinIO
            log.info("⬆️  Uploading audio to MinIO...");
            String audioUrl = uploadAudioToMinIO(audioBytes, languageCode);
            log.info("✅ Successfully generated and stored audio via EdgeTTS: {}", audioUrl);

            return audioUrl;
        } catch (Exception e) {
            log.error("❌ EdgeTTS synthesis failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            log.info("🔍 Checking EdgeTTS service availability at: {}", edgeTtsApiUrl);

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
                edgeTtsApiUrl,
                entity,
                byte[].class
            );

            boolean available = response.getStatusCode().is2xxSuccessful();
            log.info("🔍 EdgeTTS availability check: {} (HTTP {})",
                available ? "✅ AVAILABLE" : "❌ NOT AVAILABLE",
                response.getStatusCode());

            return available;
        } catch (Exception e) {
            log.error("❌ EdgeTTS service not available: {} | URL: {}", e.getMessage(), edgeTtsApiUrl);
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
                edgeTtsApiUrl,
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
