<template>
  <div class="problem-panel">
    <div class="problem-header">
      <div class="tab-switcher">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-btn"
          :class="{ active: activeTab === tab.id }"
          @click="changeTab(tab.id)"
        >
          {{ tab.label }}
        </button>
      </div>
    </div>

    <div class="tab-content" :class="{ active: activeTab === 'description' }">
      <div class="markdown-content">
        <h2 class="problem-title">
          {{ problem?.title || '加载中...' }}
          <div class="problem-tags">
            <span v-for="tag in problem?.tags" :key="tag" class="tag">
              {{ tag }}
            </span>
          </div>
        </h2>
        <div class="problem-meta">
          <div class="meta-item">
            <i class="fas fa-clock"></i>
            <span>时间限制: {{ problem?.timeLimit || '--' }}</span>
          </div>
          <div class="meta-item">
            <i class="fas fa-memory"></i>
            <span>内存限制: {{ problem?.memoryLimit || '--' }}</span>
          </div>
          <div class="meta-item">
            <i class="fas fa-tag"></i>
            <span :class="['difficulty', `difficulty-${problem?.difficulty || 'easy'}`]">
              {{ problem?.difficulty || '简单' }}
            </span>
          </div>
        </div>

        <!-- 直接展示完整的题目描述 -->
        <div class="problem-description" v-html="processedDescription"></div>
      </div>
    </div>

    <div class="tab-content" :class="{ active: activeTab === 'submissions' }">
      <div class="markdown-content">
        <h2>提交记录</h2>
        <p v-if="!problem?.submissions || problem?.submissions.length === 0">
          您还没有提交过此题的解答。
        </p>
        <div v-else class="submission-list">
          <!-- 提交记录列表 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useProblemStore } from '@/stores/problemStore'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

const problemStore = useProblemStore()
const problem = computed(() => problemStore.currentProblem || {
  title: '加载中...',
  timeLimit: '--',
  memoryLimit: '--',
  difficulty: '简单',
  description: '加载中...',
  examples: [],
  constraints: [],
  solution: {
    approach: '加载中...',
    steps: [],
    complexity: [],
    code: '加载中...'
  }
})

const processedDescription = computed(() => {
  if (!problem.value?.description) return '';
  return problem.value.description;
});

const activeTab = ref('description')
const tabs = ref([
  {id: 'description', label: '题目描述'},
  {id: 'submissions', label: '提交记录'}
])

const changeTab = (tabId) => {
  activeTab.value = tabId
}

onMounted(() => {
  // 初始化代码高亮
  if (typeof hljs !== 'undefined') {
    hljs.highlightAll()
  }
})
</script>

<style scoped>
@import url('https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css');

.problem-panel {
  flex: 1;
  height: 100%;
  overflow-y: auto;
  background-color: #fff;
  padding: 20px 25px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.problem-header {
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 15px;
}

.tab-switcher {
  display: flex;
  gap: 10px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 8px 16px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: all 0.3s;
}

.tab-btn.active {
  color: #3498db;
  border-bottom-color: #3498db;
  font-weight: bold;
}

.tab-content {
  display: none;
  padding: 15px 0;
}

.tab-content.active {
  display: block;
}

.markdown-content {
  line-height: 1.6;
}

.markdown-content h2 {
  font-size: 20px;
  margin: 20px 0 10px;
  color: #333;
}

.markdown-content .problem-title {
  color: #34dba1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.markdown-content h3 {
  font-size: 18px;
  margin: 15px 0 8px;
  color: #444;
}

.example {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin: 10px 0;
  border-left: 3px solid #3498db;
}

.problem-data {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  margin: 10px 0;
  border-left: 3px solid #3498db;
}

pre {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
}

.problem-meta {
  display: flex;
  gap: 15px;
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-item i {
  margin-right: 5px;
  color: #3498db;
}

.problem-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: normal;
}

.problem-data ul {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.problem-data li {
  margin: 8px 0;
  padding-left: 20px;
  position: relative;
}

.problem-data li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: #3498db;
}

.markdown-content img {
  max-width: 100%;
  height: auto;
  margin: 10px 0;
}

.markdown-content pre {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 10px 0;
}

.markdown-content strong {
  color: #333;
  font-weight: 600;
}

.problem-description {
  line-height: 1.6;
  color: #333;
}

.problem-description :deep(p) {
  margin: 12px 0;
}

.problem-description :deep(strong) {
  color: #2c3e50;
  font-weight: 600;
}

.problem-description :deep(strong.example) {
  color: #3498db;
  font-size: 1.1em;
  display: block;
  margin: 20px 0 10px;
}

.problem-description :deep(pre) {
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 10px 0;
  border: 1px solid #e9ecef;
}

.problem-description :deep(pre strong) {
  color: #2c3e50;
  font-weight: 600;
}

.problem-description :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 15px 0;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.problem-description :deep(ul) {
  margin: 15px 0;
  padding-left: 20px;
}

.problem-description :deep(li) {
  margin: 8px 0;
  line-height: 1.6;
}

.problem-description :deep(code) {
  background-color: #f1f3f5;
  padding: 2px 6px;
  border-radius: 4px;
  color: #e83e8c;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
  font-size: 0.9em;
}

.problem-description :deep(p:empty) {
  display: none;
}
</style>
