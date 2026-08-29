import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiRefreshCw, FiEye, FiMail, FiCheckCircle, FiClock, FiHome, FiX } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const AssessmentMonitoring = () => {
  const navigate = useNavigate();
  const [assessments, setAssessments] = useState([]);
  const [selectedAssessment, setSelectedAssessment] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [selectedSubmission, setSelectedSubmission] = useState(null);
  const [submissionDetails, setSubmissionDetails] = useState(null);
  const [loadingSubmission, setLoadingSubmission] = useState(false);
  const [evaluatorNotes, setEvaluatorNotes] = useState('');
  const [responseScores, setResponseScores] = useState({});
  const [evaluationSaved, setEvaluationSaved] = useState(false);
  const [stats, setStats] = useState({
    totalCandidates: 0,
    invited: 0,
    started: 0,
    completed: 0,
    evaluated: 0,
  });

  useEffect(() => {
    loadAssessments();
  }, []);

  useEffect(() => {
    if (selectedAssessment) {
      loadCandidates(selectedAssessment);
    }
  }, [selectedAssessment]);

  const loadAssessments = async () => {
    try {
      const response = await api.get('/assessments?page=0&size=10');
      setAssessments(response.data.data.content);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load assessments:', error);
      setLoading(false);
    }
  };

  const loadCandidates = async (assessmentId) => {
    try {
      const response = await api.get(
        `/admin/assessments/${assessmentId}/candidates?page=0&size=100`
      );
      const candidateList = response.data.data.content;
      setCandidates(candidateList);
      calculateStats(candidateList);
    } catch (error) {
      console.error('Failed to load candidates:', error);
    }
  };

  const calculateStats = (candidateList) => {
    const newStats = {
      totalCandidates: candidateList.length,
      invited: candidateList.filter((c) => c.status === 'INVITED').length,
      started: candidateList.filter((c) => c.status === 'STARTED').length,
      completed: candidateList.filter((c) => c.status === 'COMPLETED').length,
      evaluated: candidateList.filter((c) => c.status === 'EVALUATED').length,
    };
    setStats(newStats);
  };

  const resendInvitation = async (candidateId) => {
    try {
      await api.post(`/admin/candidates/${candidateId}/resend-invitation`);
      alert('Invitation resent successfully');
      loadCandidates(selectedAssessment);
    } catch (error) {
      console.error('Failed to resend invitation:', error);
      alert('Failed to resend invitation');
    }
  };

  const openSubmissionReview = async (candidate) => {
    setSelectedSubmission(candidate);
    setLoadingSubmission(true);
    setShowReviewModal(true);
    try {
      const detailsResponse = await api.get(`/admin/submissions/candidate/${candidate.id}`);
      if (detailsResponse.data.success) {
        setSubmissionDetails(detailsResponse.data.data);
        setEvaluatorNotes(detailsResponse.data.data.evaluatorNotes || '');

        // Initialize response scores
        const scores = {};
        if (detailsResponse.data.data.responses) {
          detailsResponse.data.data.responses.forEach(r => {
            scores[r.id] = r.score || 0;
          });
        }
        setResponseScores(scores);
      } else {
        toast.error(detailsResponse.data.message || 'Failed to load submission details');
        setShowReviewModal(false);
      }
    } catch (error) {
      console.error('Failed to load submission details:', error);
      toast.error('Failed to load submission details');
      setShowReviewModal(false);
    } finally {
      setLoadingSubmission(false);
    }
  };

  const handleScoreChange = (responseId, score) => {
    setResponseScores(prev => ({
      ...prev,
      [responseId]: parseFloat(score)
    }));
  };

  const saveEvaluation = async () => {
    if (!submissionDetails) return;

    try {
      await api.post(`/admin/submissions/${submissionDetails.id}/evaluate`, {
        responseScores,
        evaluatorNotes
      });
      toast.success('Submission evaluated successfully');
      setEvaluationSaved(true);
    } catch (error) {
      console.error('Failed to save evaluation:', error);
      toast.error('Failed to save evaluation');
    }
  };

  const closeReviewModal = () => {
    setShowReviewModal(false);
    setEvaluationSaved(false);
    loadCandidates(selectedAssessment);
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'INVITED':
        return 'bg-yellow-100 text-yellow-800';
      case 'STARTED':
        return 'bg-blue-100 text-blue-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'EVALUATED':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'INVITED':
        return <FiMail size={16} />;
      case 'STARTED':
        return <FiClock size={16} />;
      case 'COMPLETED':
        return <FiCheckCircle size={16} />;
      case 'EVALUATED':
        return <FiEye size={16} />;
      default:
        return null;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/admin')}
            className="btn btn-secondary flex items-center gap-2 hover:bg-gray-200"
            title="Back to Admin Dashboard"
          >
            <FiHome size={18} /> Home
          </button>
          <div>
            <h1 className="page-title">Assessment Monitoring</h1>
            <p className="page-subtitle">Track candidate progress and completion status</p>
          </div>
        </div>
        <button
          onClick={() => {
            loadAssessments();
            if (selectedAssessment) loadCandidates(selectedAssessment);
          }}
          className="btn btn-secondary flex items-center gap-2"
        >
          <FiRefreshCw size={18} /> Refresh
        </button>
      </div>

      {loading ? (
        <div className="text-center py-8">Loading assessments...</div>
      ) : (
        <>
          <div className="card mb-6">
            <label className="form-label">Select Assessment</label>
            <select
              value={selectedAssessment || ''}
              onChange={(e) => setSelectedAssessment(parseInt(e.target.value))}
              className="form-input"
            >
              <option value="">Choose an assessment...</option>
              {assessments.map((assessment) => (
                <option key={assessment.id} value={assessment.id}>
                  {assessment.title} ({assessment.languageName})
                </option>
              ))}
            </select>
          </div>

          {selectedAssessment && (
            <>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-6">
                <StatCard
                  label="Total Candidates"
                  value={stats.totalCandidates}
                  color="blue"
                />
                <StatCard
                  label="Invited"
                  value={stats.invited}
                  color="yellow"
                />
                <StatCard
                  label="Started"
                  value={stats.started}
                  color="cyan"
                />
                <StatCard
                  label="Completed"
                  value={stats.completed}
                  color="green"
                />
                <StatCard
                  label="Evaluated"
                  value={stats.evaluated}
                  color="purple"
                />
              </div>

              <div className="card">
                <div className="mb-4">
                  <h2 className="text-xl font-bold mb-4">Candidate Progress</h2>
                  {candidates.length === 0 ? (
                    <div className="text-center py-8">
                      <p className="text-gray-600">No candidates yet</p>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="border-b">
                            <th className="text-left py-3 px-4">Name</th>
                            <th className="text-left py-3 px-4">Email</th>
                            <th className="text-left py-3 px-4">Status</th>
                            <th className="text-left py-3 px-4">Started</th>
                            <th className="text-left py-3 px-4">Completed</th>
                            <th className="text-center py-3 px-4">Actions</th>
                          </tr>
                        </thead>
                        <tbody>
                          {candidates.map((candidate) => (
                            <tr
                              key={candidate.id}
                              className="border-b hover:bg-gray-50"
                            >
                              <td className="py-3 px-4 font-medium">
                                {candidate.name}
                              </td>
                              <td className="py-3 px-4 text-sm">{candidate.email}</td>
                              <td className="py-3 px-4">
                                <span
                                  className={`inline-flex items-center gap-1 px-3 py-1 rounded text-sm font-medium ${getStatusColor(
                                    candidate.status
                                  )}`}
                                >
                                  {getStatusIcon(candidate.status)}
                                  {candidate.status}
                                </span>
                              </td>
                              <td className="py-3 px-4 text-sm">
                                {formatDate(candidate.startedAt)}
                              </td>
                              <td className="py-3 px-4 text-sm">
                                {formatDate(candidate.completedAt)}
                              </td>
                              <td className="py-3 px-4 text-center">
                                {candidate.status === 'INVITED' && (
                                  <button
                                    onClick={() => resendInvitation(candidate.id)}
                                    className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                                    title="Resend invitation"
                                  >
                                    Resend
                                  </button>
                                )}
                                {candidate.status === 'COMPLETED' && (
                                  <button
                                    onClick={() => openSubmissionReview(candidate)}
                                    className="text-orange-600 hover:text-orange-800 text-sm font-medium flex items-center gap-1"
                                    title="Review and evaluate submission"
                                  >
                                    <FiEye size={14} /> Review
                                  </button>
                                )}
                                {candidate.status === 'EVALUATED' && (
                                  <button
                                    onClick={() => openSubmissionReview(candidate)}
                                    className="text-green-600 hover:text-green-800 text-sm font-medium flex items-center gap-1"
                                    title="View evaluation"
                                  >
                                    <FiEye size={14} /> View
                                  </button>
                                )}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
        </>
      )}

      {/* Review Modal */}
      {showReviewModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg max-w-4xl w-full max-h-96 overflow-y-auto">
            <div className="flex justify-between items-center p-6 border-b sticky top-0 bg-white">
              <h2 className="text-xl font-bold">
                Review Submission - {selectedSubmission?.name}
              </h2>
              <button
                onClick={() => setShowReviewModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                <FiX size={24} />
              </button>
            </div>

            {loadingSubmission ? (
              <div className="p-6 text-center">Loading submission details...</div>
            ) : evaluationSaved ? (
              <div className="p-6 text-center space-y-4">
                <div className="text-5xl">✓</div>
                <h3 className="text-xl font-bold text-green-600">Evaluation Approved</h3>
                <p className="text-gray-600">The submission has been evaluated and the candidate can now view their results.</p>
                <button
                  onClick={closeReviewModal}
                  className="btn btn-primary mt-4"
                >
                  Close
                </button>
              </div>
            ) : submissionDetails ? (
              <div className="p-6 space-y-6">
                {/* Submission Summary */}
                <div className="bg-gray-50 p-4 rounded">
                  <div className="grid grid-cols-4 gap-4">
                    <div>
                      <p className="text-sm text-gray-600">Total Questions</p>
                      <p className="text-2xl font-bold">{submissionDetails.totalQuestions}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Correct Answers</p>
                      <p className="text-2xl font-bold text-green-600">{submissionDetails.correctAnswers}</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">Score</p>
                      <p className="text-2xl font-bold">{submissionDetails.totalScore ? submissionDetails.totalScore.toFixed(2) : 'N/A'}%</p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">CEFR Level</p>
                      <p className="text-2xl font-bold text-indigo-600">{submissionDetails.cefrLevel || 'Pending'}</p>
                    </div>
                  </div>
                </div>

                {/* Responses */}
                <div>
                  <h3 className="font-bold mb-3">Responses</h3>
                  <div className="space-y-4">
                    {submissionDetails.responses?.map((response, idx) => (
                      <div key={response.id} className="border rounded p-4">
                        <div className="mb-2">
                          <p className="font-medium">Q{idx + 1}: {response.questionText}</p>
                          <p className="text-sm text-gray-600">{response.moduleType}</p>
                        </div>
                        <div className="mb-3">
                          {response.selectedOption && (
                            <p><strong>Answer:</strong> {response.selectedOption}</p>
                          )}
                          {response.responseText && (
                            <p className="text-sm"><strong>Response:</strong> {response.responseText.substring(0, 100)}...</p>
                          )}
                        </div>
                        {['WRITING', 'SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION'].includes(response.moduleType) && (
                          <div>
                            <label className="text-sm font-medium">Score (0-100):</label>
                            <input
                              type="number"
                              min="0"
                              max="100"
                              value={responseScores[response.id] || response.score || 0}
                              onChange={(e) => handleScoreChange(response.id, e.target.value)}
                              className="form-input w-full mt-1"
                            />
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                {/* Evaluator Notes */}
                <div>
                  <label className="form-label">Evaluator Notes</label>
                  <textarea
                    value={evaluatorNotes}
                    onChange={(e) => setEvaluatorNotes(e.target.value)}
                    className="form-input"
                    rows="3"
                    placeholder="Add any notes about this evaluation..."
                  />
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3 justify-end">
                  <button
                    onClick={() => setShowReviewModal(false)}
                    className="btn btn-secondary"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={saveEvaluation}
                    className="btn btn-primary"
                  >
                    Save & Approve Evaluation
                  </button>
                </div>
              </div>
            ) : (
              <div className="p-6 text-center text-gray-600">
                No submission details available
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const StatCard = ({ label, value, color }) => {
  const colorMap = {
    blue: 'bg-blue-50 border-blue-200 text-blue-900',
    yellow: 'bg-yellow-50 border-yellow-200 text-yellow-900',
    cyan: 'bg-cyan-50 border-cyan-200 text-cyan-900',
    green: 'bg-green-50 border-green-200 text-green-900',
    purple: 'bg-purple-50 border-purple-200 text-purple-900',
  };

  return (
    <div className={`card border-2 ${colorMap[color]}`}>
      <p className="text-sm font-medium mb-2">{label}</p>
      <p className="text-3xl font-bold">{value}</p>
    </div>
  );
};

export default AssessmentMonitoring;
