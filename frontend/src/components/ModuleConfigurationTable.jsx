import React, { useState, useEffect } from 'react';
import { FiCheck, FiX } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../api/axiosConfig';

const DEFAULT_CONFIG = {
  LISTENING: { numQuestions: 10, difficultyLevel: 'INTERMEDIATE', enabled: true },
  READING: { numQuestions: 10, difficultyLevel: 'INTERMEDIATE', enabled: true },
  SPOKEN_INTERACTION: { numQuestions: 8, difficultyLevel: 'INTERMEDIATE', enabled: true },
  SPOKEN_PRODUCTION: { numQuestions: 8, difficultyLevel: 'INTERMEDIATE', enabled: true },
  WRITING: { numQuestions: 5, difficultyLevel: 'INTERMEDIATE', enabled: true },
};

const MODULE_DESCRIPTIONS = {
  LISTENING: 'Audio comprehension and listening skills',
  READING: 'Reading comprehension and text analysis',
  SPOKEN_INTERACTION: 'Conversational interaction and dialogue',
  SPOKEN_PRODUCTION: 'Speaking fluency and pronunciation',
  WRITING: 'Writing composition and grammar',
};

const QUESTION_OPTIONS = [5, 8, 10, 12, 15, 20];
const DIFFICULTY_OPTIONS = ['EASY', 'INTERMEDIATE', 'HARD'];

