import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiHome, FiArrowRight } from 'react-icons/fi';
import api from '../../api/axiosConfig';

const EvaluationReview = () => {
  const navigate = useNavigate();
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadEvaluatedSubmissions();
  }, []);

  const loadEvaluatedSubmissions = async () => {
    try {
      const response = await api.get('/admin/submissions?page=0&size=20');
      const evaluated = response.data.data.content.filter(s => s.status === 'EVALUATED');
      setSubmissions(evaluated);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load submissions:', error);
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      <div className="flex items-center gap-4 mb-6">
        <button
          onClick={() => navigate('/admin')}
          className="btn btn-secondary flex items-center gap-2 hover:bg-gray-200"
          title="Back to Admin Dashboard"
        >
          <FiHome size={18} /> Home
        </button>
        <div>
          <h1 className="page-title">Evaluation Review</h1>
          <p className="page-subtitle">View all evaluated candidate submissions</p>
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="text-center py-8">
            <p className="text-gray-600">Loading evaluations...</p>
          </div>
        ) : submissions.length === 0 ? (
          <div className="text-center py-8">
            <p className="text-gray-600 mb-4">No evaluated submissions yet</p>
            <p className="text-sm text-gray-500">
              Submissions will appear here after they have been evaluated in the Assessment Monitoring section.
            </p>
            <button
              onClick={() => navigate('/admin/assessments/monitoring')}
              className="mt-4 flex items-center gap-2 justify-center mx-auto px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              Go to Assessment Monitoring
              <FiArrowRight size={16} />
            </button>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left py-3 px-4">Assessment ID</th>
                  <th className="text-left py-3 px-4">Candidate ID</th>
                  <th className="text-left py-3 px-4">Score</th>
                  <th className="text-left py-3 px-4">CEFR Level</th>
                  <th className="text-left py-3 px-4">Questions</th>
                  <th className="text-left py-3 px-4">Correct</th>
                  <th className="text-left py-3 px-4">Status</th>
                </tr>
              </thead>
              <tbody>
                {submissions.map((submission) => (
                  <tr key={submission.id} className="border-b hover:bg-gray-50">
                    <td className="py-3 px-4 font-medium">{submission.assessmentCandidateId}</td>
                    <td className="py-3 px-4 text-sm">{submission.assessmentCandidateId}</td>
                    <td className="py-3 px-4 font-bold text-indigo-600">
                      {submission.totalScore ? Math.round(submission.totalScore) : 'N/A'}/100
                    </td>
                    <td className="py-3 px-4">
                      <span className="inline-block px-3 py-1 bg-blue-100 text-blue-800 rounded text-sm font-medium">
                        {submission.cefrLevel || 'Pending'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center">{submission.totalQuestions}</td>
                    <td className="py-3 px-4 text-center">{submission.correctAnswers}</td>
                    <td className="py-3 px-4">
                      <span className="inline-block px-3 py-1 bg-green-100 text-green-800 rounded text-sm font-medium">
                        {submission.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="mt-6 card bg-blue-50 p-4">
        <h3 className="font-bold mb-2">About Evaluation Review</h3>
        <p className="text-sm text-gray-700 mb-2">
          This page shows all candidate submissions that have been evaluated. Admins can:
        </p>
        <ul className="text-sm text-gray-700 space-y-1 ml-4">
          <li>• View submission scores and CEFR levels</li>
          <li>• Review evaluator notes and responses in Assessment Monitoring</li>
          <li>• Approve and finalize evaluations</li>
          <li>• Enable candidates to view their results</li>
        </ul>
      </div>
    </div>
  );
};

export default EvaluationReview;
