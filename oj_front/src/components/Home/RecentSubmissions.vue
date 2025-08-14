<template>
  <div class="section">
    <h2>最近的提交</h2>
    <template v-if="!userStore.isLogin">
      <div class="login-prompt">
        <p class="prompt-text">请登录后查看提交记录</p>
      </div>
    </template>
    <template v-else>
      <!-- 优化：保持列表始终显示，只在顶部显示加载状态 -->
      <div v-if="isLoading" class="loading-overlay">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <ul v-if="submissions.length > 0" class="submission-list" :class="{ 'loading': isLoading }">
        <li v-for="submission in displayedSubmissions" :key="submission.id" class="submission-item">
          <div class="submission-info">
            <div class="submission-title">{{ submission.title }}</div>
            <div class="submission-meta">{{ submission.user }} · {{ submission.time }}</div>
          </div>
          <span :class="['submission-status', `status-${submission.status}`]">
            {{ submission.cnName }}
          </span>
        </li>
      </ul>
      <div v-else-if="!isLoading" class="no-data">
        暂无提交记录
      </div>

      <div v-if="submissions.length > 0 && totalPages > 1" class="pagination">
        <button
          class="page-btn"
          :disabled="currentPage === 1 || isLoading"
          @click="prevPage">
          上一页
        </button>
        <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
        <button
          class="page-btn"
          :disabled="currentPage >= totalPages || isLoading"
          @click="nextPage">
          下一页
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { RecentCommit, RecentCommitCount } from '@/api/UserApi'

const userStore = useUserStore()

const submissions = ref([])
const isLoading = ref(false)
const currentPage = ref(1)
const totalPages = ref(1)
const totalCount = ref(0) // 新增：记录总数
const pageSize = 5 // 每页显示5条

// 直接显示后端返回的数据，不需要前端再次分页
const displayedSubmissions = computed(() => {
  return submissions.value
})

// 新增：获取记录总数
async function fetchTotalCount() {
  if (!userStore.isLogin || !userStore.user?.uuid) return

  try {
    const response = await RecentCommitCount(userStore.user.uuid)
    console.log('记录总数API响应:', response) // 调试日志

    const data = response.data || response
    totalCount.value = parseInt(data) || 0 // 确保转换为数字

    // 根据总数计算总页数
    totalPages.value = Math.max(1, Math.ceil(totalCount.value / pageSize))
    console.log('记录总数:', totalCount.value, '总页数:', totalPages.value, '每页大小:', pageSize) // 调试日志
  } catch (error) {
    console.error('获取记录总数失败:', error)
    totalCount.value = 0
    totalPages.value = 1
  }
}

async function fetchRecentSubmissions() {
  if (!userStore.isLogin || !userStore.user?.uuid) return

  try {
    isLoading.value = true
    console.log('请求页码:', currentPage.value) // 调试日志
    const response = await RecentCommit(userStore.user.uuid, currentPage.value)

    console.log('最近提交API响应:', response) // 调试日志

    // 处理后端返回的数据
    const data = response.data || response
    if (Array.isArray(data)) {
      submissions.value = data.map((item, index) => ({
        id: item.id || `${currentPage.value}-${index}`, // 使用页码和索引作为ID
        title: item.title || '未知题目', // title是题目名称
        user: userStore.user.username || userStore.user.name || '用户',
        time: formatTime(item.createTime),
        status: mapStatusFromCnName(item.cnName), // 用于CSS样式类
        cnName: item.cnName || '未知状态' // 直接保存中文状态用于显示
      }))
    } else if (data.records && Array.isArray(data.records)) {
      // 如果返回的是分页数据结构
      submissions.value = data.records.map((item, index) => ({
        id: item.id || `${currentPage.value}-${index}`,
        title: item.title || '未知题目',
        user: userStore.user.username || userStore.user.name || '用户',
        time: formatTime(item.createTime),
        status: mapStatusFromCnName(item.cnName), // 用于CSS样式类
        cnName: item.cnName || '未知状态' // 直接保存中文状态用于显示
      }))
    } else {
      submissions.value = []
    }
  } catch (error) {
    console.error('获取最近提交失败:', error)
    submissions.value = []
  } finally {
    isLoading.value = false
  }
}

