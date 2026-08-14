import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { toast } from 'react-toastify';

const CandidateLoginPage = () => {
  const navigate = useNavigate();
  const { candidateLogin } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    secureLink: '',
    password: '',
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      await candidateLogin(formData.secureLink, formData.password);
      toast.success('Login successful!');
      navigate('/candidate');
    } catch (error) {
      toast.error(error || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow-xl p-8 w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Language Assessment</h1>
          <p className="text-gray-600 mt-2">Candidate Login</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="form-group">
            <label className="form-label">Secure Link</label>
            <input
              type="text"
              name="secureLink"
              value={formData.secureLink}
              onChange={handleChange}
              className="form-input"
              placeholder="Your secure link"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="form-input"
              placeholder="••••••••"
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary w-full mt-6"
          >
            {loading ? 'Logging in...' : 'Start Assessment'}
          </button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-gray-600">
            Admin User?{' '}
            <Link to="/login" className="text-indigo-600 hover:text-indigo-700 font-medium">
              Login here
            </Link>
          </p>
        </div>

        <div className="mt-4 p-4 bg-amber-50 rounded-lg">
          <p className="text-sm text-amber-800">
            <strong>Note:</strong> You should have received a secure link and password from your administrator.
          </p>
        </div>
      </div>
    </div>
  );
};

export default CandidateLoginPage;
