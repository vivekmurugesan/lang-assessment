import React, { useState, useEffect } from 'react';
import { FiPlus, FiEdit2, FiTrash2, FiChevronDown, FiChevronUp } from 'react-icons/fi';
import api from '../../api/axiosConfig';

const AssessmentSetup = () => {
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
    if (window.confirm('Are you sure you want to delete this assessment?')) {
      try {
        await api.delete(`/assessments/${id}`);
        loadAssessments();
      } catch (error) {
        console.error('Failed to delete assessment:', error);
      }
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="page-title">Assessment Setup</h1>
          <p className="page-subtitle">Create and configure language assessments</p>
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
  const [modules, setModules] = useState([]);
  const [showModuleForm, setShowModuleForm] = useState(false);
  const [moduleFormData, setModuleFormData] = useState({
    moduleType: '',
    numQuestions: 10,
    difficultyLevel: 'INTERMEDIATE',
    isEnabled: true,
  });

  const moduleTypes = [
    'LISTENING',
    'READING',
    'SPOKEN_INTERACTION',
    'SPOKEN_PRODUCTION',
    'WRITING',
  ];

  useEffect(() => {
    if (expanded) {
      loadModules();
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

  const handleAddModule = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/admin/assessments/${assessment.id}/modules`, moduleFormData);
      setModuleFormData({
        moduleType: '',
        numQuestions: 10,
        difficultyLevel: 'INTERMEDIATE',
        isEnabled: true,
      });
      setShowModuleForm(false);
      loadModules();
    } catch (error) {
      console.error('Failed to add module:', error);
    }
  };

  const handleDeleteModule = async (moduleId) => {
    if (window.confirm('Are you sure?')) {
      try {
        await api.delete(`/admin/assessments/${assessment.id}/modules/${moduleId}`);
        loadModules();
      } catch (error) {
        console.error('Failed to delete module:', error);
      }
    }
  };

  return (
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

          <div className="mb-4">
            <div className="flex justify-between items-center mb-3">
              <h4 className="font-bold">Assessment Modules</h4>
              <button
                onClick={() => setShowModuleForm(!showModuleForm)}
                className="btn btn-sm btn-primary flex items-center gap-1"
              >
                <FiPlus size={14} /> Add Module
              </button>
            </div>

            {showModuleForm && (
              <form onSubmit={handleAddModule} className="bg-gray-50 p-4 rounded mb-3 space-y-3">
                <select
                  required
                  value={moduleFormData.moduleType}
                  onChange={(e) =>
                    setModuleFormData({ ...moduleFormData, moduleType: e.target.value })
                  }
                  className="form-input"
                >
                  <option value="">Select Module Type</option>
                  {moduleTypes.map((type) => (
                    <option key={type} value={type}>
                      {type.replace(/_/g, ' ')}
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  min="1"
                  value={moduleFormData.numQuestions}
                  onChange={(e) =>
                    setModuleFormData({
                      ...moduleFormData,
                      numQuestions: parseInt(e.target.value),
                    })
                  }
                  className="form-input"
                  placeholder="Number of Questions"
                />
                <select
                  value={moduleFormData.difficultyLevel}
                  onChange={(e) =>
                    setModuleFormData({
                      ...moduleFormData,
                      difficultyLevel: e.target.value,
                    })
                  }
                  className="form-input"
                >
                  <option value="EASY">Easy</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="HARD">Hard</option>
                </select>
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={moduleFormData.isEnabled}
                    onChange={(e) =>
                      setModuleFormData({ ...moduleFormData, isEnabled: e.target.checked })
                    }
                  />
                  Enabled
                </label>
                <div className="flex gap-2">
                  <button type="submit" className="btn btn-primary btn-sm">
                    Add Module
                  </button>
                  <button
                    type="button"
                    onClick={() => setShowModuleForm(false)}
                    className="btn btn-secondary btn-sm"
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}

            {modules.length === 0 ? (
              <p className="text-gray-600 text-sm">No modules added yet</p>
            ) : (
              <div className="space-y-2">
                {modules.map((module) => (
                  <div key={module.id} className="flex justify-between items-center bg-gray-50 p-3 rounded">
                    <div>
                      <p className="font-medium">{module.moduleType}</p>
                      <p className="text-xs text-gray-600">
                        {module.numQuestions} questions • {module.difficultyLevel || 'Mixed'}
                      </p>
                    </div>
                    <button
                      onClick={() => handleDeleteModule(module.id)}
                      className="text-red-500 hover:text-red-700"
                    >
                      <FiTrash2 size={16} />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="flex gap-2 pt-4 border-t">
            <button className="btn btn-secondary btn-sm flex items-center gap-1">
              <FiEdit2 size={14} /> Manage Questions
            </button>
            <button
              onClick={onDelete}
              className="btn btn-sm text-red-600 border border-red-600 hover:bg-red-50"
            >
              <FiTrash2 size={14} /> Delete Assessment
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AssessmentSetup;
