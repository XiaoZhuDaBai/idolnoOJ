import { defineStore } from 'pinia'
import { ref } from 'vue'
import { GetContestTables } from '@/api/ContestApi.js'

export const useContestStore = defineStore('contest', () => {
  // 状态
  const contests = ref([])
  const loading = ref(false)
  const error = ref(null)
  const lastUpdateTime = ref(null)

  // 工具方法
  function getContestStatus(startTime, endTime) {
    const now = new Date()
    if (now < startTime) return 'upcoming'
    if (now >= startTime && now <= endTime) return 'running'
    return 'ended'
  }

  // 获取比赛数据
  async function fetchContests() {
    loading.value = true
    error.value = null
    try {
      const data = await GetContestTables()
      // 统一格式化后端返回的比赛数据
      contests.value = data.map((contest, idx) => {
        const startTime = new Date(contest.start_time * 1000)
        const endTime = new Date(contest.end_time * 1000)
        return {
          id: `${contest.platform}_${idx}`,
          name: contest.name,
          organizer: contest.platform,
          startTime: startTime.toISOString(),
          endTime: endTime.toISOString(),
          duration: contest.durationSeconds / 3600, // 小时
          status: getContestStatus(startTime, endTime),
          link: contest.contest_url,
          oj: contest.platform,
          phase: ''
        }
      })
      lastUpdateTime.value = new Date()
    } catch (err) {
      error.value = '比赛数据获取失败'
      contests.value = []
      console.error('fetchContests error:', err)
    } finally {
      loading.value = false
    }
  }

  // 更新比赛状态
  function updateContestStatus() {
    const now = new Date()
    contests.value = contests.value.filter(contest => {
      const startTime = new Date(contest.startTime)
      const endTime = new Date(contest.endTime)
      contest.status = getContestStatus(startTime, endTime)
      return endTime > now
    })
  }

  // 时间格式化
  function formatDate(dateString) {
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 倒计时格式化
  function formatCountdown(dateString) {
    const now = new Date()
    const start = new Date(dateString)
    const diff = start - now
    if (diff <= 0) return '即将开始'
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    if (days > 0) return `还有 ${days} 天`
    if (hours > 0) return `还有 ${hours} 小时`
    return `还有 ${minutes} 分钟`
  }

  // 获取最后更新时间
  function getLastUpdateTime() {
    if (!lastUpdateTime.value) return null
    return lastUpdateTime.value.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  }

  return {
    contests,
    loading,
    error,
    fetchContests,
    updateContestStatus,
    formatDate,
    formatCountdown,
    getLastUpdateTime
  }
})
