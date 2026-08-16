import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FiHome } from 'react-icons/fi';

const EvaluationReview = () => {
  const navigate = useNavigate();

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
          <p className="page-subtitle">Review and manage candidate evaluations</p>
        </div>
      </div>
      <div className="card">
        <p className="text-gray-600">Evaluation review interface coming soon...</p>
      </div>
    </div>
  );
};

export default EvaluationReview;
