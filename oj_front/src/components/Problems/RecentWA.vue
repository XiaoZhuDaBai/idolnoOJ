<template>
  <div class="wa-container">
    <div class="header">
      <h2>近期提交未过的题目</h2>
      <span class="badge">{{ waList.length }}</span>
    </div>

    <div class="scroll-container">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      <ul v-else class="wa-list">
        <li
          v-for="(item, index) in waList"
          :key="index"
          class="wa-item"
          @click="viewProblem(item.id)"
        >
          <div class="problem-id">{{ item.id }}</div>
          <div class="problem-title">{{ item.title }}</div>
          <div class="attempt-count">尝试: {{ item.attempts }}次</div>
        </li>
      </ul>
    </div>

    <div class="footer" v-if="!loading && waList.length === 0">
      暂时没有WA记录，继续保持！
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { WrongCommit } from '@/api/UserApi.js'
import { useUserStore } from '@/stores/userStore.js'

const waList = ref([])
const loading = ref(true)
const userStore = useUserStore()

// 获取错误提交数据
async function fetchWrongCommits() {
  try {
    loading.value = true
    const uuid = userStore.user?.uuid
    if (!uuid) {
      console.warn('用户未登录，无法获取错误提交数据')
      return
    }

    const response = await WrongCommit(uuid)

    // 检查响应数据结构
    let apiData = response.data || response
    if (!Array.isArray(apiData)) {
      apiData = []
    }

    // 转换API数据格式
    waList.value = apiData.map(item => ({
      id: item.problemId,
      title: item.problemName,
      attempts: item.tryCount
    }))

  } catch (error) {
    console.error('获取错误提交数据失败:', error)
    // 使用默认数据作为后备
    waList.value = []
  } finally {
    loading.value = false
  }
}

function viewProblem(id) {
  // 实际应跳转到问题详情页
  console.log('查看问题:', id);
}

onMounted(() => {
  fetchWrongCommits()
})
</script>

<style scoped>
.wa-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.header h2 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.badge {
  background: #ff4d4f;
  color: white;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
}

.scroll-container {
  max-height: 300px;
  overflow-y: auto;
}

.wa-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.wa-item {
  display: flex;
  flex-direction: column;
  padding: 7px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.wa-item:hover {
  background-color: #f9f9f9;
}

.wa-item:last-child {
  border-bottom: none;
}

.problem-id {
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 4px;
  font-size: 14px;
}

.problem-title {
  color: #333;
  margin-bottom: 4px;
  font-size: 14px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.attempt-count {
  color: #ff4d4f;
  font-size: 12px;
}

.footer {
  padding: 16px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 滚动条样式 */
.scroll-container::-webkit-scrollbar {
  width: 6px;
}

.scroll-container::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.scroll-container::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  color: #999;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid #f3f3f3;
  border-top: 2px solid #ff4d4f;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 8px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
