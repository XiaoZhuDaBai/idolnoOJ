import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { SearchCommits, SearchCommitsCount } from '@/api/UserApi'

export const useSubmissionStore = defineStore('submission', () => {
  // 状态
  const submissions = ref([])
  const loading = ref(false)
  const currentPage = ref(1)
  const totalPages = ref(1)
  const totalSubmissions = ref(0) // 添加总记录数
  const itemsPerPage = 15 // 每页显示数量
  const currentSubmission = ref(null) // 当前查看的提交详情
  const filters = reactive({
    problemId: '',
    language: '',
    status: '',
    userType: 'all'
  })

  // 获取状态文本
  const getStatusText = (status) => {
    const statusMap = {
      accepted: '通过',
      wrong_answer: '答案错误',
      time_limit: '超时',
      memory_limit: '内存超限',
      runtime_error: '运行错误',
      compile_error: '编译错误',
      other: '非零错误'
    }
    return statusMap[status] || status
  }

  // 格式化内存显示
  const formatMemory = (memory) => {
    if (memory < 1024 * 1024) {
      return `${(memory / 1024).toFixed(2)}KB`
    }
    return `${(memory / 1024 / 1024).toFixed(2)}MB`
  }

  // 格式化日期显示
  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  // 映射状态文本到状态码
  const mapStatus = (commitCase) => {
    const statusMap = {
      '通过': 'accepted',
      '答案错误': 'wrong_answer',
      '超时': 'time_limit',
      '内存超限': 'memory_limit',
      '运行错误': 'runtime_error',
      '编译错误': 'compile_error',
      '提交超时': 'time_limit'
    }

    // 尝试直接匹配
    if (statusMap[commitCase]) {
      return statusMap[commitCase]
    }

    // 尝试部分匹配
    for (const [key, value] of Object.entries(statusMap)) {
      if (commitCase.includes(key)) {
        return value
      }
    }

    // 特殊情况处理
    if (commitCase.includes('非零异常')) {
      return 'runtime_error'
    }

    return 'other'
  }

  // 获取总记录数
  const getTotalCount = async (searchParams) => {
    try {
      const response = await SearchCommitsCount(searchParams)

      // 处理API返回的数据结构
      if (typeof response === 'number') {
        return response
      } else if (response && typeof response === 'object') {
        // 如果响应包含code字段，说明是标准API响应格式
        if (response.code !== undefined) {
          if (response.code === 200 && response.data !== undefined) {
            // data字段可能是数字或者包含count的对象
            if (typeof response.data === 'number') {
              return response.data
            } else if (response.data && typeof response.data === 'object') {
              if (response.data.count !== undefined) {
                return response.data.count
              } else if (response.data.total !== undefined) {
                return response.data.total
              }
            }
          }
        } else {
          // 直接检查常见字段
          if (response.count !== undefined) {
            return response.count
          } else if (response.total !== undefined) {
            return response.total
          } else if (response.data !== undefined && typeof response.data === 'number') {
            return response.data
          }
        }
      }
      return 0
    } catch (error) {
      console.error('获取总记录数失败:', error)
      return 0
    }
  }

  // 搜索提交记录
  const searchSubmissions = async (uuid) => {
    loading.value = true
    try {
      // 构建搜索参数
      const searchParams = {
        problemName: filters.problemId,
        language: filters.language,
        status: filters.status,
        userType: filters.userType,
        page: currentPage.value,
        pageSize: itemsPerPage // 添加页面大小参数
      }

      // 如果是"我的提交"或"其他用户"，需要添加用户UUID
      if (filters.userType === 'me' && uuid) {
        searchParams.uuid = uuid
      } else if (filters.userType === 'others' && uuid) {
        searchParams.uuid = uuid
        searchParams.excludeMe = true // 添加一个标记，表示排除自己的提交
      }

      // 并行获取数据和总数
      const [response, totalCount] = await Promise.all([
        SearchCommits(searchParams),
        getTotalCount(searchParams)
      ])

      // 更新总记录数
      totalSubmissions.value = totalCount

      // 计算总页数
      totalPages.value = Math.ceil(totalCount / itemsPerPage) || 1

      // 处理响应数据
      let dataToProcess = response

      // 如果响应包含code字段，说明是标准API响应格式
      if (response && typeof response === 'object' && response.code !== undefined) {
        if (response.code === 200 && response.data !== undefined) {
          dataToProcess = response.data
        }
      }

      if (dataToProcess && Array.isArray(dataToProcess)) {
        submissions.value = dataToProcess.map(item => {
          // 处理头像地址
          let avatarUrl = 'https://api.dicebear.com/7.x/avataaars/svg?seed=' + (item.nickname || 'default')

          // 如果有头像地址但不是以http开头，可能需要添加域名前缀
          if (item.avatar) {
            if (item.avatar.startsWith('http')) {
              // 检查是否是外部不可访问的地址，如果是则使用默认头像
              if (item.avatar.includes('pixnio.com') || item.avatar.includes('2025')) {
                // 这些地址可能有问题，使用默认头像
              } else {
                avatarUrl = item.avatar
              }
            } else {
              // 如果是相对路径，可能需要添加域名
              // avatarUrl = 'https://your-domain.com' + item.avatar
            }
          }

          return {
            id: item.id || Math.random().toString(36).substring(2, 10), // 生成随机ID如果没有
            problemId: item.problemId || '',
            problemName: item.problemName || '',
            username: item.nickname || '未知用户',
            userAvatar: avatarUrl,
            status: mapStatus(item.commitCase || ''),
            language: item.language || '',
            executionTime: item.time || 0,
            memory: item.memory || 0,
            submitTime: item.createTime || Date.now(),
            code: item.code || ''
          }
        })
      } else {
        submissions.value = []
      }
    } catch (error) {
      console.error('搜索失败:', error)
      submissions.value = []
      totalSubmissions.value = 0
      totalPages.value = 1
    } finally {
      loading.value = false
    }
  }

  // 切换页码
  const changePage = (page, uuid) => {
    currentPage.value = page
    searchSubmissions(uuid)
  }

  // 设置当前查看的提交详情
  const setCurrentSubmission = (submission) => {
    currentSubmission.value = submission
  }

  // 重置过滤器
  const resetFilters = () => {
    filters.problemId = ''
    filters.language = ''
    filters.status = ''
    filters.userType = 'all'
    currentPage.value = 1 // 重置页码
  }

  return {
    submissions,
    loading,
    currentPage,
    totalPages,
    totalSubmissions, // 导出总记录数
    itemsPerPage, // 导出每页数量
    currentSubmission, // 导出当前提交详情
    filters,
    getStatusText,
    formatMemory,
    formatDate,
    searchSubmissions,
    changePage,
    setCurrentSubmission,
    resetFilters
  }
})
