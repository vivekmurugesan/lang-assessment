import logging
import json
from typing import Dict, List, Optional
from enum import Enum
import numpy as np
from .ai_adapter import AIModelAdapter

logger = logging.getLogger(__name__)

class CEFRLevel(Enum):
    A1 = "A1"
    A2 = "A2"
    B1 = "B1"
    B2 = "B2"
    C1 = "C1"
    C2 = "C2"

class EvaluationEngine:
    """
    Core evaluation engine for assessing language proficiency based on CEFR levels.
    Combines rule-based and AI-driven evaluation methods.
    """

    def __init__(self):
        self.ai_adapter = AIModelAdapter()
        self.metrics_config = self._load_metrics_config()

    def _load_metrics_config(self) -> Dict:
        """Load evaluation metrics configuration"""
        return {
            'listening': {
                'A1': ['vocabulary_recognition', 'comprehension', 'response_accuracy'],
                'B2': ['advanced_vocabulary', 'implicit_meaning', 'detailed_comprehension', 'response_accuracy']
            },
            'reading': {
                'A1': ['basic_vocabulary', 'simple_text_comprehension', 'information_extraction'],
                'B2': ['complex_vocabulary', 'implicit_meaning', 'detailed_analysis', 'critical_comprehension']
            },
            'writing': {
                'A1': ['vocabulary_usage', 'grammar_accuracy', 'coherence', 'spelling'],
                'B2': ['vocabulary_range', 'grammar_complexity', 'coherence_organization', 'tone_style', 'spelling_punctuation']
            },
            'speaking': {
                'A1': ['pronunciation', 'fluency', 'vocabulary', 'grammar'],
                'B2': ['pronunciation_intonation', 'fluency_coherence', 'vocabulary_idiom', 'grammar', 'discourse_management']
            }
        }

    def evaluate_listening(self, submission: Dict) -> Dict:
        """
        Evaluate listening section submission.
        """
        try:
            cefr_level = submission.get('cefr_level', 'B1')
            questions = submission.get('questions', [])
            answers = submission.get('answers', [])

            # Calculate accuracy score
            accuracy_score = self._calculate_accuracy(answers)

            # AI-based evaluation for comprehension
            comprehension_result = self.ai_adapter.evaluate_comprehension(answers, questions)

            # Combine scores
            combined_score = self._combine_scores(
                accuracy_score=accuracy_score,
                comprehension_score=comprehension_result['score'],
                weight_accuracy=0.5,
                weight_comprehension=0.5
            )

            # Determine CEFR level based on score
            determined_level = self._map_score_to_cefr(combined_score)

            return {
                'module': 'LISTENING',
                'score': combined_score,
                'max_score': 100,
                'cefr_level': determined_level,
                'metrics': {
                    'accuracy': accuracy_score,
                    'comprehension': comprehension_result['score'],
                    'reasoning': comprehension_result['reasoning']
                },
                'feedback': self._generate_feedback('listening', determined_level, combined_score)
            }
        except Exception as e:
            logger.error(f"Listening evaluation error: {str(e)}")
            raise

    def evaluate_reading(self, submission: Dict) -> Dict:
        """
        Evaluate reading section submission.
        """
        try:
            text = submission.get('text', '')
            questions = submission.get('questions', [])
            answers = submission.get('answers', [])

            # Calculate accuracy
            accuracy_score = self._calculate_accuracy(answers)

            # AI-based text analysis
            text_analysis = self.ai_adapter.analyze_reading(
                text=text,
                questions=questions,
                answers=answers
            )

            # Combine scores
            combined_score = self._combine_scores(
                accuracy_score=accuracy_score,
                comprehension_score=text_analysis['comprehension_score'],
                weight_accuracy=0.4,
                weight_comprehension=0.6
            )

            determined_level = self._map_score_to_cefr(combined_score)

            return {
                'module': 'READING',
                'score': combined_score,
                'max_score': 100,
                'cefr_level': determined_level,
                'metrics': {
                    'accuracy': accuracy_score,
                    'vocabulary_level': text_analysis['vocabulary_level'],
                    'comprehension': text_analysis['comprehension_score'],
                    'reasoning': text_analysis['reasoning']
                },
                'feedback': self._generate_feedback('reading', determined_level, combined_score)
            }
        except Exception as e:
            logger.error(f"Reading evaluation error: {str(e)}")
            raise

    def evaluate_writing(self, submission: Dict) -> Dict:
        """
        Evaluate writing section submission.
        """
        try:
            text = submission.get('text', '')
            question = submission.get('question', '')

            # AI-based writing analysis
            writing_analysis = self.ai_adapter.analyze_writing(
                text=text,
                prompt=question
            )

            score = writing_analysis['overall_score']
            determined_level = self._map_score_to_cefr(score)

            return {
                'module': 'WRITING',
                'score': score,
                'max_score': 100,
                'cefr_level': determined_level,
                'metrics': {
                    'grammar_accuracy': writing_analysis['grammar_score'],
                    'vocabulary_range': writing_analysis['vocabulary_score'],
                    'coherence': writing_analysis['coherence_score'],
                    'task_completion': writing_analysis['task_completion_score']
                },
                'feedback': self._generate_feedback('writing', determined_level, score)
            }
        except Exception as e:
            logger.error(f"Writing evaluation error: {str(e)}")
            raise

    def evaluate_speaking(self, submission: Dict, audio_file) -> Dict:
        """
        Evaluate speaking section submission.
        """
        try:
            # Process audio file
            audio_features = self._extract_audio_features(audio_file)

            # AI-based speech analysis
            speech_analysis = self.ai_adapter.analyze_speech(
                audio_file=audio_file,
                transcript=submission.get('transcript', '')
            )

            # Combine audio features and speech analysis
            score = self._combine_scores(
                accuracy_score=speech_analysis['accuracy'],
                comprehension_score=speech_analysis['fluency'] * 100,
                weight_accuracy=0.3,
                weight_comprehension=0.7
            )

            determined_level = self._map_score_to_cefr(score)

            return {
                'module': 'SPOKEN_PRODUCTION',
                'score': score,
                'max_score': 100,
                'cefr_level': determined_level,
                'metrics': {
                    'pronunciation': audio_features.get('pronunciation_score', 0),
                    'fluency': speech_analysis['fluency'],
                    'vocabulary': speech_analysis['vocabulary_score'],
                    'grammar': speech_analysis['grammar_score']
                },
                'feedback': self._generate_feedback('speaking', determined_level, score)
            }
        except Exception as e:
            logger.error(f"Speaking evaluation error: {str(e)}")
            raise

    def evaluate_batch(self, submissions: List[Dict]) -> List[Dict]:
        """
        Batch evaluate multiple submissions.
        """
        results = []
        for submission in submissions:
            module_type = submission.get('module_type', '').lower()
            try:
                if module_type == 'listening':
                    result = self.evaluate_listening(submission)
                elif module_type == 'reading':
                    result = self.evaluate_reading(submission)
                elif module_type == 'writing':
                    result = self.evaluate_writing(submission)
                elif module_type in ['speaking', 'spoken_production']:
                    result = self.evaluate_speaking(submission, None)
                else:
                    result = {'error': f'Unknown module type: {module_type}'}
                results.append(result)
            except Exception as e:
                results.append({'error': str(e), 'submission_id': submission.get('id')})
        return results

    def _calculate_accuracy(self, answers: List[Dict]) -> float:
        """Calculate accuracy based on correct answers."""
        if not answers:
            return 0.0
        correct = sum(1 for ans in answers if ans.get('is_correct', False))
        return (correct / len(answers)) * 100

    def _combine_scores(self, accuracy_score: float, comprehension_score: float,
                       weight_accuracy: float, weight_comprehension: float) -> float:
        """Combine multiple scores with weights."""
        return (accuracy_score * weight_accuracy) + (comprehension_score * weight_comprehension)

    def _map_score_to_cefr(self, score: float) -> str:
        """Map numerical score to CEFR level."""
        if score < 20:
            return 'A1'
        elif score < 40:
            return 'A2'
        elif score < 60:
            return 'B1'
        elif score < 80:
            return 'B2'
        elif score < 90:
            return 'C1'
        else:
            return 'C2'

    def _extract_audio_features(self, audio_file) -> Dict:
        """Extract audio features for pronunciation analysis."""
        return {
            'pronunciation_score': 0,
            'clarity_score': 0,
            'pace_score': 0
        }

    def _generate_feedback(self, module_type: str, level: str, score: float) -> str:
        """Generate feedback for the candidate."""
        feedback_templates = {
            'listening': {
                'A1': 'You can understand basic, familiar words and simple sentences about common topics.',
                'A2': 'You can understand main points in simple, clear messages about familiar topics.',
                'B1': 'You can understand the main points of clear messages about familiar topics.',
                'B2': 'You can understand extended speech and complex information about various topics.',
                'C1': 'You can understand almost all types of spoken language, including implied meaning.',
                'C2': 'You have complete mastery of spoken language.'
            },
            'reading': {
                'A1': 'You can understand basic texts about familiar topics.',
                'A2': 'You can understand simple texts with common vocabulary.',
                'B1': 'You can understand articles and reports about familiar topics.',
                'B2': 'You can understand complex texts and varied texts on various topics.',
                'C1': 'You can understand implicit meaning and complex texts.',
                'C2': 'You have complete mastery of written language.'
            },
            'writing': {
                'A1': 'You can write simple, short messages.',
                'A2': 'You can write simple texts on familiar topics.',
                'B1': 'You can write clear texts on various topics.',
                'B2': 'You can write well-structured texts with varied vocabulary.',
                'C1': 'You can write complex, well-structured texts.',
                'C2': 'You have complete mastery of written expression.'
            },
            'speaking': {
                'A1': 'You can speak about simple topics using basic vocabulary.',
                'A2': 'You can speak about familiar topics with simple language.',
                'B1': 'You can speak about various topics with some hesitation.',
                'B2': 'You can speak fluently and spontaneously on various topics.',
                'C1': 'You can speak with fluency, spontaneity, and precision.',
                'C2': 'You have complete mastery of spoken expression.'
            }
        }
        return feedback_templates.get(module_type, {}).get(level, 'Assessment complete.')
