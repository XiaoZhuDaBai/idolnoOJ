<template>
  <div class="submissions-container">
    <!-- 搜索过滤区域 -->
    <div class="search-filters">
      <div class="filter-group">
        <input
          type="text"
          v-model="filters.problemId"
          placeholder="题目名称"
          class="filter-input"
        >
        <select v-model="filters.language" class="filter-select">
          <option value="">所有语言</option>
          <option value="cpp">C++</option>
          <option value="java">Java</option>
          <option value="python">Python</option>
          <option value="javascript">JavaScript</option>
        </select>
        <select v-model="filters.status" class="filter-select">
          <option value="">所有状态</option>
          <option value="通过">通过</option>
          <option value="答案错误">答案错误</option>
          <option value="提交超时">提交超时</option>
          <option value="内存超限">内存超限</option>
          <option value="运行错误">运行错误</option>
          <option value="编译错误">编译错误</option>
          <option value="非零异常">非零异常</option>
        </select>
        <select v-model="filters.userType" class="filter-select">
          <option value="all">所有用户</option>
          <option value="me">我的提交</option>
          <option value="others">其他用户</option>
        </select>
        <button class="search-btn" @click="searchSubmissions">搜索</button>
      </div>
    </div>

    <!-- 提交记录列表 -->
    <div class="submissions-list">
      <div class="list-header">
        <div class="header-item">题目</div>
        <div class="header-item">用户</div>
        <div class="header-item">状态</div>
        <div class="header-item">语言</div>
        <div class="header-item">执行时间</div>
        <div class="header-item">内存</div>
        <div class="header-item">提交时间</div>
      </div>

      <!-- 优化：保持列表始终显示，只在顶部显示加载状态 -->
      <div v-if="loading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-if="submissions.length === 0 && !loading" class="empty-state">
        暂无提交记录
      </div>

      <div v-else class="submission-items" :class="{ 'loading': loading }">
        <div
          v-for="submission in submissions"
          :key="submission.id"
          class="submission-item"
          @click="viewSubmission(submission.id)"
        >
          <div class="item-cell problem-cell">
            <router-link :to="`/problem/${submission.problemId}`">
              {{ submission.problemName }}
            </router-link>
          </div>
          <div class="item-cell user-cell">
            <img :src="submission.userAvatar" :alt="submission.username" class="user-avatar">
            <span>{{ submission.username }}</span>
          </div>
          <div class="item-cell">
            <span :class="['status-badge', submission.status]">
              {{ getStatusText(submission.status) }}
            </span>
          </div>
          <div class="item-cell">{{ submission.language }}</div>
          <div class="item-cell">{{ submission.executionTime }}ms</div>
          <div class="item-cell">{{ formatMemory(submission.memory) }}</div>
          <div class="item-cell">{{ formatDate(submission.submitTime) }}</div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <div class="pagination">
          <button
            @click="prevPage"
            :disabled="currentPage === 1 || loading"
            class="page-btn"
          >
            &laquo;
          </button>

          <!-- 第一页 -->
          <button
            v-if="showFirstPage"
            @click="goToPage(1)"
            :class="{ active: currentPage === 1 }"
            :disabled="loading"
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
            :disabled="loading"
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
            :disabled="loading"
            class="page-btn"
          >
            {{ totalPages }}
          </button>

          <button
            @click="nextPage"
            :disabled="currentPage === totalPages || loading"
            class="page-btn"
          >
            &raquo;
          </button>
        </div>
        <div class="page-info">
          每页 {{ itemsPerPage }} 条，共 {{ totalSubmissions }} 条记录
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useSubmissionStore } from '@/stores/submissionStroe'

const router = useRouter()
const userStore = useUserStore()
const submissionStore = useSubmissionStore()

// 从 store 中获取状态，使用 computed 保持响应性
const submissions = computed(() => submissionStore.submissions)
const loading = computed(() => submissionStore.loading)
const currentPage = computed({
  get: () => submissionStore.currentPage,
  set: (value) => {
    submissionStore.currentPage = value
  }
})
const totalPages = computed(() => submissionStore.totalPages)
const totalSubmissions = computed(() => submissionStore.totalSubmissions)
const itemsPerPage = computed(() => submissionStore.itemsPerPage)
const filters = computed({
  get: () => submissionStore.filters,
  set: (value) => {
    Object.assign(submissionStore.filters, value)
  }
})

// 获取方法（方法不需要保持响应性）
const { getStatusText, formatMemory, formatDate } = submissionStore

// 分页逻辑 - 参考ProblemTable.vue
const maxDisplayedPages = 5 // 显示的页码数量

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

// 搜索提交记录
const searchSubmissions = () => {
  const uuid = userStore.user?.uuid
  submissionStore.searchSubmissions(uuid)
}

// 分页方法
const prevPage = () => {
  if (currentPage.value > 1 && !loading.value) {
    changePage(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value && !loading.value) {
    changePage(currentPage.value + 1)
  }
}

const goToPage = (page) => {
  if (!loading.value) {
    changePage(page)
  }
}

// 切换页码
const changePage = (page) => {
  const uuid = userStore.user?.uuid
  submissionStore.changePage(page, uuid)
}

// 查看提交详情
const viewSubmission = (id) => {
  // 找到对应的提交记录
  const submission = submissions.value.find(s => s.id === id)
  if (submission) {
    // 将完整的提交数据存储到 store 中，供详情页面使用
    submissionStore.setCurrentSubmission(submission)

    // 跳转到详情页面
    router.push({
      path: `/submission/${id}`
    })
  }
}

// 组件挂载时加载数据
onMounted(() => {
  // 重置页码到第一页
  currentPage.value = 1
  searchSubmissions()
})
</script>

<style scoped>
.submissions-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-filters {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  min-width: 120px;
}

.filter-input {
  flex: 1;
  min-width: 200px;
}

.search-btn {
  padding: 8px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #2980b9;
}

.submissions-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  position: relative; /* 为loading-overlay定位 */
}

/* 优化：加载覆盖层，不影响布局 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  border-radius: 8px;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.list-header {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1fr 1fr 1fr 1.5fr;
  padding: 12px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #eee;
  font-weight: 500;
  color: #666;
}

.submission-items {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
  min-height: 200px; /* 保持最小高度，减少布局跳动 */
  transition: opacity 0.2s ease; /* 添加过渡效果 */
}

.submission-items.loading {
  opacity: 0.6; /* 加载时降低透明度 */
}

.submission-item {
  display: grid;
  grid-template-columns: 2fr 1.5fr 1fr 1fr 1fr 1fr 1.5fr;
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.submission-item:hover {
  background: #f8f9fa;
}

.item-cell {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.problem-cell a {
  color: #3498db;
  text-decoration: none;
}

.problem-cell a:hover {
  text-decoration: underline;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.accepted {
  background: #d4edda;
  color: #155724;
}

.status-badge.wrong_answer {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.time_limit {
  background: #fff3cd;
  color: #856404;
}

.status-badge.memory_limit {
  background: #e2e3e5;
  color: #383d41;
}

.status-badge.runtime_error {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.compile_error {
  background: #f8d7da;
  color: #721c24;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #666;
  min-height: 200px; /* 保持最小高度 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 分页样式 - 参考ProblemTable.vue */
.pagination-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
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
</style>
