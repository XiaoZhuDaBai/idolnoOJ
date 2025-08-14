<template>
  <div class="problems-table-container">
    <!-- 添加加载状态显示 -->
    <div v-if="loading" class="loading-state">
      加载中...
    </div>

    <!-- 添加空状态显示 -->
    <div v-else-if="problems.length === 0" class="empty-state">
      暂无题目
    </div>

    <table class="problems-table">
      <thead>
      <tr>
        <th>题目</th>
        <th>难度</th>
        <th>总数</th>
        <th>提交通过率</th>
      </tr>
      </thead>
      <tbody>
      <tr
        v-for="problem in paginatedProblems"
        :key="problem.id"
        @click="navigateToProblem(problem)"
        class="problem-row"
      >
        <td class="problem-title-cell">
          <span class="problem-title">{{ problem.title }}</span>
        </td>
        <td :class="['difficulty', `difficulty-${problem.difficulty}`]">
          {{ difficultyText[problem.difficulty] }}
        </td>
        <td>{{ problem.total }}</td>
        <td>
          <div class="progress-container">
            <div class="progress-bar">
              <div
                class="progress-value"
                :style="{ width: `${problem.acRate}%` }"
              ></div>
            </div>
            <span>{{ problem.acRate }}%</span>
          </div>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="pagination-container">
      <div class="pagination">
        <button
          @click="prevPage"
          :disabled="currentPage === 1"
          class="page-btn"
        >
          &laquo;
        </button>

        <!-- 第一页 -->
        <button
          v-if="showFirstPage"
          @click="goToPage(1)"
          :class="{ active: currentPage === 1 }"
          class="page-btn"
        >
          1
        </button>

        <!-- 第一页后的省略号 -->
        <span v-if="showFirstEllipsis" class="ellipsis">...</span>

        <!-- 当前页附近的页码 -->
        <button
          v-for="page in displayedPages"
          :key="page"
          @click="goToPage(page)"
          :class="{ active: currentPage === page }"
          class="page-btn"
        >
          {{ page }}
        </button>

        <!-- 最后一页前的省略号 -->
        <span v-if="showLastEllipsis" class="ellipsis">...</span>

        <!-- 最后一页 -->
        <button
          v-if="showLastPage"
          @click="goToPage(totalPages)"
          :class="{ active: currentPage === totalPages }"
          class="page-btn"
        >
          {{ totalPages }}
        </button>

        <button
          @click="nextPage"
          :disabled="currentPage === totalPages"
          class="page-btn"
        >
          &raquo;
        </button>
      </div>
      <div class="page-info">
        每页 {{ itemsPerPage }} 条，共 {{ totalProblems }} 题
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, watch} from 'vue'
import { useRouter } from 'vue-router'
import { useProblemStore } from "@/stores/problemStore.js";

const router = useRouter()
const problemStore = useProblemStore()

const difficultyText = {
  easy: '简单',
  medium: '中等',
  hard: '困难'
}

// 修改后的导航方法
const navigateToProblem = async (problem) => {
  try {
    // 先获取题目详情
    await problemStore.fetchProblemDetail(problem.id)

    // 使用resolve方法获取完整路径
    const resolved = router.resolve({
      path: `/problemPage/${problem.id}`
    })

    // 构造完整URL
    const fullUrl = new URL(
      resolved.href,
      window.location.origin
    ).href
    // 安全打开新窗口
    window.open(
      fullUrl,
      '_blank',
      'noopener,noreferrer'
    )
  } catch (error) {
    console.error('获取题目详情失败:', error)
  }
}

// 使用 store 中的状态
const loading = computed(() => problemStore.loading)
const problems = computed(() => problemStore.problems)
const totalProblems = computed(() => problemStore.totalProblems)
const currentPage = computed({
  get: () => problemStore.currentPage,
  set: (value) => {
    problemStore.currentPage = value
  }
})

// 分页逻辑
const itemsPerPage = 15
const maxDisplayedPages = 5 // 显示的页码数量

// 计算总页数
const totalPages = computed(() => Math.ceil(totalProblems.value / itemsPerPage))

// 计算显示的页码范围
const displayedPages = computed(() => {
  const pages = []
  const halfDisplayed = Math.floor(maxDisplayedPages / 2)

  let start = Math.max(currentPage.value - halfDisplayed, 1)
  let end = Math.min(start + maxDisplayedPages - 1, totalPages.value)

  // 调整起始页，确保显示足够的页码
  if (end - start + 1 < maxDisplayedPages) {
    start = Math.max(end - maxDisplayedPages + 1, 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// 是否显示第一页
const showFirstPage = computed(() => {
  return displayedPages.value[0] > 1
})

// 是否显示第一页后的省略号
const showFirstEllipsis = computed(() => {
  return displayedPages.value[0] > 2
})

// 是否显示最后一页
const showLastPage = computed(() => {
  return displayedPages.value[displayedPages.value.length - 1] < totalPages.value
})

// 是否显示最后一页前的省略号
const showLastEllipsis = computed(() => {
  return displayedPages.value[displayedPages.value.length - 1] < totalPages.value - 1
})

// 直接使用 store 中的问题列表
const paginatedProblems = computed(() => problems.value)

// 监听页码变化
watch(currentPage, (newPage) => {
  console.log('Page changed to:', newPage)
  if (problemStore.searchParams) {
    // 如果有搜索参数，使用搜索
    problemStore.searchProblems(problemStore.searchParams)
  } else {
    // 否则使用普通获取
    problemStore.fetchProblems(newPage)
  }
}, { immediate: true })

function prevPage() {
  if (currentPage.value > 1) currentPage.value--
}

function nextPage() {
  if (currentPage.value < totalPages.value) currentPage.value++
}

function goToPage(page) {
  currentPage.value = page
}
</script>

<style scoped>
.problems-table-container {
  width: 100%;
  overflow-x: auto;
}

.problems-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;
}

.problems-table th {
  background-color: #f8f9fa;
  padding: 12px 15px;
  text-align: left;
  font-weight: bold;
  border-bottom: 2px solid #ddd;
}

.problems-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
}

/* 整行点击效果 */
.problem-row {
  cursor: pointer;
  transition: background-color 0.2s;
}

.problem-row:hover {
  background-color: #f5f5f5;
}

.problem-title-cell {
  color: #3498db; /* 让标题看起来像链接 */
  font-weight: 500;
}

/* 难度颜色 */
.difficulty-easy {
  color: #28a745;
  font-weight: bold;
}

.difficulty-medium {
  color: #ffc107;
  font-weight: bold;
}

.difficulty-hard {
  color: #dc3545;
  font-weight: bold;
}

/* 进度条样式 */
.progress-container {
  display: flex;
  align-items: center;
}

.progress-bar {
  height: 8px;
  background-color: #e9ecef;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
  min-width: 100px;
  margin-right: 10px;
}

.progress-value {
  height: 100%;
  background-color: #3498db;
  transition: width 0.3s ease;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
}

.pagination {
  display: flex;
  gap: 5px;
  margin-bottom: 10px;
  align-items: center;
}

.ellipsis {
  padding: 8px 12px;
  color: #666;
}

.page-btn {
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: white;
  color: #3498db;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 40px;
  text-align: center;
}

.page-btn:hover:not(:disabled) {
  background-color: #f1f1f1;
}

.page-btn.active {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  color: #aaa;
  cursor: not-allowed;
  opacity: 0.7;
}

.page-info {
  color: #777;
  font-size: 14px;
}

/* 添加新的样式 */
.loading-state,
.empty-state {
  text-align: center;
  padding: 20px;
  color: #666;
}
</style>
