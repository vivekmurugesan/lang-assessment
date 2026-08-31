#!/usr/bin/env python3
"""
EdgeTTS Server - Lightweight free Text-to-Speech API wrapper
Uses Microsoft Edge's TTS engine (no authentication required)
Perfect for development and testing
"""

import asyncio
import io
import logging
from flask import Flask, request, jsonify
import edge_tts

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Available voices by language
VOICES = {
    "en-US": ["en-US-AriaNeural", "en-US-GuyNeural", "en-US-JennyNeural"],
    "en-GB": ["en-GB-SoniaNeural", "en-GB-RyanNeural", "en-GB-LibbyNeural"],
    "es-ES": ["es-ES-AlvaroNeural", "es-ES-ElviraNeural"],
    "es-MX": ["es-MX-JorgeNeural", "es-MX-DaliaNeural"],
    "fr-FR": ["fr-FR-DeniseNeural", "fr-FR-HenriNeural"],
    "de-DE": ["de-DE-ConradNeural", "de-DE-KatjaNeural"],
    "pt-BR": ["pt-BR-AntonioNeural", "pt-BR-FranciscaNeural"],
    "it-IT": ["it-IT-DiegoNeural", "it-IT-IsabellaNeural"],
    "ja-JP": ["ja-JP-NanamiNeural", "ja-JP-KeitaNeural"],
    "zh-CN": ["zh-CN-XiaoxiaoNeural", "zh-CN-YunyangNeural"],
}


@app.route("/health", methods=["GET"])
def health():
    """Health check endpoint"""
    return jsonify({"status": "healthy", "service": "EdgeTTS"}), 200


@app.route("/voices", methods=["GET"])
def list_voices():
    """List available voices"""
    return jsonify({"voices": VOICES}), 200


@app.route("/tts", methods=["POST"])
def text_to_speech():
    """Convert text to speech"""
    try:
        data = request.get_json()

        if not data or "text" not in data:
            return jsonify({"error": "Missing 'text' parameter"}), 400

        text = data.get("text", "").strip()
        voice = data.get("voice", "en-US-AriaNeural")
        rate = data.get("rate", "+0%")

        if not text:
            return jsonify({"error": "Text cannot be empty"}), 400

        if len(text) > 5000:
            return jsonify({"error": "Text too long (max 5000 characters)"}), 400

        logger.info(f"Generating audio for: {text[:50]}... (voice: {voice}, rate: {rate})")

        # Generate audio asynchronously
        audio_bytes = asyncio.run(generate_audio(text, voice, rate))

        # Return as MP3 binary
        return audio_bytes, 200, {"Content-Type": "audio/mpeg"}

    except Exception as e:
        logger.error(f"TTS error: {str(e)}", exc_info=True)
        return jsonify({"error": str(e)}), 500


async def generate_audio(text: str, voice: str, rate: str) -> bytes:
    """Generate audio using EdgeTTS"""
    try:
        communicate = edge_tts.Communicate(text=text, voice=voice, rate=rate)
        audio_buffer = io.BytesIO()

        async for chunk in communicate.stream():
            if chunk["type"] == "audio":
                audio_buffer.write(chunk["data"])

        audio_buffer.seek(0)
        logger.info(f"✅ Audio generated successfully for voice: {voice}")
        return audio_buffer.getvalue()
    except Exception as e:
        logger.error(f"❌ EdgeTTS generation failed: {str(e)}", exc_info=True)
        raise


@app.route("/", methods=["GET"])
def index():
    """API documentation"""
    return jsonify({
        "name": "EdgeTTS Server",
        "description": "Free Text-to-Speech API using Microsoft Edge TTS",
        "endpoints": {
            "/health": "Health check",
            "/voices": "List available voices",
            "/tts": "Convert text to speech (POST)"
        },
        "usage": {
            "endpoint": "POST /tts",
            "body": {
                "text": "Text to convert to speech",
                "voice": "en-US-AriaNeural (optional, defaults to en-US-AriaNeural)",
                "rate": "+0% (optional, format: +10%, -10%, etc. defaults to +0%)"
            },
            "response": "MP3 audio binary",
            "example": {
                "text": "Hello, this is a test",
                "voice": "en-US-AriaNeural"
            }
        },
        "supported_languages": list(VOICES.keys())
    }), 200


if __name__ == "__main__":
    logger.info("Starting EdgeTTS Server on http://0.0.0.0:5001")
    app.run(host="0.0.0.0", port=5001, debug=False)