const ModuleConfigurationTable = ({ assessmentId, onConfigurationChange, existingModules = [] }) => {
  const [configuration, setConfiguration] = useState(DEFAULT_CONFIG);
  const [isApplying, setIsApplying] = useState(false);

  useEffect(() => {
    // Update configuration based on existing modules
    if (existingModules.length > 0) {
      const updatedConfig = { ...DEFAULT_CONFIG };
      existingModules.forEach((module) => {
        if (updatedConfig[module.moduleType]) {
          updatedConfig[module.moduleType] = {
            numQuestions: module.numQuestions,
            difficultyLevel: module.difficultyLevel || 'INTERMEDIATE',
            enabled: module.isEnabled !== false,
            id: module.id, // Track existing module ID
          };
        }
      });
      setConfiguration(updatedConfig);
    }
  }, [existingModules]);

  const toggleModule = (moduleType) => {
    setConfiguration((prev) => ({
      ...prev,
      [moduleType]: {
        ...prev[moduleType],
        enabled: !prev[moduleType].enabled,
      },
    }));
  };

  const updateModuleConfig = (moduleType, field, value) => {
    setConfiguration((prev) => ({
      ...prev,
      [moduleType]: {
        ...prev[moduleType],
        [field]: value,
      },
    }));
  };

  const applyDefaultConfiguration = async () => {
    setIsApplying(true);
    try {
      // Delete existing modules
      if (existingModules.length > 0) {
        await Promise.all(
          existingModules.map((module) =>
            api.delete(`/admin/assessments/${assessmentId}/modules/${module.id}`)
          )
        );
      }

      // Add modules based on current configuration
      const modulesToAdd = Object.entries(configuration)
        .filter(([_, config]) => config.enabled)
        .map(([moduleType, config]) => ({
          moduleType,
          numQuestions: config.numQuestions,
          difficultyLevel: config.difficultyLevel,
          isEnabled: true,
        }));

      await Promise.all(
        modulesToAdd.map((module) =>
          api.post(`/admin/assessments/${assessmentId}/modules`, module)
        )
      );

      toast.success('Modules configured successfully!');
      onConfigurationChange?.();
    } catch (error) {
      console.error('Failed to apply configuration:', error);
      toast.error('Failed to apply configuration. Please try again.');
    } finally {
      setIsApplying(false);
    }
  };

  const applyDefaultPreset = () => {
    setConfiguration(JSON.parse(JSON.stringify(DEFAULT_CONFIG)));
  };

  const enabledModuleCount = Object.values(configuration).filter((c) => c.enabled).length;
  const totalQuestions = Object.values(configuration)
    .filter((c) => c.enabled)
    .reduce((sum, c) => sum + c.numQuestions, 0);

  return (
    <div className="card">
      <div className="mb-4">
        <div className="flex justify-between items-center mb-4">
          <div>
            <h3 className="text-lg font-bold mb-1">Configure Modules</h3>
            <p className="text-sm text-gray-600">
              Select modules and configure the number of questions for each
            </p>
          </div>
          <button
            onClick={applyDefaultPreset}
            className="btn btn-sm btn-outline"
            title="Reset to default configuration"
          >
            Reset to Defaults
          </button>
        </div>
      </div>

      <div className="overflow-x-auto mb-4">
        <table className="w-full">
          <thead>
            <tr className="border-b border-gray-300 bg-gray-50">
              <th className="text-left py-3 px-4 font-semibold text-sm">Module</th>
              <th className="text-left py-3 px-4 font-semibold text-sm">Description</th>
              <th className="text-center py-3 px-4 font-semibold text-sm">Enabled</th>
              <th className="text-center py-3 px-4 font-semibold text-sm">Questions</th>
              <th className="text-center py-3 px-4 font-semibold text-sm">Difficulty</th>
            </tr>
          </thead>
          <tbody>
            {Object.entries(configuration).map(([moduleType, config]) => (
              <tr
                key={moduleType}
                className={`border-b border-gray-200 transition-colors ${
                  config.enabled ? 'bg-white hover:bg-blue-50' : 'bg-gray-50 opacity-60'
                }`}
              >
                <td className="py-4 px-4 font-medium text-sm">{moduleType.replace(/_/g, ' ')}</td>
                <td className="py-4 px-4 text-sm text-gray-600">{MODULE_DESCRIPTIONS[moduleType]}</td>
                <td className="py-4 px-4 text-center">
                  <label className="flex justify-center">
                    <input
                      type="checkbox"
                      checked={config.enabled}
                      onChange={() => toggleModule(moduleType)}
                      className="w-5 h-5 cursor-pointer rounded border-gray-300"
                    />
                  </label>
                </td>
                <td className="py-4 px-4 text-center">
                  <select
                    value={config.numQuestions}
                    onChange={(e) =>
                      updateModuleConfig(moduleType, 'numQuestions', parseInt(e.target.value))
                    }
                    disabled={!config.enabled}
                    className={`form-input text-sm text-center max-w-20 mx-auto ${
                      !config.enabled ? 'bg-gray-200 cursor-not-allowed' : ''
                    }`}
                  >
                    {QUESTION_OPTIONS.map((num) => (
                      <option key={num} value={num}>
                        {num}
                      </option>
                    ))}
                  </select>
                </td>
                <td className="py-4 px-4 text-center">
                  <select
                    value={config.difficultyLevel}
                    onChange={(e) =>
                      updateModuleConfig(moduleType, 'difficultyLevel', e.target.value)
                    }
                    disabled={!config.enabled}
                    className={`form-input text-sm text-center max-w-28 mx-auto ${
                      !config.enabled ? 'bg-gray-200 cursor-not-allowed' : ''
                    }`}
                  >
                    {DIFFICULTY_OPTIONS.map((level) => (
                      <option key={level} value={level}>
                        {level}
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded p-4 mb-4">
        <p className="text-sm text-blue-800">
          <span className="font-semibold">Summary:</span> {enabledModuleCount} module(s) selected,{' '}
          {totalQuestions} total questions
        </p>
      </div>

      <div className="flex gap-2">
        <button
          onClick={applyDefaultConfiguration}
          disabled={isApplying || enabledModuleCount === 0}
          className="btn btn-primary flex items-center gap-2"
        >
          {isApplying ? (
            <>
              <span className="animate-spin">⚙️</span> Applying Configuration...
            </>
          ) : (
            <>
              <FiCheck size={16} /> Apply Configuration
            </>
          )}
        </button>
        {enabledModuleCount === 0 && (
          <p className="text-sm text-red-600 flex items-center gap-1">
            <FiX size={14} /> Select at least one module
          </p>
        )}
      </div>
    </div>
  );
};

export default ModuleConfigurationTable;
