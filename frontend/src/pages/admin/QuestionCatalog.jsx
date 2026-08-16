import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiCheck, FiX, FiHome, FiRefreshCw, FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const QuestionCatalog = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [stats, setStats] = useState(null);
  const [languages, setLanguages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(10);
  const [filters, setFilters] = useState({
    languageId: '',
    moduleType: '',
    approvalStatus: 'PENDING_REVIEW',
    catalogCategory: ''
  });

  useEffect(() => {
    loadLanguages();
  }, []);

  useEffect(() => {
    if (filters.languageId) {
      loadCatalogQuestions();
      loadStatistics();
    }
  }, [filters, currentPage]);

  const loadLanguages = async () => {
    try {
      const response = await api.get('/languages');
      setLanguages(response.data.data);
      if (response.data.data.length > 0) {
        setFilters(prev => ({ ...prev, languageId: response.data.data[0].id }));
      }
    } catch (error) {
      console.error('Failed to load languages:', error);
      toast.error('Failed to load languages');
    }
  };

  const loadCatalogQuestions = async () => {
    try {
      setLoading(true);
      const params = new URLSearchParams({
        languageId: filters.languageId,
        approvalStatus: filters.approvalStatus,
        page: currentPage,
        size: pageSize,
        ...(filters.moduleType && { moduleType: filters.moduleType }),
        ...(filters.catalogCategory && { catalogCategory: filters.catalogCategory })
      });

      const response = await api.get(`/admin/questions/catalog?${params}`);
      if (response.data.success) {
        setQuestions(response.data.data.content || []);
      }
      setLoading(false);
    } catch (error) {
      console.error('Failed to load catalog questions:', error);
      toast.error('Failed to load catalog questions');
      setLoading(false);
    }
  };

  const loadStatistics = async () => {
    try {
      const response = await api.get(`/admin/questions/catalog/stats?languageId=${filters.languageId}`);
      if (response.data.success) {
        setStats(response.data.data);
      }
    } catch (error) {
      console.error('Failed to load statistics:', error);
    }
  };

  const handleApprove = async (questionId) => {
    try {
      const userEmail = localStorage.getItem('userEmail');
      if (!userEmail) {
        toast.error('User email not found. Please log in again.');
        return;
      }

      const response = await api.post(
        `/admin/questions/catalog/${questionId}/approve?approvedBy=${encodeURIComponent(userEmail)}`
      );

      if (response.data.success) {
        toast.success('Question approved successfully');
        loadCatalogQuestions();
        loadStatistics();
      }
    } catch (error) {
      console.error('Failed to approve question:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to approve question';
      toast.error(errorMessage);
    }
  };

  const handleReject = async (questionId) => {
    const reason = window.prompt('Enter rejection reason:');
    if (!reason) return;

    try {
      const userEmail = localStorage.getItem('userEmail');
      if (!userEmail) {
        toast.error('User email not found. Please log in again.');
        return;
      }

      const response = await api.post(
        `/admin/questions/catalog/${questionId}/reject?reason=${encodeURIComponent(reason)}&rejectedBy=${encodeURIComponent(userEmail)}`
      );

      if (response.data.success) {
        toast.success('Question rejected successfully');
        loadCatalogQuestions();
        loadStatistics();
      }
    } catch (error) {
      console.error('Failed to reject question:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to reject question';
      toast.error(errorMessage);
    }
  };

  const handleFilterChange = (field, value) => {
    setFilters(prev => ({ ...prev, [field]: value }));
    setCurrentPage(0);
  };

  const modules = ['LISTENING', 'READING', 'SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION', 'WRITING'];
  const categories = ['GENERAL', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

  return (
    <div className="p-6">
      {/* Header */}
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
            <h1 className="page-title">Question Catalog</h1>
            <p className="page-subtitle">Review and approve questions for use in assessments</p>
          </div>
        </div>
        <button
          onClick={() => loadCatalogQuestions()}
          className="btn btn-primary flex items-center gap-2"
          title="Refresh"
        >
          <FiRefreshCw size={18} /> Refresh
        </button>
      </div>

      {/* Statistics Cards */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="card p-4">
            <p className="text-sm text-gray-600">Total Questions</p>
            <p className="text-3xl font-bold text-indigo-600">{stats.totalQuestions || 0}</p>
          </div>
          <div className="card p-4">
            <p className="text-sm text-gray-600">Approved</p>
            <p className="text-3xl font-bold text-green-600">{stats.approvedQuestions || 0}</p>
          </div>
          <div className="card p-4">
            <p className="text-sm text-gray-600">Pending Review</p>
            <p className="text-3xl font-bold text-yellow-600">
              {stats.byApprovalStatus?.PENDING_REVIEW || 0}
            </p>
          </div>
          <div className="card p-4">
            <p className="text-sm text-gray-600">Rejected</p>
            <p className="text-3xl font-bold text-red-600">
              {stats.byApprovalStatus?.REJECTED || 0}
            </p>
          </div>
        </div>
      )}

      {/* Filters */}
      <div className="card mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="form-label">Language</label>
            <select
              value={filters.languageId}
              onChange={(e) => handleFilterChange('languageId', e.target.value)}
              className="form-input"
            >
              <option value="">Select a language</option>
              {languages.map((lang) => (
                <option key={lang.id} value={lang.id}>
                  {lang.name}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="form-label">Module Type</label>
            <select
              value={filters.moduleType}
              onChange={(e) => handleFilterChange('moduleType', e.target.value)}
              className="form-input"
            >
              <option value="">All Modules</option>
              {modules.map((module) => (
                <option key={module} value={module}>
                  {module.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="form-label">Status</label>
            <select
              value={filters.approvalStatus}
              onChange={(e) => handleFilterChange('approvalStatus', e.target.value)}
              className="form-input"
            >
              <option value="PENDING_REVIEW">Pending Review</option>
              <option value="APPROVED">Approved</option>
              <option value="REJECTED">Rejected</option>
            </select>
          </div>

          <div>
            <label className="form-label">Category</label>
            <select
              value={filters.catalogCategory}
              onChange={(e) => handleFilterChange('catalogCategory', e.target.value)}
              className="form-input"
            >
              <option value="">All Categories</option>
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Questions List */}
      {loading ? (
        <div className="text-center py-8">
          <p className="text-gray-600">Loading questions...</p>
        </div>
      ) : questions.length === 0 ? (
        <div className="card text-center py-8">
          <p className="text-gray-600">No questions found</p>
        </div>
      ) : (
        <div className="space-y-4">
          {questions.map((question) => (
            <div key={question.id} className="card p-6">
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <div className="flex gap-2 items-center mb-2">
                    <span className="inline-block px-3 py-1 bg-indigo-100 text-indigo-700 rounded text-sm font-medium">
                      {question.moduleType}
                    </span>
                    <span className="inline-block px-3 py-1 bg-blue-100 text-blue-700 rounded text-sm font-medium">
                      {question.cefrLevel}
                    </span>
                    {question.catalogCategory && (
                      <span className="inline-block px-3 py-1 bg-purple-100 text-purple-700 rounded text-sm font-medium">
                        {question.catalogCategory}
                      </span>
                    )}
                    <span className={`inline-block px-3 py-1 rounded text-sm font-medium ${
                      question.approvalStatus === 'APPROVED' ? 'bg-green-100 text-green-700' :
                      question.approvalStatus === 'REJECTED' ? 'bg-red-100 text-red-700' :
                      'bg-yellow-100 text-yellow-700'
                    }`}>
                      {question.approvalStatus}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold mb-2">{question.questionText}</h3>
                  {question.approvalNotes && (
                    <div className="bg-red-50 border border-red-200 rounded p-3 mb-2">
                      <p className="text-sm text-red-800">
                        <strong>Reason:</strong> {question.approvalNotes}
                      </p>
                    </div>
                  )}
                  {question.approvedBy && (
                    <p className="text-xs text-gray-600">
                      Approved by: {question.approvedBy} on {new Date(question.approvedAt).toLocaleString()}
                    </p>
                  )}
                </div>

                {question.approvalStatus === 'PENDING_REVIEW' && (
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleApprove(question.id)}
                      className="btn btn-sm bg-green-600 text-white hover:bg-green-700 flex items-center gap-1"
                      title="Approve this question"
                    >
                      <FiCheck size={16} /> Approve
                    </button>
                    <button
                      onClick={() => handleReject(question.id)}
                      className="btn btn-sm text-red-600 border border-red-600 hover:bg-red-50 flex items-center gap-1"
                      title="Reject this question"
                    >
                      <FiX size={16} /> Reject
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && questions.length > 0 && (
        <div className="flex justify-between items-center mt-6">
          <button
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0}
            className="btn btn-secondary flex items-center gap-2 disabled:opacity-50"
          >
            <FiChevronLeft size={18} /> Previous
          </button>
          <span className="text-gray-600">Page {currentPage + 1}</span>
          <button
            onClick={() => setCurrentPage(currentPage + 1)}
            disabled={questions.length < pageSize}
            className="btn btn-secondary flex items-center gap-2 disabled:opacity-50"
          >
            Next <FiChevronRight size={18} />
          </button>
        </div>
      )}
    </div>
  );
};

export default QuestionCatalog;
