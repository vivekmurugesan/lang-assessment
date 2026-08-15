import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FiChevronLeft, FiChevronRight, FiVolume2, FiMic, FiPause, FiSave, FiSend } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const AssessmentTaking = () => {
  const { secureLink } = useParams();
  const navigate = useNavigate();
  const [assessment, setAssessment] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [submission, setSubmission] = useState(null);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [responses, setResponses] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);

  useEffect(() => {
    loadAssessment();
  }, [secureLink]);

  const loadAssessment = async () => {
    try {
      const [assessmentRes, questionsRes, startRes] = await Promise.all([
        api.get(`/candidate/assessments/${secureLink}`),
        api.get(`/candidate/assessments/${secureLink}/questions`),
        api.post(`/candidate/assessments/${secureLink}/start`)
      ]);

      setAssessment(assessmentRes.data.data);
      setQuestions(questionsRes.data.data || []);
      setSubmission(startRes.data.data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load assessment:', error);
      toast.error('Failed to load assessment');
      setLoading(false);
    }
  };

  const currentQuestion = questions[currentQuestionIndex];

  const handleResponseChange = (value, type = 'text') => {
    setResponses(prev => ({
      ...prev,
      [currentQuestion?.id]: {
        ...prev[currentQuestion?.id],
        [type]: value
      }
    }));
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;
      audioChunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          audioChunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' });
        const audioUrl = URL.createObjectURL(audioBlob);
        handleResponseChange(audioUrl, 'audio');
      };

      mediaRecorder.start();
      setIsRecording(true);
    } catch (error) {
      toast.error('Microphone access denied');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current) {
      mediaRecorderRef.current.stop();
      mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
      setIsRecording(false);
    }
  };

  const saveCurrentResponse = async () => {
    if (!submission || !currentQuestion) return;

    try {
      const response = responses[currentQuestion.id] || {};
      await api.post(
        `/candidate/submissions/${submission.id}/responses?questionId=${currentQuestion.id}`,
        {
          responseText: response.text || '',
          audioFilePath: response.audio || null,
          selectedOption: response.option || null
        }
      );
      toast.success('Response saved');
    } catch (error) {
      console.error('Failed to save response:', error);
      toast.error('Failed to save response');
    }
  };

  const handleNext = async () => {
    if (currentQuestionIndex < questions.length - 1) {
      await saveCurrentResponse();
      setCurrentQuestionIndex(prev => prev + 1);
    }
  };

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1);
    }
  };

  const handleSubmit = async () => {
    if (!window.confirm('Are you sure you want to submit your assessment? You cannot make changes after submission.')) {
      return;
    }

    setSubmitting(true);
    try {
      await saveCurrentResponse();
      await api.post(`/candidate/submissions/${submission.id}/submit`);
      toast.success('Assessment submitted successfully!');
      navigate(`/assessment/${secureLink}/results`);
    } catch (error) {
      console.error('Failed to submit assessment:', error);
      toast.error('Failed to submit assessment');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-gray-600">Loading assessment...</p>
        </div>
      </div>
    );
  }

  if (!assessment || questions.length === 0) {
    return (
      <div className="p-6">
        <div className="card text-center py-12">
          <p className="text-gray-600">Assessment data not available</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto p-6">
        {/* Header */}
        <div className="mb-6">
          <h1 className="page-title">{assessment.title}</h1>
          <p className="page-subtitle">Question {currentQuestionIndex + 1} of {questions.length}</p>
        </div>

        {/* Progress Bar */}
        <div className="mb-6">
          <div className="flex justify-between text-sm text-gray-600 mb-2">
            <span>Progress</span>
            <span>{Math.round(((currentQuestionIndex + 1) / questions.length) * 100)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-indigo-600 h-2 rounded-full transition-all duration-300"
              style={{ width: `${((currentQuestionIndex + 1) / questions.length) * 100}%` }}
            />
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <div className="card p-8">
              {/* Module Type Badge */}
              <div className="mb-4">
                <span className="inline-block px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm font-medium">
                  {currentQuestion?.moduleType}
                </span>
              </div>

              {/* Question Text */}
              <h2 className="text-2xl font-bold mb-6 text-gray-800">
                {currentQuestion?.questionText}
              </h2>

              {/* Question Content */}
              <div className="mb-8">
                {/* Audio Playback for Listening */}
                {currentQuestion?.moduleType === 'LISTENING' && (
                  <div className="mb-6 p-4 bg-blue-50 rounded-lg">
                    <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                      <FiVolume2 size={18} />
                      Play Audio
                    </button>
                    <p className="text-sm text-gray-600 mt-2">Click to listen to the audio content</p>
                  </div>
                )}

                {/* Multiple Choice Options */}
                {['READING', 'LISTENING'].includes(currentQuestion?.moduleType) && (
                  <div className="space-y-3">
                    {['A', 'B', 'C', 'D'].map(option => (
                      <label key={option} className="flex items-center p-4 border-2 border-gray-300 rounded-lg cursor-pointer hover:border-indigo-500 transition"
                        style={{
                          borderColor: responses[currentQuestion?.id]?.option === option ? '#4F46E5' : '#D1D5DB'
                        }}>
                        <input
                          type="radio"
                          name={`question-${currentQuestion?.id}`}
                          value={option}
                          checked={responses[currentQuestion?.id]?.option === option}
                          onChange={(e) => handleResponseChange(e.target.value, 'option')}
                          className="mr-3"
                        />
                        <span className="font-medium mr-3">{option}.</span>
                        <span>Option text here</span>
                      </label>
                    ))}
                  </div>
                )}

                {/* Recording for Speaking */}
                {currentQuestion?.moduleType === 'SPOKEN_INTERACTION' && (
                  <div className="space-y-4">
                    <div className="p-6 bg-red-50 rounded-lg border border-red-200">
                      <div className="flex items-center justify-center">
                        <button
                          onClick={isRecording ? stopRecording : startRecording}
                          className={`flex items-center gap-2 px-6 py-3 rounded-full font-semibold transition ${
                            isRecording
                              ? 'bg-red-600 hover:bg-red-700 text-white'
                              : 'bg-red-500 hover:bg-red-600 text-white'
                          }`}
                        >
                          <FiMic size={20} />
                          {isRecording ? 'Stop Recording' : 'Start Recording'}
                        </button>
                      </div>
                      {isRecording && (
                        <p className="text-center text-red-600 font-semibold mt-2">Recording in progress...</p>
                      )}
                      {responses[currentQuestion?.id]?.audio && (
                        <div className="mt-4">
                          <audio
                            src={responses[currentQuestion?.id]?.audio}
                            controls
                            className="w-full"
                          />
                        </div>
                      )}
                    </div>
                  </div>
                )}

                {/* Text Input for Writing */}
                {currentQuestion?.moduleType === 'WRITING' && (
                  <div>
                    <textarea
                      value={responses[currentQuestion?.id]?.text || ''}
                      onChange={(e) => handleResponseChange(e.target.value, 'text')}
                      placeholder="Write your response here (minimum 150 words)..."
                      className="form-input min-h-64 p-4 border-2 border-gray-300 rounded"
                    />
                    <p className="text-xs text-gray-500 mt-2">
                      Word count: {(responses[currentQuestion?.id]?.text || '').split(/\s+/).filter(w => w).length}
                    </p>
                  </div>
                )}
              </div>

              {/* Save Response Button */}
              <button
                onClick={saveCurrentResponse}
                className="flex items-center gap-2 px-4 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
              >
                <FiSave size={16} />
                Save Response
              </button>
            </div>
          </div>

          {/* Sidebar - Question List */}
          <div className="lg:col-span-1">
            <div className="card">
              <h3 className="font-bold text-lg mb-4">Questions</h3>
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {questions.map((q, idx) => (
                  <button
                    key={q.id}
                    onClick={() => setCurrentQuestionIndex(idx)}
                    className={`w-full p-3 text-left rounded transition ${
                      idx === currentQuestionIndex
                        ? 'bg-indigo-600 text-white'
                        : responses[q.id]
                        ? 'bg-green-100 text-green-900 hover:bg-green-200'
                        : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                    }`}
                  >
                    <div className="font-medium">Q{idx + 1}</div>
                    <div className="text-xs opacity-75">{q.moduleType}</div>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Navigation Buttons */}
        <div className="mt-6 flex justify-between">
          <button
            onClick={handlePrevious}
            disabled={currentQuestionIndex === 0}
            className="flex items-center gap-2 px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <FiChevronLeft size={18} />
            Previous
          </button>

          {currentQuestionIndex === questions.length - 1 ? (
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="flex items-center gap-2 px-6 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
            >
              <FiSend size={18} />
              {submitting ? 'Submitting...' : 'Submit Assessment'}
            </button>
          ) : (
            <button
              onClick={handleNext}
              className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
            >
              Next
              <FiChevronRight size={18} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AssessmentTaking;
