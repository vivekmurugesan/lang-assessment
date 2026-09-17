import React from 'react';
import { Link } from 'react-router-dom';

const tiles = [
  {
    to: '/admin/catalog',
    icon: '📚',
    color: 'bg-indigo-100 text-indigo-700',
    title: 'Question Catalog',
    desc: 'Review and approve questions for assessments',
  },
  {
    to: '/admin/assessments',
    icon: '📋',
    color: 'bg-blue-100 text-blue-700',
    title: 'Assessments',
    desc: 'Setup and manage language assessments',
  },
  {
    to: '/admin/onboarding',
    icon: '👥',
    color: 'bg-purple-100 text-purple-700',
    title: 'Onboarding',
    desc: 'Bulk add candidates and generate links',
  },
  {
    to: '/admin/monitoring',
    icon: '📊',
    color: 'bg-amber-100 text-amber-700',
    title: 'Monitoring',
    desc: 'Track candidate progress and completion',
  },
  {
    to: '/admin/evaluation',
    icon: '✅',
    color: 'bg-emerald-100 text-emerald-700',
    title: 'Evaluation',
    desc: 'Review and manage evaluations',
  },
  {
    to: '/admin/reports',
    icon: '📈',
    color: 'bg-rose-100 text-rose-700',
    title: 'Reports',
    desc: 'View analytics and performance reports',
  },
];

const AdminDashboard = () => {
  return (
    <div className="p-6 max-w-7xl mx-auto">
      <div className="mb-8">
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="page-subtitle">Welcome to Language Assessment Administration</p>
      </div>

      <div className="grid-3">
        {tiles.map((tile) => (
          <Link key={tile.to} to={tile.to} className="card-hover">
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4 ${tile.color}`}>
              {tile.icon}
            </div>
            <h2 className="text-lg font-bold text-gray-900 mb-1">{tile.title}</h2>
            <p className="text-gray-500 text-sm">{tile.desc}</p>
          </Link>
        ))}

        <div className="card opacity-60 cursor-not-allowed">
          <div className="w-12 h-12 rounded-xl flex items-center justify-center text-2xl mb-4 bg-gray-100 text-gray-500">
            ⚙️
          </div>
          <h2 className="text-lg font-bold text-gray-900 mb-1">Settings</h2>
          <p className="text-gray-500 text-sm">System configuration and preferences</p>
        </div>
      </div>

      <div className="mt-10">
        <div className="alert alert-info flex items-start gap-3">
          <span className="text-lg">💡</span>
          <div>
            <h3 className="font-semibold text-blue-900 mb-1">Getting Started</h3>
            <p className="text-blue-800 text-sm">
              Start by creating an assessment, then onboard candidates and review their submissions.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
