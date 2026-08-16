import create from 'zustand';
import api from '../api/axiosConfig';

export const useAuthStore = create((set) => ({
  user: null,
  token: localStorage.getItem('token'),
  isAuthenticated: !!localStorage.getItem('token'),
  loading: true,

  login: async (email, password) => {
    try {
      const response = await api.post('/auth/login', { email, password });
      const { data: { data: loginResponse } } = response;

      localStorage.setItem('token', loginResponse.token);
      localStorage.setItem('user', JSON.stringify(loginResponse));

      set({
        user: loginResponse,
        token: loginResponse.token,
        isAuthenticated: true,
      });

      return loginResponse;
    } catch (error) {
      throw error.response?.data?.message || 'Login failed';
    }
  },

  candidateLogin: async (secureLink, password) => {
    try {
      const response = await api.post('/auth/candidate-login', { secureLink, password });
      const { data: { data: loginResponse } } = response;

      localStorage.setItem('token', loginResponse.token);
      localStorage.setItem('user', JSON.stringify(loginResponse));

      set({
        user: loginResponse,
        token: loginResponse.token,
        isAuthenticated: true,
      });

      return loginResponse;
    } catch (error) {
      throw error.response?.data?.message || 'Login failed';
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({
      user: null,
      token: null,
      isAuthenticated: false,
    });
  },

  checkAuth: () => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        set({
          user,
          token,
          isAuthenticated: true,
          loading: false,
        });
      } catch (error) {
        set({ loading: false });
      }
    } else {
      set({ loading: false });
    }
  },
}));
