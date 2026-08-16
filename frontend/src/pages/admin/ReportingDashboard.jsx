import React from 'react';
import { useNavigate } from 'react-router-dom';
import { FiHome } from 'react-icons/fi';

const ReportingDashboard = () => {
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
          <h1 className="page-title">Reporting Dashboard</h1>
          <p className="page-subtitle">Analytics and performance reports</p>
        </div>
      </div>
      <div className="card">
        <p className="text-gray-600">Reporting dashboard interface coming soon...</p>
      </div>
    </div>
  );
};

export default ReportingDashboard;
