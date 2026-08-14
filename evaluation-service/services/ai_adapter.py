import logging
import os
import json
from typing import Dict, List, Optional
import google.generativeai as genai

logger = logging.getLogger(__name__)

class AIModelAdapter:
    """
    Adapter for integrating multiple AI models for language evaluation.
    Currently supports Google Gemini API.
    """

    def __init__(self):
        self.gemini_api_key = os.getenv('GEMINI_API_KEY')
        if self.gemini_api_key:
            genai.configure(api_key=self.gemini_api_key)
        self.model = genai.GenerativeModel('gemini-pro')
        self.usage_stats = {
            'total_calls': 0,
            'total_tokens_used': 0,
            'total_cost': 0.0
        }

    def evaluate_comprehension(self, answers: List[Dict], questions: List[Dict]) -> Dict:
        """
        Evaluate comprehension based on answers to questions.
        """
        try:
            prompt = self._create_comprehension_prompt(answers, questions)
            response = self.model.generate_content(prompt)

            reasoning = response.text
            score = self._extract_score_from_response(reasoning)

            self._record_usage(response)

            return {
                'score': score,
                'reasoning': reasoning,
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Comprehension evaluation failed: {str(e)}")
            return {'score': 0, 'reasoning': str(e), 'model': 'gemini-pro'}

    def analyze_reading(self, text: str, questions: List[Dict], answers: List[Dict]) -> Dict:
        """
        Analyze reading comprehension.
        """
        try:
            prompt = f"""Analyze the following reading comprehension:

Text: {text}

Questions: {json.dumps(questions)}
Answers: {json.dumps(answers)}

Provide:
1. Comprehension score (0-100)
2. Vocabulary level assessment
3. Detailed reasoning

Format your response as JSON."""

            response = self.model.generate_content(prompt)
            result = self._parse_json_response(response.text)

            self._record_usage(response)

            return {
                'comprehension_score': result.get('comprehension_score', 0),
                'vocabulary_level': result.get('vocabulary_level', 'Unknown'),
                'reasoning': result.get('reasoning', response.text),
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Reading analysis failed: {str(e)}")
            return {
                'comprehension_score': 0,
                'vocabulary_level': 'Unknown',
                'reasoning': str(e),
                'model': 'gemini-pro'
            }

    def analyze_writing(self, text: str, prompt: str) -> Dict:
        """
        Analyze written text quality.
        """
        try:
            evaluation_prompt = f"""Evaluate the following written text based on:
- Grammar accuracy
- Vocabulary range
- Coherence and organization
- Task completion

Prompt: {prompt}
Text: {text}

Provide scores (0-100) for each criterion and overall score.
Format response as JSON with keys: grammar_score, vocabulary_score, coherence_score, task_completion_score, overall_score"""

            response = self.model.generate_content(evaluation_prompt)
            result = self._parse_json_response(response.text)

            self._record_usage(response)

            return {
                'grammar_score': result.get('grammar_score', 0),
                'vocabulary_score': result.get('vocabulary_score', 0),
                'coherence_score': result.get('coherence_score', 0),
                'task_completion_score': result.get('task_completion_score', 0),
                'overall_score': result.get('overall_score', 0),
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Writing analysis failed: {str(e)}")
            return {
                'grammar_score': 0,
                'vocabulary_score': 0,
                'coherence_score': 0,
                'task_completion_score': 0,
                'overall_score': 0,
                'model': 'gemini-pro'
            }

    def analyze_speech(self, audio_file, transcript: str) -> Dict:
        """
        Analyze spoken language quality.
        """
        try:
            prompt = f"""Analyze the following speech transcript:

Transcript: {transcript}

Evaluate:
1. Accuracy (grammar and vocabulary usage)
2. Fluency (speech flow and naturalness)
3. Vocabulary range
4. Grammar correctness

Provide scores (0-1.0) for each criterion.
Format response as JSON."""

            response = self.model.generate_content(prompt)
            result = self._parse_json_response(response.text)

            self._record_usage(response)

            return {
                'accuracy': result.get('accuracy', 0) * 100,
                'fluency': result.get('fluency', 0),
                'vocabulary_score': result.get('vocabulary_range', 0) * 100,
                'grammar_score': result.get('grammar', 0) * 100,
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Speech analysis failed: {str(e)}")
            return {
                'accuracy': 0,
                'fluency': 0,
                'vocabulary_score': 0,
                'grammar_score': 0,
                'model': 'gemini-pro'
            }

    def generate_content(self, language: str, module_type: str,
                        cefr_level: str, count: int = 1) -> Dict:
        """
        Generate assessment content.
        """
        try:
            prompt = f"""Generate {count} unique {module_type.lower()} assessment question(s) for {cefr_level} level in {language}:

Requirements:
- Appropriate for {cefr_level} CEFR level
- Clear and unambiguous
- Diverse content
- Include answer key

Format response as JSON with questions array."""

            response = self.model.generate_content(prompt)
            result = self._parse_json_response(response.text)

            self._record_usage(response)

            return {
                'questions': result.get('questions', []),
                'language': language,
                'module_type': module_type,
                'cefr_level': cefr_level,
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Content generation failed: {str(e)}")
            return {
                'questions': [],
                'error': str(e),
                'model': 'gemini-pro'
            }

    def translate_content(self, content: str, source_language: str,
                         target_language: str) -> Dict:
        """
        Translate content to different languages.
        """
        try:
            prompt = f"""Translate the following {source_language} content to {target_language}.
Keep the meaning and context intact.

Content: {content}

Provide the translation directly."""

            response = self.model.generate_content(prompt)

            self._record_usage(response)

            return {
                'original': content,
                'translation': response.text,
                'source_language': source_language,
                'target_language': target_language,
                'model': 'gemini-pro'
            }
        except Exception as e:
            logger.error(f"Translation failed: {str(e)}")
            return {
                'error': str(e),
                'model': 'gemini-pro'
            }

    def list_available_models(self) -> List[str]:
        """List available AI models."""
        return ['gemini-pro', 'gemini-pro-vision']

    def get_usage_stats(self) -> Dict:
        """Get API usage statistics."""
        return self.usage_stats

    def _create_comprehension_prompt(self, answers: List[Dict], questions: List[Dict]) -> str:
        """Create a prompt for comprehension evaluation."""
        return f"""Evaluate the comprehension quality of these answers:

Questions: {json.dumps(questions)}
Answers: {json.dumps(answers)}

Provide:
1. Score (0-100)
2. Reasoning
3. Areas for improvement"""

    def _parse_json_response(self, response_text: str) -> Dict:
        """Parse JSON from AI response."""
        try:
            # Try to extract JSON from response
            if '{' in response_text and '}' in response_text:
                start = response_text.find('{')
                end = response_text.rfind('}') + 1
                json_str = response_text[start:end]
                return json.loads(json_str)
        except Exception as e:
            logger.warning(f"Failed to parse JSON response: {str(e)}")
        return {}

    def _extract_score_from_response(self, response_text: str) -> float:
        """Extract numerical score from response."""
        try:
            result = self._parse_json_response(response_text)
            if 'score' in result:
                return float(result['score'])
        except Exception as e:
            logger.warning(f"Failed to extract score: {str(e)}")
        return 50.0  # Default to neutral score

    def _record_usage(self, response) -> None:
        """Record API usage for billing."""
        self.usage_stats['total_calls'] += 1
        # Note: Gemini API doesn't return token count in standard response
        # This would need to be calculated or obtained from the API
