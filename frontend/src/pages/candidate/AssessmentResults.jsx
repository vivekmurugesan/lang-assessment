import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FiArrowLeft, FiDownload } from 'react-icons/fi';
import api from '../../api/axiosConfig';

const AssessmentResults = () => {
  const { secureLink } = useParams();
  const navigate = useNavigate();
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadResults();
  }, [secureLink]);

  const loadResults = async () => {
    try {
      const response = await api.get(`/candidate/submissions/${secureLink}/results`);
      setResults(response.data.data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load results:', error);
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-gray-600">Loading your results...</p>
        </div>
      </div>
    );
  }

  if (!results) {
    return (
      <div className="p-6">
        <div className="card text-center py-12">
          <p className="text-gray-600">Results not available yet</p>
          <p className="text-sm text-gray-500 mt-2">Your assessment is being evaluated</p>
        </div>
      </div>
    );
  }

  const cefrLevelColor = {
    'A1': 'bg-red-100 text-red-900',
    'A2': 'bg-orange-100 text-orange-900',
    'B1': 'bg-yellow-100 text-yellow-900',
    'B2': 'bg-green-100 text-green-900',
    'C1': 'bg-blue-100 text-blue-900',
    'C2': 'bg-purple-100 text-purple-900'
  };

  const cefrLevelDescription = {
    'A1': 'Beginner',
    'A2': 'Elementary',
    'B1': 'Intermediate',
    'B2': 'Upper Intermediate',
    'C1': 'Advanced',
    'C2': 'Mastery'
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <button
            onClick={() => navigate('/candidate')}
            className="flex items-center gap-2 text-indigo-600 hover:text-indigo-700 mb-4"
          >
            <FiArrowLeft size={18} />
            Back to Home
          </button>
          <h1 className="page-title">Assessment Results</h1>
          <p className="page-subtitle">Your language proficiency assessment has been evaluated</p>
        </div>

        {/* CEFR Level Card */}
        <div className={`card mb-6 p-8 text-center ${cefrLevelColor[results.cefrLevel] || 'bg-gray-100'}`}>
          <p className="text-sm font-semibold mb-2">YOUR PROFICIENCY LEVEL</p>
          <h2 className="text-6xl font-bold mb-2">{results.cefrLevel}</h2>
          <p className="text-xl mb-4">{cefrLevelDescription[results.cefrLevel]}</p>
          <p className="text-sm opacity-75">
            Common European Framework of Reference for Languages
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          {/* Overall Score */}
          <div className="card">
            <h3 className="font-bold text-lg mb-4">Overall Score</h3>
            <div className="text-4xl font-bold text-indigo-600 mb-2">
              {results.totalScore ? Math.round(results.totalScore) : 'N/A'}/100
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3 mb-2">
              <div
                className="bg-indigo-600 h-3 rounded-full transition-all duration-500"
                style={{ width: `${results.totalScore || 0}%` }}
              />
            </div>
            <p className="text-sm text-gray-600">
              {results.totalScore >= 80 ? 'Excellent performance!' :
               results.totalScore >= 60 ? 'Good performance' :
               results.totalScore >= 40 ? 'Satisfactory performance' :
               'Keep practicing'}
            </p>
          </div>

          {/* Results Summary */}
          <div className="card">
            <h3 className="font-bold text-lg mb-4">Summary</h3>
            <div className="space-y-3">
              <div className="flex justify-between">
                <span className="text-gray-600">Total Questions:</span>
                <span className="font-semibold">{results.totalQuestions || 0}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Correct Answers:</span>
                <span className="font-semibold">{results.correctAnswers || 0}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Assessment Date:</span>
                <span className="font-semibold">
                  {new Date(results.submittedAt).toLocaleDateString()}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Module Performance */}
        {results.modulePerformance && results.modulePerformance.length > 0 && (
          <div className="card mb-6">
            <h3 className="font-bold text-lg mb-4">Performance by Module</h3>
            <div className="space-y-4">
              {results.modulePerformance.map((module, idx) => (
                <div key={idx} className="p-4 bg-gray-50 rounded">
                  <div className="flex justify-between items-center mb-2">
                    <span className="font-medium">{module.moduleType}</span>
                    <span className="text-sm font-semibold">{Math.round(module.score || 0)}%</span>
                  </div>
                  <div className="w-full bg-gray-300 rounded-full h-2">
                    <div
                      className="bg-indigo-600 h-2 rounded-full transition-all duration-500"
                      style={{ width: `${module.score || 0}%` }}
                    />
                  </div>
                  <p className="text-xs text-gray-600 mt-1">
                    {module.correct || 0} correct out of {module.total || 0} questions
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Evaluator Notes */}
        {results.evaluatorNotes && (
          <div className="card mb-6">
            <h3 className="font-bold text-lg mb-4">Detailed Feedback</h3>
            <p className="text-gray-700 leading-relaxed">{results.evaluatorNotes}</p>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-4">
          <button
            onClick={() => window.print()}
            className="flex items-center gap-2 px-6 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
          >
            <FiDownload size={18} />
            Download Results
          </button>
          <button
            onClick={() => navigate('/candidate')}
            className="flex items-center gap-2 px-6 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
          >
            Back to Home
          </button>
        </div>
      </div>
    </div>
  );
};

export default AssessmentResults;
