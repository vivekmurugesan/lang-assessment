import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { FiChevronLeft, FiChevronRight, FiVolume2, FiMic, FiPause, FiSave, FiSend } from 'react-icons/fi';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

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
  const [questionOptions, setQuestionOptions] = useState({});
  const mediaRecorderRef = useRef(null);
  const audioChunksRef = useRef([]);
  const audioPlayerRef = useRef(null);

  useEffect(() => {
    loadAssessment();
  }, [secureLink]);

  const currentQuestion = questions[currentQuestionIndex];

  const getAudioUrl = (questionId) => {
    const token = localStorage.getItem('token');
    return `${API_BASE_URL}/candidate/questions/${questionId}/audio${token ? `?token=${token}` : ''}`;
  };

  useEffect(() => {
    if (currentQuestion?.questionOptionsUri && !questionOptions[currentQuestion.id]) {
      fetchQuestionOptions(currentQuestion);
    }
  }, [currentQuestion?.id, questionOptions]);

  const loadAssessment = async () => {
    try {
      const [assessmentRes, questionsRes, startRes] = await Promise.all([
        api.get(`/candidate/assessments/${secureLink}`),
        api.get(`/candidate/assessments/${secureLink}/questions`),
        api.post(`/candidate/assessments/${secureLink}/start`)
      ]);

      console.log('Assessment Response:', assessmentRes);
      console.log('Questions Response:', questionsRes);
      console.log('Start Response:', startRes);

      if (!assessmentRes.data.success || !questionsRes.data.success || !startRes.data.success) {
        console.error('API returned error:',
          assessmentRes.data.message || questionsRes.data.message || startRes.data.message
        );
        toast.error('Failed to load assessment data');
        setLoading(false);
        return;
      }

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

  const fetchQuestionOptions = async (question) => {
    if (!question.questionOptionsUri) {
      console.warn(`No options URI for question ${question.id}`);
      return;
    }

    try {
      const response = await api.get(`/candidate/questions/${question.id}/options`);
      if (!response.data.success) {
        console.error('API returned error:', response.data.message);
        throw new Error(response.data.message || 'Failed to fetch options');
      }

      const data = response.data.data;
      if (!data) {
        console.warn(`No options data returned for question ${question.id}`);
        return;
      }

      // Convert array to object with A, B, C, D keys if it's an array
      let optionsObj = data;
      if (Array.isArray(data)) {
        optionsObj = {};
        const letters = ['A', 'B', 'C', 'D'];
        data.forEach((option, index) => {
          if (index < letters.length) {
            optionsObj[letters[index]] = option;
          }
        });
      }

      setQuestionOptions(prev => ({
        ...prev,
        [question.id]: optionsObj
      }));
    } catch (error) {
      console.error('Failed to fetch question options for question', question.id, ':', error);
      toast.error('Failed to load options for this question');
    }
  };

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
      toast.success('Assessment submitted successfully! Your results will be shared via email once the evaluation is complete.');
      navigate(`/candidate`);
    } catch (error) {
      console.error('Failed to submit assessment:', error);
      toast.error('Failed to submit assessment');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="spinner mx-auto mb-4" />
          <p className="text-gray-500">Loading assessment...</p>
        </div>
      </div>
    );
  }

  if (!assessment) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
        <div className="card text-center py-12 max-w-md">
          <p className="text-gray-600">Assessment data not available</p>
        </div>
      </div>
    );
  }

  if (questions.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-6">
        <div className="card text-center py-12 max-w-md">
          <h2 className="text-xl font-bold text-gray-800 mb-3">No Questions Available</h2>
          <p className="text-gray-600">
            This assessment has not been configured with any modules yet.
          </p>
          <p className="text-sm text-gray-500 mt-2">
            Please contact your administrator to set up the assessment modules.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Focus-mode top bar */}
      <div className="bg-white border-b border-gray-200 sticky top-0 z-30">
        <div className="max-w-4xl mx-auto px-6 py-3 flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center text-white font-bold text-xs shrink-0">
            LA
          </div>
          <span className="font-semibold text-gray-900 text-sm">Language Assessment</span>
        </div>
      </div>

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
            <span className="font-semibold text-indigo-600">{Math.round(((currentQuestionIndex + 1) / questions.length) * 100)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
            <div
              className="bg-gradient-to-r from-indigo-500 to-purple-600 h-2 rounded-full transition-all duration-300"
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
                <span className="badge bg-indigo-100 text-indigo-700">
                  {currentQuestion?.moduleType?.replace(/_/g, ' ')}
                </span>
              </div>

              {/* Question Text */}
              <h2 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900 leading-snug">
                {currentQuestion?.questionText}
              </h2>

              {/* Question Content */}
              <div className="mb-8">
                {/* Audio Playback for Listening */}
                {currentQuestion?.moduleType === 'LISTENING' && (
                  <div className="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
                    {currentQuestion?.audioUrl ? (
                      <>
                        <p className="text-sm font-semibold text-blue-900 mb-3">📢 Listen to the audio and answer the question below</p>
                        <div className="mb-3">
                          <audio
                            ref={audioPlayerRef}
                            src={getAudioUrl(currentQuestion.id)}
                            className="w-full"
                            controls
                            crossOrigin="anonymous"
                            onError={(e) => {
                              console.error('Audio playback error:', e);
                              toast.error('Failed to load audio. Please try refreshing the page.');
                            }}
                          />
                        </div>
                        <p className="text-xs text-gray-500 mt-2">💡 Tip: You can play the audio multiple times. Click the play button to listen.</p>
                      </>
                    ) : (
                      <div className="text-center py-6">
                        <p className="text-sm text-gray-600">Audio content is being prepared</p>
                        <p className="text-xs text-gray-500 mt-1">Please check back in a moment or contact support if this persists</p>
                      </div>
                    )}
                  </div>
                )}

                {/* Multiple Choice Options */}
                {['READING', 'LISTENING'].includes(currentQuestion?.moduleType) && (
                  <div className="space-y-3">
                    {questionOptions[currentQuestion?.id] ? (
                      <div className="space-y-3">
                        {Object.keys(questionOptions[currentQuestion?.id]).length > 0 ? (
                          Object.entries(questionOptions[currentQuestion?.id]).map(([key, value]) => {
                            const selected = responses[currentQuestion?.id]?.option === key;
                            return (
                              <label
                                key={key}
                                className={`flex items-center p-4 border-2 rounded-xl cursor-pointer transition-colors ${
                                  selected
                                    ? 'border-indigo-600 bg-indigo-50'
                                    : 'border-gray-200 hover:border-indigo-300 hover:bg-gray-50'
                                }`}
                              >
                                <input
                                  type="radio"
                                  name={`question-${currentQuestion?.id}`}
                                  value={key}
                                  checked={selected}
                                  onChange={(e) => handleResponseChange(e.target.value, 'option')}
                                  className="mr-3 w-4 h-4 accent-indigo-600"
                                />
                                <span className={`font-semibold mr-3 ${selected ? 'text-indigo-700' : 'text-gray-500'}`}>{key}.</span>
                                <span className="text-gray-800">{value}</span>
                              </label>
                            );
                          })
                        ) : (
                          <div className="text-center py-6 bg-yellow-50 rounded-lg border border-yellow-200">
                            <p className="text-sm text-yellow-800">No options available for this question</p>
                          </div>
                        )}
                      </div>
                    ) : (
                      <div className="text-center py-6 bg-gray-100 rounded-lg">
                        <p className="text-gray-600 animate-pulse">Loading options...</p>
                      </div>
                    )}
                  </div>
                )}

                {/* Recording for Speaking */}
                {['SPOKEN_INTERACTION', 'SPOKEN_PRODUCTION'].includes(currentQuestion?.moduleType) && (
                  <div className="space-y-4">
                    <div className="p-6 bg-red-50 rounded-xl border border-red-100">
                      <div className="flex items-center justify-center">
                        <button
                          onClick={isRecording ? stopRecording : startRecording}
                          className={`flex items-center gap-2 px-6 py-3 rounded-full font-semibold text-white shadow-soft transition-all ${
                            isRecording
                              ? 'bg-red-600 hover:bg-red-700 animate-pulse'
                              : 'bg-red-500 hover:bg-red-600'
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
                      className="form-input min-h-64 p-4"
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
                className="btn btn-secondary btn-sm"
              >
                <FiSave size={16} />
                Save Response
              </button>
            </div>
          </div>

          {/* Sidebar - Question List */}
          <div className="lg:col-span-1">
            <div className="card sticky top-20">
              <h3 className="font-bold text-base mb-4 text-gray-900">Questions</h3>
              <div className="space-y-2 max-h-96 overflow-y-auto pr-1">
                {questions.map((q, idx) => (
                  <button
                    key={q.id}
                    onClick={() => setCurrentQuestionIndex(idx)}
                    className={`w-full p-3 text-left rounded-lg transition-colors ${
                      idx === currentQuestionIndex
                        ? 'bg-indigo-600 text-white shadow-soft'
                        : responses[q.id]
                        ? 'bg-emerald-50 text-emerald-900 hover:bg-emerald-100'
                        : 'bg-gray-50 text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    <div className="font-medium text-sm">Q{idx + 1}</div>
                    <div className="text-xs opacity-75">{q.moduleType?.replace(/_/g, ' ')}</div>
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
            className="btn btn-secondary"
          >
            <FiChevronLeft size={18} />
            Previous
          </button>

          {currentQuestionIndex === questions.length - 1 ? (
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className="btn btn-success"
            >
              <FiSend size={18} />
              {submitting ? 'Submitting...' : 'Submit Assessment'}
            </button>
          ) : (
            <button
              onClick={handleNext}
              className="btn btn-primary"
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
