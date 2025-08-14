<template>
  <div class="problem-page-view">
    <div class="resizable-container">
      <ProblemContext
        ref="leftPanel"
        class="problem-context"
      />
      <div class="resize-handle" @mousedown="startResize"></div>
      <div class="right-panel">
        <ProblemWrite
          class="problem-write"
          :problemId="problemStore.currentProblem?.id"
          ref="problemWrite"
          @code-change="handleCodeChange"
          @language-change="handleLanguageChange"
        />
      </div>
    </div>
    <!-- 将Console移到最外层 -->
    <Console
      ref="consoleRef"
      :current-code="currentCode"
      :current-language="currentLanguage"
      :problem-id="problemStore.currentProblem?.id"
      :initial-test-input="''"
      :submission-result="null"
    />
    <div v-if="showResultModal" class="result-modal">
      <div class="result-modal-content">
        <button class="close-btn" @click="closeResultModal">×</button>
        <SubmissionResultView
          :result="submissionResult"
          :is-polling="isPolling"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useProblemStore } from '@/stores/problemStore'
import ProblemContext from '@/components/Problems/ProblemPage/ProblemContext.vue'
import ProblemWrite from '@/components/Problems/ProblemPage/ProblemWrite.vue'
import Console from '@/components/Problems/ProblemPage/Console.vue'
import SubmissionResultView from '@/components/Submission/SubmissionResultView.vue'

const route = useRoute()
const problemStore = useProblemStore()
const leftPanel = ref(null)
const problemWrite = ref(null)
const consoleRef = ref(null)

const currentCode = ref('// 在这里编写你的代码')
const currentLanguage = ref('JavaScript')
const submissionResult = ref(null)
const showResultModal = ref(false)

// 添加轮询状态管理
const isPolling = ref(false)
const currentCommitId = ref(null)

const handleCodeChange = (data) => {
  currentCode.value = data.code

  // 处理提交事件
  if (data.type === 'submit') {
    if (data.status === 'polling') {
      // 开始轮询状态
      isPolling.value = true
      currentCommitId.value = data.commitId
      showResultModal.value = true
      submissionResult.value = data.response || null
    } else if (data.status === 'completed') {
      // 轮询完成
      isPolling.value = false
      currentCommitId.value = null
      submissionResult.value = data.response
    } else if (data.status === 'error') {
      // 轮询出错
      isPolling.value = false
      currentCommitId.value = null
      submissionResult.value = data.response
    } else {
      // 直接显示结果（没有轮询）
      showResultModal.value = true
      submissionResult.value = data.response
    }
  }
}

const handleLanguageChange = (data) => {
  currentLanguage.value = data.display
}

let isResizing = false
const startResize = (e) => {
  isResizing = true
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  e.preventDefault()
}

const handleResize = (e) => {
  if (!isResizing) return;
  const container = document.querySelector('.resizable-container');
  const containerRect = container.getBoundingClientRect();
  const leftWidth = e.clientX - containerRect.left;
  const newWidth = Math.max(300, Math.min(containerRect.width * 0.7, leftWidth));
  leftPanel.value.$el.style.flex = `0 0 ${newWidth}px`;
}

const stopResize = () => {
  isResizing = false
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
}

const closeResultModal = () => {
  showResultModal.value = false
}

onMounted(async () => {
  const problemId = route.params.id
  if (problemId) {
    await problemStore.fetchProblemDetail(problemId)
  }
})
</script>

<style scoped>
:root {
  --primary-color: #3498db;
  --light-gray: #f5f5f5;
  --medium-gray: #e0e0e0;
  --dark-gray: #333;
  --dotted-bg: #f9f9f9;
  --sidebar-width: 220px;
  --success-color: #2ecc71;
  --danger-color: #e74c3c;
  --warning-color: #f39c12;
}

.problem-page-view {
  height: 100vh;
  overflow: hidden;
  position: relative; /* 添加相对定位，作为Console的定位上下文 */
}

/* 当显示布局时，减去Navbar和Footer的高度 */
.app-container.show-layout .problem-page-view {
  height: calc(100vh - var(--navbar-height) - var(--footer-height));
}

.resizable-container {
  display: flex;
  height: 100%;
  position: relative;
}

.problem-context {
  flex: 0 0 50%;
  min-width: 300px;
  max-width: 70%;
  height: 100%;
  display: flex;
}

.right-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 300px;
  height: 100%;
}

.problem-write {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.resize-handle {
  width: 10px;
  background: #ddd;
  cursor: col-resize;
  transition: background 0.2s;
}

.resize-handle:hover {
  background: #3498db;
}

/* Console组件样式 */
:deep(.console-container) {
  position: fixed !important;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

:deep(#mini-console) {
  position: fixed !important;
  z-index: 1001;
}

.result-modal {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.result-modal-content {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.08);
  padding: 0;
  min-width: 350px;
  text-align: center;
  position: relative;
}
.close-btn {
  position: absolute;
  right: 16px;
  top: 16px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  z-index: 10;
}
</style>
