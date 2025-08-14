import { defineStore } from 'pinia'

export const useResultStore = defineStore('result', {
  state: () => ({
    // 提交结果
    submissionResult: null,
    // 运行状态
    status: 'idle', // idle, running, success, error
    // 执行时间
    executionTime: 0,
    // 内存使用
    memoryUsage: 0,
    // 错误信息
    error: null,
    // 测试用例结果
    testCases: [],
  }),

  actions: {
    // 设置提交结果
    setSubmissionResult(result) {
      this.submissionResult = result
      this.status = 'success'
      this.executionTime = result.executionTime || 0
      this.memoryUsage = result.memoryUsage || 0
      this.testCases = result.testCases || []
    },

    // 设置运行状态
    setStatus(status) {
      this.status = status
    },

    // 设置错误信息
    setError(error) {
      this.error = error
      this.status = 'error'
    },

    // 重置状态
    reset() {
      this.submissionResult = null
      this.status = 'idle'
      this.executionTime = 0
      this.memoryUsage = 0
      this.error = null
      this.testCases = []
    }
  },

  getters: {
    // 获取格式化后的结果
    formattedResult: (state) => {
      if (!state.submissionResult) return null

      return {
        status: state.status,
        executionTime: state.executionTime,
        memoryUsage: state.memoryUsage,
        testCases: state.testCases,
        error: state.error
      }
    }
  }
})
