import React from 'react';
import { Link } from 'react-router-dom';

const AdminDashboard = () => {
  return (
    <div className="p-6">
      <div className="mb-8">
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="page-subtitle">Welcome to Language Assessment Administration</p>
      </div>

      <div className="grid-3">
        <Link to="/admin/catalog" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">📚</div>
            <h2 className="text-xl font-semibold ml-4">Question Catalog</h2>
          </div>
          <p className="text-gray-600">Review and approve questions for assessments</p>
        </Link>

        <Link to="/admin/assessments" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">📋</div>
            <h2 className="text-xl font-semibold ml-4">Assessments</h2>
          </div>
          <p className="text-gray-600">Setup and manage language assessments</p>
        </Link>

        <Link to="/admin/onboarding" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">👥</div>
            <h2 className="text-xl font-semibold ml-4">Onboarding</h2>
          </div>
          <p className="text-gray-600">Bulk add candidates and generate links</p>
        </Link>

        <Link to="/admin/monitoring" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">📊</div>
            <h2 className="text-xl font-semibold ml-4">Monitoring</h2>
          </div>
          <p className="text-gray-600">Track candidate progress and completion</p>
        </Link>

        <Link to="/admin/evaluation" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">✅</div>
            <h2 className="text-xl font-semibold ml-4">Evaluation</h2>
          </div>
          <p className="text-gray-600">Review and manage evaluations</p>
        </Link>

        <Link to="/admin/reports" className="card-hover">
          <div className="flex items-center mb-4">
            <div className="text-3xl">📈</div>
            <h2 className="text-xl font-semibold ml-4">Reports</h2>
          </div>
          <p className="text-gray-600">View analytics and performance reports</p>
        </Link>

        <div className="card-hover opacity-50 cursor-not-allowed">
          <div className="flex items-center mb-4">
            <div className="text-3xl">⚙️</div>
            <h2 className="text-xl font-semibold ml-4">Settings</h2>
          </div>
          <p className="text-gray-600">System configuration and preferences</p>
        </div>
      </div>

      <div className="mt-12">
        <div className="bg-blue-50 border-l-4 border-blue-400 p-6 rounded">
          <h3 className="font-semibold text-blue-900 mb-2">Getting Started</h3>
          <p className="text-blue-800 text-sm">
            Start by creating an assessment, then onboard candidates and review their submissions.
          </p>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
