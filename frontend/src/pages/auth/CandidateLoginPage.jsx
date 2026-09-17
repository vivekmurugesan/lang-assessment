import React, { useState, useEffect } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { toast } from 'react-toastify';

const CandidateLoginPage = () => {
  const navigate = useNavigate();
  const { candidateLogin } = useAuthStore();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    secureLink: searchParams.get('secureLink') || '',
    password: '',
  });

  useEffect(() => {
    const secureLink = searchParams.get('secureLink');
    if (secureLink) {
      setFormData(prev => ({
        ...prev,
        secureLink
      }));
    }
  }, [searchParams]);

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
    <div className="min-h-screen relative flex items-center justify-center overflow-hidden bg-slate-950 px-4">
      <div className="absolute inset-0 bg-gradient-to-br from-indigo-700 via-indigo-900 to-slate-950" />
      <div className="absolute -top-24 -right-24 w-96 h-96 bg-purple-600/30 rounded-full blur-3xl" />
      <div className="absolute -bottom-24 -left-24 w-96 h-96 bg-indigo-500/30 rounded-full blur-3xl" />

      <div className="relative w-full max-w-md">
        <div className="bg-white/95 backdrop-blur rounded-2xl shadow-2xl p-8 w-full border border-white/20">
          <div className="text-center mb-8">
            <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 items-center justify-center text-white font-bold text-xl shadow-lg mb-4">
              LA
            </div>
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Language Assessment</h1>
            <p className="text-gray-500 mt-1 text-sm">Candidate Login</p>
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
            <p className="text-gray-600 text-sm">
              Admin User?{' '}
              <Link to="/login" className="text-indigo-600 hover:text-indigo-700 font-semibold">
                Login here
              </Link>
            </p>
          </div>

          <div className="mt-4 p-4 bg-amber-50 rounded-lg border border-amber-100">
            <p className="text-xs text-amber-900 leading-relaxed">
              <strong>Note:</strong> You should have received a secure link and password from your administrator.
            </p>
          </div>
        </div>

        <p className="text-center text-white/60 text-xs mt-6">
          &copy; {new Date().getFullYear()} Language Assessment System
        </p>
      </div>
    </div>
  );
};

export default CandidateLoginPage;
