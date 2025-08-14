import { defineStore } from 'pinia'

export const useCodeStore = defineStore('code', {
  state: () => ({
    code: '',
    language: 'javascript',
    languageDisplay: 'JavaScript'
  }),

  actions: {
    setCode(newCode) {
      this.code = newCode
    },
    setLanguage(lang, display) {
      this.language = lang
      this.languageDisplay = display
    }
  }
})