// 修改：初始化数据时先获取总数，再获取当前页数据
async function initializeData() {
  if (!userStore.isLogin || !userStore.user?.uuid) return

  await fetchTotalCount() // 先获取总数
  await fetchRecentSubmissions() // 再获取当前页数据
}

function mapStatusFromCnName(cnName) {
  // 根据中文状态名称映射到CSS样式类
  if (!cnName) return 'pending'

  const statusStr = cnName.toString()
  if (statusStr === '通过' || statusStr === 'AC') return 'accepted'
  if (statusStr === '答案错误' || statusStr === 'WA') return 'rejected'
  if (statusStr.includes('超时') || statusStr === 'TLE') return 'rejected'
  if (statusStr.includes('内存') || statusStr === 'MLE') return 'rejected'
  if (statusStr.includes('运行错误') || statusStr === 'RE') return 'rejected'
  if (statusStr.includes('编译错误') || statusStr === 'CE') return 'rejected'
  if (statusStr.includes('格式错误') || statusStr === 'PE') return 'rejected'

  return 'pending'
}

function formatTime(timestamp) {
  if (!timestamp) return '未知时间'

  const now = new Date()
  const date = new Date(timestamp)

  // 检查日期是否有效
  if (isNaN(date.getTime())) return '未知时间'

  const diff = Math.floor((now - date) / 1000 / 60) // 分钟差

  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff}分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)}小时前`
  return `${Math.floor(diff / 1440)}天前`
}

function prevPage() {
  console.log('点击上一页，当前页:', currentPage.value)
  if (currentPage.value > 1 && !isLoading.value) {
    currentPage.value--
    console.log('切换到页码:', currentPage.value)
    fetchRecentSubmissions()
  }
}

function nextPage() {
  console.log('点击下一页，当前页:', currentPage.value, '总页数:', totalPages.value)
  if (currentPage.value < totalPages.value && !isLoading.value) {
    currentPage.value++
    console.log('切换到页码:', currentPage.value)
    fetchRecentSubmissions()
  }
}

// 监听登录状态变化
watch(() => userStore.isLogin, (newValue) => {
  if (newValue) {
    currentPage.value = 1
    initializeData() // 使用新的初始化方法
  } else {
    submissions.value = []
    totalCount.value = 0
    totalPages.value = 1
  }
})

onMounted(() => {
  initializeData() // 使用新的初始化方法
})

</script>

<style scoped>
.login-prompt {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.prompt-text {
  font-size: 18px;
  font-weight: bold;
  color: #666;
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
}

.section {
  min-height: 284px;
  max-height: none;
  overflow: visible;
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

.submission-list {
  list-style: none;
  min-height: 200px; /* 保持最小高度，减少布局跳动 */
  transition: opacity 0.2s ease; /* 添加过渡效果 */
}

.submission-list.loading {
  opacity: 0.6; /* 加载时降低透明度 */
}

.submission-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.submission-item:last-child {
  border-bottom: none;
}

.submission-info {
  display: flex;
  flex-direction: column;
}

.submission-title {
  font-weight: bold;
  margin-bottom: 0;
}

.submission-meta {
  font-size: 12px;
  color: #777;
}

.submission-status {
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: bold;
}

.status-accepted {
  background-color: #d4edda;
  color: #155724;
}

.status-rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.status-pending {
  background-color: #e2e3e5;
  color: #383d41;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background-color: white;
  color: #3498db;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f8f9fa;
  border-color: #3498db;
}

.page-btn:disabled {
  color: #999;
  cursor: not-allowed;
  background-color: #f8f9fa;
}

.page-info {
  font-size: 14px;
  color: #666;
  margin: 0 10px;
}

.loading {
  text-align: center;
  padding: 40px;
  color: #666;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
  min-height: 200px; /* 保持最小高度 */
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
