import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiPlus, FiCopy, FiTrash2, FiDownload, FiUpload, FiHome } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const CandidateOnboarding = () => {
  const navigate = useNavigate();
  const [assessments, setAssessments] = useState([]);
  const [selectedAssessment, setSelectedAssessment] = useState(null);
  const [candidates, setCandidates] = useState([]);
  const [showAddForm, setShowAddForm] = useState(false);
  const [showBulkForm, setShowBulkForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [copiedLink, setCopiedLink] = useState(null);
  const [assessmentStatus, setAssessmentStatus] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    email: '',
  });

  const [bulkData, setBulkData] = useState('');
  const [contentValidation, setContentValidation] = useState(null);

  useEffect(() => {
    loadAssessments();
  }, []);

  useEffect(() => {
    if (selectedAssessment) {
      loadCandidates(selectedAssessment);
      loadAssessmentStatus(selectedAssessment);
      validateAssessmentContent(selectedAssessment);
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
        `/admin/assessments/${assessmentId}/candidates?page=0&size=20`
      );
      setCandidates(response.data.data.content);
    } catch (error) {
      console.error('Failed to load candidates:', error);
    }
  };

  const loadAssessmentStatus = async (assessmentId) => {
    try {
      const response = await api.get(`/admin/questions/assessments/${assessmentId}/status`);
      if (response.data.success) {
        setAssessmentStatus(response.data.data);
      }
    } catch (error) {
      console.error('Failed to load assessment status:', error);
    }
  };

  const validateAssessmentContent = async (assessmentId) => {
    try {
      const response = await api.get(`/admin/questions/${assessmentId}/validate-content`);
      if (response.data.success) {
        setContentValidation(response.data.data);
      }
    } catch (error) {
      console.error('Failed to validate content:', error);
    }
  };

  const handleAddCandidate = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/admin/assessments/${selectedAssessment}/candidates`, {
        name: formData.name,
        email: formData.email,
      });
      setFormData({ name: '', email: '' });
      setShowAddForm(false);
      loadCandidates(selectedAssessment);
    } catch (error) {
      console.error('Failed to add candidate:', error);
    }
  };

  const handleBulkUpload = async (e) => {
    e.preventDefault();
    const lines = bulkData.trim().split('\n');
    const candidatesList = lines
      .map((line) => {
        const [name, email] = line.split(',').map((s) => s.trim());
        return name && email ? { name, email } : null;
      })
      .filter(Boolean);

    if (candidatesList.length === 0) {
      alert('No valid candidates found. Please use format: Name, Email');
      return;
    }

    try {
      await api.post(`/admin/assessments/${selectedAssessment}/candidates/bulk-upload`, candidatesList);
      setBulkData('');
      setShowBulkForm(false);
      loadCandidates(selectedAssessment);
      alert(`Successfully added ${candidatesList.length} candidates`);
    } catch (error) {
      console.error('Failed to add candidates:', error);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    setCopiedLink(text);
    setTimeout(() => setCopiedLink(null), 2000);
  };

  const downloadCandidateList = () => {
    const csv = candidates
      .map((c) => `${c.email},${c.secureLink}`)
      .join('\n');
    const element = document.createElement('a');
    element.setAttribute(
      'href',
      'data:text/csv;charset=utf-8,' + encodeURIComponent(`Email,Secure Link\n${csv}`)
    );
    element.setAttribute('download', 'candidates.csv');
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
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
            <h1 className="page-title">Candidate Onboarding</h1>
            <p className="page-subtitle">Manage candidates and generate secure links</p>
          </div>
        </div>
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
              {assessmentStatus && assessmentStatus.approved === 0 && (
                <div className="card mb-6 bg-red-50 border border-red-200">
                  <div className="flex items-start gap-3">
                    <div className="text-2xl">⚠️</div>
                    <div>
                      <h3 className="font-bold text-red-900 mb-1">Assessment Not Ready</h3>
                      <p className="text-sm text-red-800 mb-2">
                        No questions are approved for this assessment yet.
                      </p>
                      <p className="text-xs text-red-700 mb-3">
                        <strong>To proceed:</strong> Go to Assessment Setup → Generate Questions → Go to Catalog → Approve Questions
                      </p>
                      <button
                        onClick={() => navigate('/admin/assessments')}
                        className="text-sm font-medium text-red-600 hover:text-red-700 underline"
                      >
                        Go to Assessment Setup →
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {contentValidation && !contentValidation.isReadyForCandidates && (
                <div className="card mb-6 bg-red-50 border border-red-200">
                  <div className="flex items-start gap-3">
                    <div className="text-2xl">🔴</div>
                    <div>
                      <h3 className="font-bold text-red-900 mb-1">Content Not Ready for Candidates</h3>
                      <p className="text-sm text-red-800 mb-2">
                        Assessment has missing required content:
                      </p>
                      {contentValidation.issues && contentValidation.issues.length > 0 && (
                        <div className="mb-3">
                          {contentValidation.issues.map((issue, idx) => (
                            <p key={idx} className="text-xs text-red-700 mb-1">• {issue}</p>
                          ))}
                        </div>
                      )}
                      <p className="text-xs text-red-700 mb-3">
                        <strong>To proceed:</strong> Go to Assessment Setup → Validate Content Ready and resolve all issues
                      </p>
                      <button
                        onClick={() => navigate('/admin/assessments')}
                        className="text-sm font-medium text-red-600 hover:text-red-700 underline"
                      >
                        Go to Assessment Setup →
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {assessmentStatus && assessmentStatus.approved > 0 && contentValidation && contentValidation.isReadyForCandidates && (
                <div className="card mb-6 bg-green-50 border border-green-200 p-3">
                  <p className="text-sm text-green-800">
                    ✓ <strong>{assessmentStatus.approved} questions approved</strong> with all required content - Ready for candidates
                  </p>
                </div>
              )}

              <div className="flex gap-2 mb-6">
                <button
                  onClick={() => setShowAddForm(!showAddForm)}
                  disabled={!assessmentStatus || assessmentStatus.approved === 0 || !contentValidation || !contentValidation.isReadyForCandidates}
                  className="btn btn-primary flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  title={!assessmentStatus || assessmentStatus.approved === 0 ? 'Approve questions first' : !contentValidation || !contentValidation.isReadyForCandidates ? 'Validate and prepare all content first' : 'Add a candidate'}
                >
                  <FiPlus size={18} /> Add Candidate
                </button>
                <button
                  onClick={() => setShowBulkForm(!showBulkForm)}
                  disabled={!assessmentStatus || assessmentStatus.approved === 0 || !contentValidation || !contentValidation.isReadyForCandidates}
                  className="btn btn-secondary flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                  title={!assessmentStatus || assessmentStatus.approved === 0 ? 'Approve questions first' : !contentValidation || !contentValidation.isReadyForCandidates ? 'Validate and prepare all content first' : 'Bulk upload candidates'}
                >
                  <FiUpload size={18} /> Bulk Upload
                </button>
                {candidates.length > 0 && (
                  <button
                    onClick={downloadCandidateList}
                    className="btn btn-secondary flex items-center gap-2"
                  >
                    <FiDownload size={18} /> Download CSV
                  </button>
                )}
              </div>

              {showAddForm && (
                <div className="card mb-6">
                  <h2 className="text-xl font-bold mb-4">Add Individual Candidate</h2>
                  <form onSubmit={handleAddCandidate} className="space-y-4">
                    <div>
                      <label className="form-label">Candidate Name</label>
                      <input
                        type="text"
                        required
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        className="form-input"
                        placeholder="e.g., John Doe"
                      />
                    </div>
                    <div>
                      <label className="form-label">Email Address</label>
                      <input
                        type="email"
                        required
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        className="form-input"
                        placeholder="john@example.com"
                      />
                    </div>
                    <div className="flex gap-2">
                      <button type="submit" className="btn btn-primary">
                        Add Candidate
                      </button>
                      <button
                        type="button"
                        onClick={() => setShowAddForm(false)}
                        className="btn btn-secondary"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}

              {showBulkForm && (
                <div className="card mb-6">
                  <h2 className="text-xl font-bold mb-4">Bulk Upload Candidates</h2>
                  <p className="text-sm text-gray-600 mb-4">
                    Enter candidates in CSV format (one per line): Name, Email
                  </p>
                  <form onSubmit={handleBulkUpload} className="space-y-4">
                    <textarea
                      value={bulkData}
                      onChange={(e) => setBulkData(e.target.value)}
                      className="form-input font-mono"
                      placeholder="John Doe, john@example.com&#10;Jane Smith, jane@example.com"
                      rows="8"
                    />
                    <div className="flex gap-2">
                      <button type="submit" className="btn btn-primary">
                        Upload Candidates
                      </button>
                      <button
                        type="button"
                        onClick={() => setShowBulkForm(false)}
                        className="btn btn-secondary"
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}

              <div>
                <h2 className="text-xl font-bold mb-4">
                  Candidates ({candidates.length})
                </h2>
                {candidates.length === 0 ? (
                  <div className="card text-center py-8">
                    <p className="text-gray-600">No candidates added yet</p>
                  </div>
                ) : (
                  <div className="grid gap-3">
                    {candidates.map((candidate) => (
                      <CandidateRow
                        key={candidate.id}
                        candidate={candidate}
                        onCopy={copyToClipboard}
                        isCopied={copiedLink === candidate.secureLink}
                      />
                    ))}
                  </div>
                )}
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
};

const CandidateRow = ({ candidate, onCopy, isCopied }) => {
  const candidateLink = `${window.location.origin}/candidate-login?link=${candidate.secureLink}`;

  return (
    <div className="card p-4">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 items-center">
        <div>
          <p className="font-medium">{candidate.name}</p>
          <p className="text-sm text-gray-600">{candidate.email}</p>
        </div>
        <div>
          <p className="text-xs text-gray-600">Status</p>
          <p className="font-medium">{candidate.status}</p>
        </div>
        <div>
          <p className="text-xs text-gray-600">Secure Link</p>
          <code className="text-xs bg-gray-100 px-2 py-1 rounded block truncate">
            {candidate.secureLink.substring(0, 12)}...
          </code>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => onCopy(candidateLink)}
            className="btn btn-sm btn-secondary flex items-center gap-1"
            title="Copy assessment link to clipboard"
          >
            <FiCopy size={14} /> {isCopied ? 'Copied!' : 'Copy'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CandidateOnboarding;
