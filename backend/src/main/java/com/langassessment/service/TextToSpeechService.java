package com.langassessment.service;

import java.util.Optional;

public interface TextToSpeechService {
    /**
     * Generate audio from text and return the file URL/path
     * @param text Text to convert to speech
     * @param languageCode Language code (e.g., "en-US", "es-ES")
     * @param voiceName Optional voice name preference
     * @return URL/path of the generated audio file
     */
    String synthesizeAndStore(String text, String languageCode, Optional<String> voiceName) throws Exception;

    /**
     * Check if the TTS service is available
     */
    boolean isAvailable();
}
