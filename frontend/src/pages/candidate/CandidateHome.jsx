import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiPlay, FiCheckCircle, FiClock } from 'react-icons/fi';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/axiosConfig';

const CandidateHome = () => {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [assessments, setAssessments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    loadAssessments();
  }, []);

  const loadAssessments = async () => {
    try {
      const response = await api.get('/candidate/my-assessments');
      setAssessments(response.data.data || []);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load assessments:', error);
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'INVITED':
        return 'bg-blue-100 text-blue-800';
      case 'STARTED':
        return 'bg-yellow-100 text-yellow-800';
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'EVALUATED':
        return 'bg-purple-100 text-purple-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusIcon = (status) => {
    if (status === 'COMPLETED' || status === 'EVALUATED') {
      return <FiCheckCircle className="text-green-600 mr-2" size={20} />;
    }
    return <FiClock className="text-blue-600 mr-2" size={20} />;
  };

  const filteredAssessments = assessments.filter(a => {
    if (filter === 'pending') return a.status === 'INVITED';
    if (filter === 'active') return a.status === 'STARTED';
    if (filter === 'completed') return a.status === 'COMPLETED' || a.status === 'EVALUATED';
    return true;
  });

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="page-title">Welcome, {user?.name}!</h1>
        <p className="page-subtitle">Your Language Assessment Portal</p>
      </div>

      <div className="mb-6 flex gap-2">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 rounded font-medium transition ${
            filter === 'all'
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setFilter('pending')}
          className={`px-4 py-2 rounded font-medium transition ${
            filter === 'pending'
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          Pending
        </button>
        <button
          onClick={() => setFilter('active')}
          className={`px-4 py-2 rounded font-medium transition ${
            filter === 'active'
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          In Progress
        </button>
        <button
          onClick={() => setFilter('completed')}
          className={`px-4 py-2 rounded font-medium transition ${
            filter === 'completed'
              ? 'bg-indigo-600 text-white'
              : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
          }`}
        >
          Completed
        </button>
      </div>

      {loading ? (
        <div className="text-center py-8 text-gray-600">Loading your assessments...</div>
      ) : filteredAssessments.length === 0 ? (
        <div className="card text-center py-12">
          <p className="text-gray-600 text-lg mb-2">No assessments in this category</p>
          <p className="text-gray-500 text-sm">Check back later for new assessments</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredAssessments.map((assessment) => (
            <div key={assessment.id} className="card hover:shadow-lg transition">
              <div className="flex justify-between items-start mb-3">
                <div className="flex-1">
                  <h3 className="font-bold text-lg mb-1">{assessment.secureLink ? 'Assessment' : assessment.title}</h3>
                  <p className="text-sm text-gray-600">{assessment.name || assessment.languageName}</p>
                </div>
                <span className={`px-2 py-1 rounded text-xs font-medium whitespace-nowrap ${getStatusColor(assessment.status)}`}>
                  {assessment.status}
                </span>
              </div>

              <div className="mb-4 flex items-center">
                {getStatusIcon(assessment.status)}
                <p className="text-sm text-gray-600">
                  {assessment.status === 'INVITED' && 'Ready to start'}
                  {assessment.status === 'STARTED' && 'In progress'}
                  {assessment.status === 'COMPLETED' && 'Awaiting evaluation'}
                  {assessment.status === 'EVALUATED' && 'Results available'}
                </p>
              </div>

              {assessment.createdAt && (
                <p className="text-xs text-gray-500 mb-4">
                  Assigned: {new Date(assessment.createdAt).toLocaleDateString()}
                </p>
              )}

              {assessment.status === 'INVITED' || assessment.status === 'STARTED' ? (
                <button
                  onClick={() => navigate(`/assessment/${assessment.secureLink || assessment.id}`)}
                  className="w-full btn btn-primary flex items-center justify-center gap-2"
                >
                  <FiPlay size={16} />
                  {assessment.status === 'INVITED' ? 'Start Assessment' : 'Continue Assessment'}
                </button>
              ) : (
                <button
                  onClick={() => navigate(`/assessment/${assessment.secureLink || assessment.id}/results`)}
                  className="w-full btn btn-secondary"
                >
                  View Results
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="mt-8 bg-blue-50 border-l-4 border-blue-400 p-4 rounded">
        <h4 className="font-semibold text-blue-900 mb-2">Assessment Tips</h4>
        <ul className="text-sm text-blue-800 space-y-1 list-disc list-inside">
          <li>Make sure you have a stable internet connection</li>
          <li>Find a quiet environment for the assessment</li>
          <li>Allow approximately 60-90 minutes to complete</li>
          <li>You can pause and resume your assessment</li>
        </ul>
      </div>
    </div>
  );
};

export default CandidateHome;
