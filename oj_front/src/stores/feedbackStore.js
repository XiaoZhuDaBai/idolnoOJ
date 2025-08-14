import { defineStore } from 'pinia'
import { SendFeedback } from '@/api/UserApi.js'

export const useFeedbackStore = defineStore('feedback', {
  state: () => ({
    feedbacks: [],
    loading: false,
    error: null
  }),

  actions: {
    async submitFeedback(feedback) {
      this.loading = true
      this.error = null

      try {
        const response = await SendFeedback({
          type: feedback.type,
          title: feedback.title,
          content: feedback.content,
          contact: feedback.contact
        })

        this.feedbacks.push(response.data)
        return response.data
      } catch (error) {
        this.error = error.response?.data?.message || '提交失败，请稍后重试'
        throw error
      } finally {
        this.loading = false
      }
    }
  }
})
