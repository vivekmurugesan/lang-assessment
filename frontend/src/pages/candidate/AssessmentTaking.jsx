import React from 'react';
import { useParams } from 'react-router-dom';

const AssessmentTaking = () => {
  const { id } = useParams();

  return (
    <div className="p-6">
      <h1 className="page-title">Assessment</h1>
      <p className="page-subtitle">Complete your language assessment</p>

      <div className="card">
        <p className="text-gray-600">
          Loading assessment {id}...
        </p>
        <p className="text-sm text-gray-500 mt-4">
          Assessment interface coming soon...
        </p>
      </div>
    </div>
  );
};

export default AssessmentTaking;
