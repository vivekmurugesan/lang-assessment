import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiPlus, FiEdit2, FiTrash2, FiChevronDown, FiChevronUp, FiZap, FiHome } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import GenerationProgressModal from '../../components/GenerationProgressModal';
import ModuleConfigurationTable from '../../components/ModuleConfigurationTable';

const AssessmentSetup = () => {
  const navigate = useNavigate();
  const [assessments, setAssessments] = useState([]);
  const [languages, setLanguages] = useState([]);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [expandedAssessment, setExpandedAssessment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    languageId: '',
  });

  useEffect(() => {
    loadAssessments();
    loadLanguages();
  }, []);

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

  const loadLanguages = async () => {
    try {
      const response = await api.get('/languages');
      setLanguages(response.data.data);
    } catch (error) {
      console.error('Failed to load languages:', error);
    }
  };

  const handleCreateAssessment = async (e) => {
    e.preventDefault();
    try {
      await api.post('/assessments', {
        title: formData.title,
        description: formData.description,
        languageId: parseInt(formData.languageId),
        status: 'DRAFT',
      });
      setFormData({ title: '', description: '', languageId: '' });
      setShowCreateForm(false);
      loadAssessments();
    } catch (error) {
      console.error('Failed to create assessment:', error);
    }
  };

  const handleDeleteAssessment = async (id) => {
    if (window.confirm('Are you sure you want to delete this assessment? This action cannot be undone.')) {
      try {
        await api.delete(`/assessments/${id}`);
        toast.success('Assessment deleted successfully');
        loadAssessments();
      } catch (error) {
        console.error('Failed to delete assessment:', error);
        toast.error('Failed to delete assessment. Please try again.');
      }
    }
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
            <h1 className="page-title">Assessment Setup</h1>
            <p className="page-subtitle">Create and configure language assessments</p>
          </div>
        </div>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn btn-primary flex items-center gap-2"
        >
          <FiPlus size={18} /> New Assessment
        </button>
      </div>

      {showCreateForm && (
        <div className="card mb-6">
          <h2 className="text-xl font-bold mb-4">Create New Assessment</h2>
          <form onSubmit={handleCreateAssessment} className="space-y-4">
            <div>
              <label className="form-label">Assessment Title</label>
              <input
                type="text"
                required
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                className="form-input"
                placeholder="e.g., English Proficiency Test"
              />
            </div>
            <div>
              <label className="form-label">Description</label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="form-input"
                placeholder="Assessment description..."
                rows="3"
              />
            </div>
            <div>
              <label className="form-label">Language</label>
              <select
                required
                value={formData.languageId}
                onChange={(e) => setFormData({ ...formData, languageId: e.target.value })}
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
            <div className="flex gap-2">
              <button type="submit" className="btn btn-primary">
                Create Assessment
              </button>
              <button
                type="button"
                onClick={() => setShowCreateForm(false)}
                className="btn btn-secondary"
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <div className="text-center py-8">Loading assessments...</div>
      ) : assessments.length === 0 ? (
        <div className="card text-center py-8">
          <p className="text-gray-600">No assessments yet. Create one to get started!</p>
        </div>
      ) : (
        <div className="space-y-3">
          {assessments.map((assessment) => (
            <AssessmentItem
              key={assessment.id}
              assessment={assessment}
              expanded={expandedAssessment === assessment.id}
              onToggleExpand={() =>
                setExpandedAssessment(expandedAssessment === assessment.id ? null : assessment.id)
              }
              onDelete={() => handleDeleteAssessment(assessment.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const AssessmentItem = ({ assessment, expanded, onToggleExpand, onDelete }) => {
  const navigate = useNavigate();
  const [modules, setModules] = useState([]);
  const [showModuleConfig, setShowModuleConfig] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [showCatalogSelection, setShowCatalogSelection] = useState(false);
  const [catalogSelecting, setCatalogSelecting] = useState(false);
  const [questionStatus, setQuestionStatus] = useState(null);
  const [progressModal, setProgressModal] = useState({
    isOpen: false,
    status: 'generating', // 'generating', 'complete', 'error'
    progress: 0,
    message: 'Initializing question generation...',
    error: null,
  });

  useEffect(() => {
    if (expanded) {
      loadModules();
      loadQuestionStatus();
    }
  }, [expanded]);

  const loadModules = async () => {
    try {
      const response = await api.get(`/admin/assessments/${assessment.id}/modules`);
      setModules(response.data.data);
    } catch (error) {
      console.error('Failed to load modules:', error);
    }
  };

  const loadQuestionStatus = async () => {
    try {
      const response = await api.get(`/admin/questions/assessments/${assessment.id}/status`);
      if (response.data.success) {
        setQuestionStatus(response.data.data);
      }
    } catch (error) {
      console.error('Failed to load question status:', error);
    }
  };

  const handleConfigurationChange = () => {
    setShowModuleConfig(false);
    loadModules();
    loadQuestionStatus();
  };

  const handleGenerateQuestions = async () => {
    if (modules.length === 0) {
      toast.error('Please add at least one module before generating questions');
      return;
    }

    setGenerating(true);
    setProgressModal({
      isOpen: true,
      status: 'generating',
      progress: 10,
      message: 'Preparing modules for generation...',
      error: null,
    });

    try {
      // Simulate progress updates
      const progressInterval = setInterval(() => {
        setProgressModal((prev) => {
          if (prev.progress < 80) {
            const newProgress = Math.min(80, prev.progress + Math.random() * 20);
            return {
              ...prev,
              progress: newProgress,
              message: 'Calling Gemini API to generate questions...',
            };
          }
          return prev;
        });
      }, 1000);

      const response = await api.post(`/admin/questions/generate/${assessment.id}`);

      clearInterval(progressInterval);

      const questionCount = response.data.data?.length || 0;

      setProgressModal({
        isOpen: true,
        status: 'complete',
        progress: 100,
        message: `Successfully generated ${questionCount} questions! Redirecting to review page...`,
        error: null,
      });

      setTimeout(() => {
        setProgressModal({ isOpen: false, status: 'generating', progress: 0, message: '', error: null });
        setGenerating(false);
        loadQuestionStatus();
        toast.success(`Generated ${questionCount} questions successfully!`);
        navigate(`/admin/questions/review/${assessment.id}`);
      }, 2000);
    } catch (error) {
      console.error('Failed to generate questions:', error);
      let errorMessage = error.response?.data?.message || error.message || 'Unknown error occurred';

      // Provide specific guidance for common errors
      if (error.response?.status === 403) {
        errorMessage = 'You do not have permission to generate questions. Please ensure you have ADMIN or EVALUATOR role.';
      } else if (errorMessage.includes('daily quota')) {
        errorMessage += ' You can use a paid API key or wait until tomorrow (UTC midnight).';
      }

      setProgressModal({
        isOpen: true,
        status: 'error',
        progress: 0,
        message: 'Question generation failed',
        error: errorMessage,
      });

      setGenerating(false);
    }
  };

  const handleSelectFromCatalog = async (moduleCounts) => {
    // Validate that at least one module has questions selected
    const hasValidCounts = Object.values(moduleCounts).some(count => count > 0);
    if (!hasValidCounts) {
      toast.error('Please select at least one question from the catalog');
      return;
    }

    setCatalogSelecting(true);
    setShowCatalogSelection(false);

    try {
      await api.post(
        `/admin/questions/assessments/${assessment.id}/select-questions`,
        moduleCounts
      );

      toast.success('Questions selected from catalog successfully!');
      loadQuestionStatus();
      setCatalogSelecting(false);
    } catch (error) {
      console.error('Failed to select questions from catalog:', error);
      let errorMessage = error.response?.data?.message || error.message || 'Unknown error occurred';

      // Provide specific guidance for common errors
      if (error.response?.status === 403) {
        errorMessage = 'You do not have permission to select questions. Please ensure you have ADMIN or EVALUATOR role.';
      } else if (errorMessage.includes('No approved questions')) {
        errorMessage = 'No approved questions available in the catalog. Please generate and approve questions first.';
      }

      toast.error(errorMessage);
      setCatalogSelecting(false);
    }
  };

  return (
    <>
      <GenerationProgressModal
        isOpen={progressModal.isOpen}
        status={progressModal.status}
        progress={progressModal.progress}
        message={progressModal.message}
        error={progressModal.error}
        onClose={() => setProgressModal({ isOpen: false, status: 'generating', progress: 0, message: '', error: null })}
      />
      <div className="card">
        <div className="flex justify-between items-center cursor-pointer" onClick={onToggleExpand}>
        <div className="flex-1">
          <h3 className="font-bold text-lg">{assessment.title}</h3>
          <p className="text-sm text-gray-600">{assessment.languageName}</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded text-sm font-medium">
            {assessment.status}
          </span>
          {expanded ? <FiChevronUp size={20} /> : <FiChevronDown size={20} />}
        </div>
      </div>

      {expanded && (
        <div className="mt-4 pt-4 border-t">
          {assessment.description && (
            <p className="text-gray-600 mb-4">{assessment.description}</p>
          )}

          {questionStatus && (
            <div className="mb-4">
              <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div className="bg-blue-50 border border-blue-200 rounded p-3">
                  <p className="text-xs text-gray-600">Generated</p>
                  <p className="text-2xl font-bold text-blue-600">{questionStatus.generated || 0}</p>
                </div>
                <div className="bg-green-50 border border-green-200 rounded p-3">
                  <p className="text-xs text-gray-600">Approved</p>
                  <p className="text-2xl font-bold text-green-600">{questionStatus.approved || 0}</p>
                </div>
                <div className="bg-yellow-50 border border-yellow-200 rounded p-3">
                  <p className="text-xs text-gray-600">Pending</p>
                  <p className="text-2xl font-bold text-yellow-600">{questionStatus.pending || 0}</p>
                </div>
                <div className="bg-red-50 border border-red-200 rounded p-3">
                  <p className="text-xs text-gray-600">Rejected</p>
                  <p className="text-2xl font-bold text-red-600">{questionStatus.rejected || 0}</p>
                </div>
              </div>
              {questionStatus.pending > 0 && (
                <div className="mt-3 text-xs text-yellow-700 bg-yellow-50 p-2 rounded border border-yellow-200">
                  ⚠️ {questionStatus.pending} questions pending review in Catalog
                </div>
              )}
            </div>
          )}

          <div className="mb-4">
            <div className="flex justify-between items-center mb-3">
              <h4 className="font-bold text-lg">Assessment Modules</h4>
              <button
                onClick={() => setShowModuleConfig(!showModuleConfig)}
                className="btn btn-sm btn-primary flex items-center gap-1"
              >
                <FiPlus size={14} /> {showModuleConfig ? 'Close Configuration' : 'Configure Modules'}
              </button>
            </div>

            {showModuleConfig && (
              <div className="mb-4">
                <ModuleConfigurationTable
                  assessmentId={assessment.id}
                  existingModules={modules}
                  onConfigurationChange={handleConfigurationChange}
                />
              </div>
            )}

            {modules.length === 0 ? (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-4 text-center">
                <p className="text-gray-700 font-medium mb-2">No modules configured yet</p>
                <p className="text-sm text-gray-600 mb-3">Click "Configure Modules" to set up your assessment</p>
                <button
                  onClick={() => setShowModuleConfig(true)}
                  className="btn btn-sm btn-primary inline-flex items-center gap-1"
                >
                  <FiPlus size={14} /> Configure Modules
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                <p className="text-sm font-medium text-gray-700 mb-2">Configured Modules:</p>
                {modules.map((module) => (
                  <div key={module.id} className="flex justify-between items-center bg-gray-50 p-3 rounded">
                    <div>
                      <p className="font-medium">{module.moduleType.replace(/_/g, ' ')}</p>
                      <p className="text-xs text-gray-600">
                        {module.numQuestions} questions • {module.difficultyLevel || 'Mixed'}
                      </p>
                    </div>
                    <button
                      onClick={() => setShowModuleConfig(true)}
                      className="text-blue-500 hover:text-blue-700 text-sm font-medium"
                    >
                      Edit
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="flex gap-2 pt-4 border-t">
            <button
              onClick={handleGenerateQuestions}
              disabled={generating}
              className="btn btn-sm bg-indigo-600 text-white hover:bg-indigo-700 disabled:bg-gray-400 flex items-center gap-1"
            >
              <FiZap size={14} /> {generating ? 'Generating...' : 'Generate Questions'}
            </button>
            <button
              onClick={() => setShowCatalogSelection(true)}
              disabled={catalogSelecting || modules.length === 0}
              className="btn btn-sm bg-green-600 text-white hover:bg-green-700 disabled:bg-gray-400 flex items-center gap-1"
            >
              <FiPlus size={14} /> {catalogSelecting ? 'Selecting...' : 'Select from Catalog'}
            </button>
            <button
              onClick={() => navigate(`/admin/questions/review/${assessment.id}`)}
              className="btn btn-secondary btn-sm flex items-center gap-1"
            >
              <FiEdit2 size={14} /> Review Questions
            </button>
            <button
              onClick={onDelete}
              className="btn btn-sm text-red-600 border border-red-600 hover:bg-red-50"
            >
              <FiTrash2 size={14} /> Delete Assessment
            </button>
          </div>

          {showCatalogSelection && (
            <CatalogSelectionModal
              modules={modules}
              onConfirm={handleSelectFromCatalog}
              onClose={() => setShowCatalogSelection(false)}
            />
          )}
        </div>
      )}
      </div>
    </>
  );
};

const CatalogSelectionModal = ({ modules, onConfirm, onClose }) => {
  const [moduleCounts, setModuleCounts] = useState(
    modules.reduce((acc, m) => {
      acc[m.moduleType] = m.numQuestions || 0;
      return acc;
    }, {})
  );

  const handleConfirm = () => {
    const counts = Object.entries(moduleCounts).reduce((acc, [moduleType, count]) => {
      acc[moduleType] = parseInt(count) || 0;
      return acc;
    }, {});
    onConfirm(counts);
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-lg max-w-md w-full p-6">
        <h2 className="text-xl font-bold mb-4">Select Questions from Catalog</h2>
        <p className="text-gray-600 text-sm mb-4">
          Specify how many questions to select from the approved catalog for each module.
        </p>

        <div className="bg-blue-50 border border-blue-200 rounded p-3 mb-4">
          <p className="text-xs text-blue-800">
            <span className="font-medium">ℹ️ Note:</span> Make sure questions have been generated and approved in the catalog first.
          </p>
        </div>

        <div className="space-y-4 mb-6 max-h-80 overflow-y-auto">
          {modules.map((module) => (
            <div key={module.id} className="border rounded p-3">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {module.moduleType.replace(/_/g, ' ')}
              </label>
              <input
                type="number"
                min="0"
                value={moduleCounts[module.moduleType] || 0}
                onChange={(e) =>
                  setModuleCounts((prev) => ({
                    ...prev,
                    [module.moduleType]: e.target.value,
                  }))
                }
                className="form-input w-full"
                placeholder="Number of questions"
              />
              <p className="text-xs text-gray-500 mt-1">
                Configured: {module.numQuestions} questions
              </p>
            </div>
          ))}
        </div>

        <div className="flex gap-2">
          <button
            onClick={handleConfirm}
            className="btn btn-primary flex-1"
          >
            Select Questions
          </button>
          <button
            onClick={onClose}
            className="btn btn-secondary flex-1"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default AssessmentSetup;
