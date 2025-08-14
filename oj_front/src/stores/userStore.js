// 当前用户信息

import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    isLogin: false,
    user: null,
    token: null,
  }),
  actions: {
    setUser(user) {
      this.user = user;
      this.isLogin = true;
      localStorage.setItem('user', JSON.stringify(user));
    },
    setToken(token) {
      this.token = token;
      localStorage.setItem('token', token);
    },
    logout() {
      this.user = null;
      this.isLogin = false;
      this.token = null;
      localStorage.removeItem('user');
      localStorage.removeItem('token');
    },
    restoreFromStorage() {
      const user = localStorage.getItem('user');
      const token = localStorage.getItem('token');
      if (user) {
        this.user = JSON.parse(user);
        this.isLogin = true;
      }
      if (token) {
        this.token = token;
      }
    }
  }
})
