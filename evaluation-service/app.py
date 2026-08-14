from flask import Flask, request, jsonify
from flask_cors import CORS
import logging
import os
from dotenv import load_dotenv

from services.evaluation_engine import EvaluationEngine
from services.content_curator import ContentCurator
from services.ai_adapter import AIModelAdapter

load_dotenv()

app = Flask(__name__)
CORS(app)

# Configure logging
logging.basicConfig(
    level=os.getenv('LOG_LEVEL', 'INFO'),
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Initialize services
evaluation_engine = EvaluationEngine()
content_curator = ContentCurator()
ai_adapter = AIModelAdapter()

# Health check endpoint
@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'healthy', 'service': 'evaluation-service'}), 200

# Evaluation endpoints
@app.route('/api/evaluate/listening', methods=['POST'])
def evaluate_listening():
    """Evaluate listening section submission"""
    try:
        data = request.get_json()
        result = evaluation_engine.evaluate_listening(data)
        return jsonify({'success': True, 'data': result}), 200
    except Exception as e:
        logger.error(f"Listening evaluation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/evaluate/reading', methods=['POST'])
def evaluate_reading():
    """Evaluate reading section submission"""
    try:
        data = request.get_json()
        result = evaluation_engine.evaluate_reading(data)
        return jsonify({'success': True, 'data': result}), 200
    except Exception as e:
        logger.error(f"Reading evaluation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/evaluate/writing', methods=['POST'])
def evaluate_writing():
    """Evaluate writing section submission"""
    try:
        data = request.get_json()
        result = evaluation_engine.evaluate_writing(data)
        return jsonify({'success': True, 'data': result}), 200
    except Exception as e:
        logger.error(f"Writing evaluation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/evaluate/speaking', methods=['POST'])
def evaluate_speaking():
    """Evaluate speaking section submission"""
    try:
        data = request.form
        audio_file = request.files.get('audio')
        result = evaluation_engine.evaluate_speaking(data, audio_file)
        return jsonify({'success': True, 'data': result}), 200
    except Exception as e:
        logger.error(f"Speaking evaluation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/evaluate/batch', methods=['POST'])
def evaluate_batch():
    """Batch evaluate multiple submissions"""
    try:
        data = request.get_json()
        results = evaluation_engine.evaluate_batch(data)
        return jsonify({'success': True, 'data': results}), 200
    except Exception as e:
        logger.error(f"Batch evaluation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

# Content curation endpoints
@app.route('/api/content/generate', methods=['POST'])
def generate_content():
    """Generate assessment content"""
    try:
        data = request.get_json()
        content = content_curator.generate_content(
            language=data.get('language'),
            module_type=data.get('module_type'),
            cefr_level=data.get('cefr_level'),
            count=data.get('count', 1)
        )
        return jsonify({'success': True, 'data': content}), 200
    except Exception as e:
        logger.error(f"Content generation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/content/translate', methods=['POST'])
def translate_content():
    """Translate content to different languages"""
    try:
        data = request.get_json()
        translated = content_curator.translate_content(
            content=data.get('content'),
            source_language=data.get('source_language'),
            target_language=data.get('target_language')
        )
        return jsonify({'success': True, 'data': translated}), 200
    except Exception as e:
        logger.error(f"Content translation failed: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

# AI Model Adapter endpoints
@app.route('/api/ai/models', methods=['GET'])
def get_available_models():
    """Get list of available AI models"""
    try:
        models = ai_adapter.list_available_models()
        return jsonify({'success': True, 'data': models}), 200
    except Exception as e:
        logger.error(f"Failed to get models: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

@app.route('/api/ai/usage', methods=['GET'])
def get_usage_stats():
    """Get AI model usage statistics"""
    try:
        stats = ai_adapter.get_usage_stats()
        return jsonify({'success': True, 'data': stats}), 200
    except Exception as e:
        logger.error(f"Failed to get usage stats: {str(e)}")
        return jsonify({'success': False, 'message': str(e)}), 400

# Error handlers
@app.errorhandler(404)
def not_found(error):
    return jsonify({'success': False, 'message': 'Endpoint not found'}), 404

@app.errorhandler(500)
def server_error(error):
    logger.error(f"Server error: {str(error)}")
    return jsonify({'success': False, 'message': 'Internal server error'}), 500

if __name__ == '__main__':
    port = int(os.getenv('EVALUATION_SERVICE_PORT', 5000))
    debug = os.getenv('FLASK_ENV', 'production') == 'development'
    app.run(host='0.0.0.0', port=port, debug=debug)
