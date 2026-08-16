import React, { useState, useEffect } from 'react';
import { FiZap, FiCheck, FiAlertCircle } from 'react-icons/fi';

const GenerationProgressModal = ({ isOpen, status, progress, message, error }) => {
  const [displayMessage, setDisplayMessage] = useState('');

  useEffect(() => {
    if (message) {
      setDisplayMessage(message);
    }
  }, [message]);

  if (!isOpen) return null;

  const isComplete = status === 'complete';
  const hasError = status === 'error';

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-8 max-w-md w-full mx-4">
        <div className="text-center">
          {/* Icon */}
          <div className="mb-6 flex justify-center">
            {hasError ? (
              <div className="text-5xl text-red-500">
                <FiAlertCircle size={56} />
              </div>
            ) : isComplete ? (
              <div className="text-5xl text-green-500">
                <FiCheck size={56} />
              </div>
            ) : (
              <div className="text-5xl text-indigo-600 animate-pulse">
                <FiZap size={56} />
              </div>
            )}
          </div>

          {/* Title */}
          <h2 className="text-2xl font-bold mb-4">
            {hasError ? 'Generation Failed' : isComplete ? 'Generation Complete' : 'Generating Questions'}
          </h2>

          {/* Message */}
          <p className="text-gray-600 mb-6 text-sm leading-relaxed">
            {displayMessage}
          </p>

          {/* Progress Bar */}
          {!isComplete && !hasError && (
            <div className="mb-6">
              <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-indigo-600 h-full transition-all duration-300 ease-out"
                  style={{ width: `${progress}%` }}
                />
              </div>
              <p className="text-xs text-gray-500 mt-2">{progress}%</p>
            </div>
          )}

          {/* Status Details */}
          {!isComplete && !hasError && (
            <div className="space-y-2 mb-6 text-left bg-gray-50 p-4 rounded">
              <p className="text-sm text-gray-700">
                <span className="font-medium">Status:</span> Processing your assessment modules
              </p>
              <p className="text-xs text-gray-600">
                This may take a few moments depending on the number of questions being generated.
              </p>
            </div>
          )}

          {/* Error Details */}
          {hasError && (
            <div className="mb-6 text-left bg-red-50 border border-red-200 p-4 rounded">
              <p className="text-sm text-red-700 font-medium mb-2">Error Details:</p>
              <p className="text-xs text-red-600">{error}</p>
              <p className="text-xs text-red-600 mt-2">Please check your backend logs for more information.</p>
            </div>
          )}

          {/* Tips */}
          {!isComplete && !hasError && (
            <div className="bg-blue-50 border border-blue-200 p-3 rounded text-left">
              <p className="text-xs text-blue-800">
                <span className="font-medium">💡 Tip:</span> Using free tier Gemini API? The system will automatically retry if rate limits are exceeded.
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default GenerationProgressModal;
