import React from 'react';
import { useAuthStore } from '../../store/authStore';

const CandidateHome = () => {
  const { user } = useAuthStore();

  return (
    <div className="p-6">
      <h1 className="page-title">Welcome, {user?.name}</h1>
      <p className="page-subtitle">Your Language Assessment Portal</p>

      <div className="grid-2">
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Available Assessments</h3>
          <p className="text-gray-600">You have no active assessments at this time.</p>
        </div>

        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Previous Results</h3>
          <p className="text-gray-600">No previous assessment results.</p>
        </div>
      </div>

      <div className="mt-6 bg-amber-50 border-l-4 border-amber-400 p-4 rounded">
        <p className="text-amber-800 text-sm">
          Your assessments will appear here once an administrator assigns them to you.
        </p>
      </div>
    </div>
  );
};

export default CandidateHome;
