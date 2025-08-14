import { defineStore } from 'pinia'
import { ref } from 'vue'
import { GetOneProblemDataById, Search, GetProblemCounts } from '@/api/ProblemApi'

export const useProblemStore = defineStore('problem', () => {
  const problems = ref([])
  const currentProblem = ref(null)
  const loading = ref(false)
  const currentPage = ref(1)
  const totalProblems = ref(0)
  const searchParams = ref(null)

  // 处理问题列表数据的通用函数
  const processProblemList = (response) => {
    if (Array.isArray(response)) {
      return response.map(problem => ({
        id: problem.questionId,
        title: problem.questionName,
        difficulty: getDifficultyLevel(problem.difficulty),
        total: problem.questionCommits || 0,
        acRate: problem.questionCommits !== 0 ? ((problem.ac / problem.questionCommits * 100).toFixed(2)) : '0',
        tags: problem.questionTag ? problem.questionTag.split(',') : []
      }))
    }
    console.error('响应格式错误:', response)
    return []
  }

  // 获取题目列表
  const fetchProblems = async (page) => {
    loading.value = true
    try {
      const params = {
        platform: '',
        difficulty: '',
        resource: '',
        tags: [],
        page: page
      }
      const [response, counts] = await Promise.all([
        Search(params),
        GetProblemCounts(params)
      ])
      problems.value = processProblemList(response)
      totalProblems.value = counts
    } catch (error) {
      console.error('获取问题列表失败:', error)
      problems.value = []
      totalProblems.value = 0
    } finally {
      loading.value = false
    }
  }

  // 获取题目详情
  const fetchProblemDetail = async (problemId) => {
    loading.value = true
    try {
      const response = await GetOneProblemDataById(problemId)
      currentProblem.value = {
        id: response.questionId,
        title: response.questionName,
        timeLimit: `${response.timeLimit}ms`,
        memoryLimit: `${response.memoryLimit / 1024 / 1024}MB`,
        difficulty: getDifficultyLevel(response.difficulty),
        description: response.questionDescription,
        tags: response.questionTag ? response.questionTag.split(',') : [],
        examples: response.questionExamples.split('\n').reduce((acc, curr, idx, arr) => {
          if (idx % 2 === 0) {
            acc.push({
              input: curr,
              output: arr[idx + 1],
              explanation: ''
            })
          }
          return acc
        }, []),
        constraints: [],
        solution: {
          approach: '',
          steps: [],
          complexity: [],
          code: ''
        }
      }
    } catch (error) {
      console.error('获取题目详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 搜索题目
  const searchProblems = async (params) => {
    console.log('开始搜索，参数:', params)
    loading.value = true
    try {
      searchParams.value = params
      const searchParamsWithPage = {
        ...params,
        page: currentPage.value
      }
      const [response, counts] = await Promise.all([
        Search(searchParamsWithPage),
        GetProblemCounts(searchParamsWithPage)
      ])
      console.log('搜索响应:', response)
      problems.value = processProblemList(response)
      totalProblems.value = counts
    } catch (error) {
      console.error('搜索题目失败:', error)
      problems.value = []
      totalProblems.value = 0
    } finally {
      loading.value = false
    }
  }

  // 将数字难度转换为文本难度
  const getDifficultyLevel = (difficulty) => {
    switch (difficulty) {
      case 0:
        return 'easy'
      case 1:
        return 'medium'
      case 2:
        return 'hard'
      default:
        return 'easy'
    }
  }

  return {
    problems,
    currentProblem,
    loading,
    currentPage,
    totalProblems,
    searchParams,
    fetchProblems,
    fetchProblemDetail,
    searchProblems
  }
})
