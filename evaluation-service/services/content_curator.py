import logging
import json
from typing import Dict, List, Optional
from .ai_adapter import AIModelAdapter

logger = logging.getLogger(__name__)

class ContentCurator:
    """
    Content curation service for managing assessment content,
    including generation, validation, and translation.
    """

    def __init__(self):
        self.ai_adapter = AIModelAdapter()
        self.valid_modules = ['LISTENING', 'READING', 'WRITING', 'SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION']
        self.cefr_levels = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']

    def generate_content(self, language: str, module_type: str,
                        cefr_level: str, count: int = 1) -> Dict:
        """
        Generate new assessment content.
        """
        try:
            if module_type not in self.valid_modules:
                raise ValueError(f"Invalid module type: {module_type}")
            if cefr_level not in self.cefr_levels:
                raise ValueError(f"Invalid CEFR level: {cefr_level}")

            logger.info(f"Generating {count} {module_type} questions for {cefr_level} level in {language}")

            # Use AI adapter to generate content
            result = self.ai_adapter.generate_content(
                language=language,
                module_type=module_type,
                cefr_level=cefr_level,
                count=count
            )

            return self._format_generated_content(result, language, module_type, cefr_level)
        except Exception as e:
            logger.error(f"Content generation failed: {str(e)}")
            raise

    def validate_content(self, content: Dict) -> Dict:
        """
        Validate generated content for quality.
        """
        validation_result = {
            'is_valid': True,
            'issues': [],
            'warnings': []
        }

        try:
            # Check required fields
            required_fields = ['question_text', 'answer_key', 'difficulty_level']
            for field in required_fields:
                if field not in content:
                    validation_result['issues'].append(f"Missing required field: {field}")
                    validation_result['is_valid'] = False

            # Check question length
            if len(content.get('question_text', '')) < 10:
                validation_result['issues'].append("Question text is too short")
                validation_result['is_valid'] = False

            # Check for appropriateness (basic checks)
            if self._contains_offensive_content(content.get('question_text', '')):
                validation_result['issues'].append("Content contains potentially offensive language")
                validation_result['is_valid'] = False

            logger.info(f"Content validation completed. Valid: {validation_result['is_valid']}")
            return validation_result
        except Exception as e:
            logger.error(f"Validation error: {str(e)}")
            validation_result['issues'].append(str(e))
            validation_result['is_valid'] = False
            return validation_result

    def translate_content(self, content: str, source_language: str,
                         target_language: str) -> Dict:
        """
        Translate content to different languages.
        """
        try:
            logger.info(f"Translating content from {source_language} to {target_language}")

            result = self.ai_adapter.translate_content(
                content=content,
                source_language=source_language,
                target_language=target_language
            )

            return {
                'success': 'error' not in result,
                'data': result
            }
        except Exception as e:
            logger.error(f"Translation failed: {str(e)}")
            return {
                'success': False,
                'error': str(e)
            }

    def mark_content_invalid(self, content_id: int, reason: str) -> Dict:
        """
        Mark content as invalid for future assessments.
        """
        try:
            logger.info(f"Marking content {content_id} as invalid. Reason: {reason}")
            # This would connect to the database to update content status
            return {
                'success': True,
                'content_id': content_id,
                'message': 'Content marked as invalid'
            }
        except Exception as e:
            logger.error(f"Failed to mark content invalid: {str(e)}")
            return {
                'success': False,
                'error': str(e)
            }

    def get_content_statistics(self, language: str) -> Dict:
        """
        Get statistics about available content.
        """
        try:
            stats = {
                'language': language,
                'by_module': {},
                'by_level': {},
                'total_items': 0
            }

            for module in self.valid_modules:
                module_count = 0
                for level in self.cefr_levels:
                    # This would query the database
                    count = 0  # Placeholder
                    if level not in stats['by_level']:
                        stats['by_level'][level] = 0
                    stats['by_level'][level] += count
                    module_count += count

                stats['by_module'][module] = module_count
                stats['total_items'] += module_count

            logger.info(f"Retrieved content statistics for {language}")
            return {'success': True, 'data': stats}
        except Exception as e:
            logger.error(f"Failed to get statistics: {str(e)}")
            return {'success': False, 'error': str(e)}

    def _format_generated_content(self, result: Dict, language: str,
                                 module_type: str, cefr_level: str) -> Dict:
        """
        Format generated content to standard structure.
        """
        if 'error' in result:
            return {'success': False, 'error': result['error']}

        formatted_questions = []
        for question in result.get('questions', []):
            formatted_question = {
                'question_text': question.get('question_text', question),
                'module_type': module_type,
                'cefr_level': cefr_level,
                'language': language,
                'answer_key': question.get('answer_key'),
                'options': question.get('options', []),
                'difficulty_level': cefr_level,
                'status': 'UNDER_REVIEW'
            }
            formatted_questions.append(formatted_question)

        return {
            'success': True,
            'data': {
                'language': language,
                'module_type': module_type,
                'cefr_level': cefr_level,
                'questions': formatted_questions,
                'total_generated': len(formatted_questions)
            }
        }

    def _contains_offensive_content(self, text: str) -> bool:
        """
        Check if content contains potentially offensive language.
        This is a basic implementation - a production system would use
        more sophisticated content filtering.
        """
        # Placeholder for offensive content detection
        return False

    def batch_generate_content(self, specifications: List[Dict]) -> List[Dict]:
        """
        Generate content for multiple specifications.
        """
        results = []
        for spec in specifications:
            try:
                result = self.generate_content(
                    language=spec.get('language', 'English'),
                    module_type=spec.get('module_type'),
                    cefr_level=spec.get('cefr_level'),
                    count=spec.get('count', 1)
                )
                results.append(result)
            except Exception as e:
                results.append({
                    'success': False,
                    'error': str(e),
                    'specification': spec
                })

        return results
