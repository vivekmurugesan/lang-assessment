import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { toast } from 'react-toastify';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    email: '',
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
      const response = await login(formData.email, formData.password);
      toast.success('Login successful!');

      if (response.role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/candidate');
      }
    } catch (error) {
      toast.error(error || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center overflow-hidden bg-slate-950 px-4">
      <div className="absolute inset-0 bg-gradient-to-br from-indigo-700 via-indigo-900 to-slate-950" />
      <div className="absolute -top-24 -left-24 w-96 h-96 bg-purple-600/30 rounded-full blur-3xl" />
      <div className="absolute -bottom-24 -right-24 w-96 h-96 bg-indigo-500/30 rounded-full blur-3xl" />

      <div className="relative w-full max-w-md">
        <div className="bg-white/95 backdrop-blur rounded-2xl shadow-2xl p-8 w-full border border-white/20">
          <div className="text-center mb-8">
            <div className="inline-flex w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-600 to-purple-600 items-center justify-center text-white font-bold text-xl shadow-lg mb-4">
              LA
            </div>
            <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Language Assessment</h1>
            <p className="text-gray-500 mt-1 text-sm">CEFR Based Evaluation System</p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="form-input"
                placeholder="your@email.com"
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
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <div className="mt-6 text-center">
            <p className="text-gray-600 text-sm">
              Taking a test?{' '}
              <Link to="/candidate-login" className="text-indigo-600 hover:text-indigo-700 font-semibold">
                Use your test link
              </Link>
            </p>
          </div>

          <div className="mt-4 p-4 bg-indigo-50 rounded-lg border border-indigo-100">
            <p className="text-xs text-indigo-900 leading-relaxed">
              <strong>Demo Login:</strong><br />
              Email: admin@langassessment.com<br />
              Password: password
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

export default LoginPage;
