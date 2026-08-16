import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiRefreshCw, FiEye, FiMail, FiCheckCircle, FiClock, FiHome } from 'react-icons/fi';
import api from '../../api/axiosConfig';

const AssessmentMonitoring = () => {
  const navigate = useNavigate();
  const [assessments, setAssessments] = useState([]);
  const [selectedAssessment, setSelectedAssessment] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
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
                                {candidate.status === 'EVALUATED' && (
                                  <button
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
