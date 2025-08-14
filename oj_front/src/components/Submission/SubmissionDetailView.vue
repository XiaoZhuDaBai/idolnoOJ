<script setup>
import { ref, computed, onMounted } from 'vue'
import { useSubmissionStore } from '@/stores/submissionStroe'
import Prism from 'prismjs'
import 'prismjs/themes/prism.css'

const submissionStore = useSubmissionStore()
const loading = ref(true)

// 从 store 获取当前提交详情
const submission = computed(() => submissionStore.currentSubmission)

// 获取提交详情
const fetchSubmission = async () => {
  try {
    // 如果 store 中没有数据，尝试从 URL 参数获取
    if (!submission.value) {
      // 这里可以添加从 API 获取数据的逻辑
      console.warn('未找到提交详情数据，请从提交列表页面进入')
    }
  } catch (error) {
    console.error('获取提交详情失败:', error)
  } finally {
    loading.value = false
  }
}



// 获取语言对应的 CSS 类名
const getLanguageClass = (language) => {
  const languageMap = {
    'cpp': 'cpp',
    'java': 'java',
    'python': 'python',
    'javascript': 'javascript',
    'js': 'javascript'
  }
  return languageMap[language?.toLowerCase()] || 'javascript'
}

// 高亮代码
const highlightCode = () => {
  if (submission.value?.code) {
    setTimeout(() => {
      Prism.highlightAll()
    }, 100)
  }
}

onMounted(() => {
  fetchSubmission().then(() => {
    highlightCode()
  })
})
</script>

<template>
  <div class="submission-detail">
    <div v-if="loading" class="loading-state">
      加载中...
    </div>

    <div v-else-if="!submission" class="empty-state">
      未找到提交记录
    </div>

    <div v-else class="detail-container">
      <!-- 头部信息 -->
      <div class="detail-header">
        <h2>
          <router-link to="/submissions" class="back-link">
            <span class="back-arrow">←</span> 返回
          </router-link>
          {{ submission.problemName }}
        </h2>
        <div class="user-info">
          <img :src="submission.userAvatar" :alt="submission.username" class="user-avatar">
          <span>{{ submission.username }}</span>
        </div>
      </div>

      <!-- 主要内容区 -->
      <div class="detail-content">
        <!-- 左侧信息区 -->
        <div class="info-section">
          <div class="info-card">
            <h3>执行结果</h3>
            <div class="info-item">
              <span class="info-label">状态:</span>
              <span class="info-value">{{ submissionStore.getStatusText(submission.status) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">执行时间:</span>
              <span class="info-value">{{ submission.executionTime }}ms</span>
            </div>
            <div class="info-item">
              <span class="info-label">内存消耗:</span>
              <span class="info-value">{{ submissionStore.formatMemory(submission.memory) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">提交时间:</span>
              <span class="info-value">{{ submissionStore.formatDate(submission.submitTime) }}</span>
            </div>
          </div>
        </div>

        <!-- 右侧代码区 -->
        <div class="code-section">
          <div class="code-header">
            <h3>提交的代码</h3>
          </div>
          <pre class="line-numbers"><code :class="`language-${getLanguageClass(submission.language)}`">{{ submission.code }}</code></pre>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.submission-detail {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-state,
.empty-state {
  padding: 40px;
  text-align: center;
  color: #666;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.back-link {
  color: #3498db;
  text-decoration: none;
  margin-right: 15px;
}

.back-link:hover {
  text-decoration: underline;
}

.back-arrow {
  margin-right: 5px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.detail-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.info-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px;
}

.info-card h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.info-item {
  margin-bottom: 12px;
}

.info-label {
  display: inline-block;
  width: 80px;
  color: #666;
}

.info-value {
  color: #333;
  font-weight: 500;
}

.code-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.code-header {
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.code-header h3 {
  margin: 0;
}

pre[class*="language-"] {
  margin: 0;
  border-radius: 0;
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}
</style>
