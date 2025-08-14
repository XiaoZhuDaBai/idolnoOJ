<template>
  <div class="tags-container">
    <button class="tags-toggle" @click="toggleTagsPanel">{{ tagsButtonText }}</button>
    <div class="tags-panel" :class="{ active: showTagsPanel }">
      <div class="tags-header">
        <span class="tags-title">标签</span>
        <button class="reset-btn" @click="resetFilters">重置</button>
      </div>
      <div class="tags-content">
        <div class="filter-section">
        <div class="platform-list">
          <h3>题库</h3>
          <div class="tag-item">
            <input
              type="checkbox"
              id="all-platforms"
              v-model="selectAllPlatforms"
              @change="toggleAllPlatforms"
            >
            <label for="all-platforms">全部</label>
          </div>
          <div class="tag-item" v-for="platform in platforms" :key="platform.id">
            <input
              type="checkbox"
              :id="platform.id"
              v-model="selectedPlatforms"
              :value="platform.id"
              @change="handlePlatformChange"
            >
            <label :for="platform.id">{{ platform.name }}</label>
          </div>
        </div>

        <div class="difficulty-list">
          <h3>难度</h3>
          <div class="tag-item" v-for="difficulty in difficulties" :key="difficulty.id" :class="`difficulty-${difficulty.id}`">
            <div
              class="difficulty-option"
              :class="{ 'selected': selectedDifficulties.includes(difficulty.id) }"
              @click="handleDifficultyClick(difficulty.id)"
            >
              {{ difficulty.name }}
            </div>
          </div>
        </div>
      </div>

        <div class="algorithm-list">
          <h3>算法标签</h3>
          <div class="tag-item" v-for="tag in algorithmTags" :key="tag.id">
            <input
              type="checkbox"
              :id="tag.id"
              v-model="selectedAlgorithmTags"
              :value="tag.id"
            >
            <label :for="tag.id">{{ tag.name }}</label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['filter-change'])

const showTagsPanel = ref(false)
const selectAllPlatforms = ref(true)
const selectedPlatforms = ref([])
const selectedDifficulties = ref([])
const selectedAlgorithmTags = ref([])

const platforms = ref([
  { id: 'leetcode', name: 'LeetCode' },
  { id: 'atcoder', name: 'AtCoder' },
  { id: 'codeforces', name: 'Codeforces' },
  { id: 'nowcoder', name: 'NowCoder' }
])

const difficulties = ref([
  { id: 0, name: '简单' },
  { id: 1, name: '中等' },
  { id: 2, name: '困难' }
])

const algorithmTags = ref([
  // 基础算法
  { id: 'simulation', name: '模拟' },
  { id: 'greedy', name: '贪心' },
  { id: 'dynamic-programming', name: '动态规划' },
  { id: 'search', name: '搜索' },
  { id: 'graph', name: '图论' },
  { id: 'math', name: '数学' },
  { id: 'string', name: '字符串' },
  { id: 'data-structures', name: '数据结构' },
  { id: 'geometry', name: '几何' },
  // 搜索相关
  { id: 'depth-first-search', name: '深度优先搜索' },
  { id: 'breadth-first-search', name: '广度优先搜索' },
  { id: 'binary-search', name: '二分查找' },
  { id: 'backtracking', name: '回溯' },
  // 数据结构
  { id: 'array', name: '数组' },
  { id: 'linked-list', name: '链表' },
  { id: 'stack', name: '栈' },
  { id: 'queue', name: '队列' },
  { id: 'tree', name: '树' },
  { id: 'heap-priority-queue', name: '堆' },
  { id: 'hash-table', name: '哈希表' },
  { id: 'union-find', name: '并查集' },
  { id: 'trie', name: '字典树' },
  // 图论
  { id: 'shortest-path', name: '最短路' },
  { id: 'minimum-spanning-tree', name: '最小生成树' },
  { id: 'topological-sort', name: '拓扑排序' },
  { id: 'strongly-connected-components', name: '强连通分量' },
  { id: 'eulerian-circuit', name: '欧拉回路' },
  // 动态规划
  { id: 'linear-dynamic-programming', name: '线性DP' },
  { id: 'interval-dynamic-programming', name: '区间DP' },
  { id: 'tree-dynamic-programming', name: '树形DP' },
  { id: 'state-compression-dynamic-programming', name: '状态压缩DP' },
  { id: 'digit-dynamic-programming', name: '数位DP' },
  // 数学
  { id: 'number-theory', name: '数论' },
  { id: 'combinatorics', name: '组合数学' },
  { id: 'probability', name: '概率论' },
  { id: 'matrix', name: '矩阵快速幂' },
  { id: 'game-theory', name: '博弈论' },
  // 字符串
  { id: 'string-matching', name: 'KMP' },
  { id: 'suffix-array', name: '后缀数组' },
  { id: 'manacher', name: 'Manacher' },
  { id: 'aho-corasick', name: 'AC自动机' },
  // 其他
  { id: 'divide-and-conquer', name: '分治' },
  { id: 'two-pointers', name: '双指针' },
  { id: 'sliding-window', name: '滑动窗口' },
  { id: 'prefix-sum', name: '前缀和' },
  { id: 'difference-array', name: '差分' },
  { id: 'bit-manipulation', name: '位运算' },
  { id: 'sorting', name: '排序' },
  { id: 'counting', name: '计数' },
  { id: 'bucket-sort', name: '桶排序' },
  { id: 'merge-sort', name: '归并排序' },
  { id: 'quick-sort', name: '快速排序' }
])

const tagsButtonText = computed(() => {
  const count = selectedPlatforms.value.length + selectedDifficulties.value.length + selectedAlgorithmTags.value.length
  return count > 0 ? `标签 (${count})` : '标签'
})

function toggleTagsPanel() {
  showTagsPanel.value = !showTagsPanel.value
}

function resetFilters() {
  selectAllPlatforms.value = true
  selectedPlatforms.value = []
  selectedDifficulties.value = []
  selectedAlgorithmTags.value = []
}

function toggleAllPlatforms() {
  if (selectAllPlatforms.value) {
    selectedPlatforms.value = []
  } else {
    // 如果取消全选，则选择所有平台
    selectedPlatforms.value = platforms.value.map(p => p.id)
  }
}

function handlePlatformChange() {
  selectAllPlatforms.value = selectedPlatforms.value.length === 0
  // 如果选择了所有平台，则设置为全选
  if (selectedPlatforms.value.length === platforms.value.length) {
    selectAllPlatforms.value = true
  }
}

function handleDifficultyClick(difficultyId) {
  if (selectedDifficulties.value.includes(difficultyId)) {
    // 如果点击的是已选中的难度，则取消选择
    selectedDifficulties.value = []
  } else {
    // 如果点击的是未选中的难度，则只选择该难度
    selectedDifficulties.value = [difficultyId]
  }
  emit('filter-change', {
    platforms: selectedPlatforms.value,
    difficulties: selectedDifficulties.value,
    tags: selectedAlgorithmTags.value
  })
}

// 点击页面其他地方关闭面板
document.addEventListener('click', (e) => {
  if (!e.target.closest('.tags-container')) {
    showTagsPanel.value = false
  }
})

// 暴露获取当前选择的方法
defineExpose({
  getSelectedFilters: () => ({
    platforms: selectedPlatforms.value,
    difficulties: selectedDifficulties.value,
    tags: selectedAlgorithmTags.value
  })
})
</script>

<style scoped>
.tags-container {
  position: relative;
  margin-right: 10px;
}

.tags-toggle {
  padding: 10px 15px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  width: 100%;
  min-width: 120px;
  justify-content: space-between;
}

.tags-toggle::after {
  content: "▼";
  font-size: 12px;
  margin-left: 5px;
  transition: transform 0.3s;
}

.tags-toggle.collapsed::after {
  transform: rotate(-90deg);
}

.tags-panel {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 15px;
  margin-top: 5px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  z-index: 10;
  display: none;
}

@media (min-width: 576px) {
  .tags-panel {
    width: 600px;
  }
}

.tags-panel.active {
  display: block;
}

.tags-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.tags-title {
  font-weight: bold;
  font-size: 16px;
}

.reset-btn {
  background: none;
  border: none;
  color: #3498db;
  cursor: pointer;
  font-size: 14px;
}

.tags-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 15px;
  max-height: 500px;
  overflow-y: auto;
  padding-right: 10px;
}

.filter-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.platform-list, .difficulty-list {
  margin-bottom: 15px;
}

.platform-list h3, .difficulty-list h3 {
  font-size: 14px;
  margin-bottom: 12px;
  color: #555;
  font-weight: 600;
}

.tag-item {
  display: inline-block;
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-item label {
  display: inline-block;
  padding: 4px 12px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tag-item label:hover {
  background-color: #e9ecef;
}

.tag-item input[type="checkbox"] {
  display: none;
}

.tag-item input[type="checkbox"]:disabled + label {
  opacity: 0.5;
  cursor: not-allowed;
}

.tag-item input[type="checkbox"]:checked + label {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.difficulty-list {
  padding-top: 10px;
}

.difficulty-list h3 {
  margin-bottom: 15px;
}

.difficulty-list .tag-item {
  margin: 4px;
}

.difficulty-option {
  display: inline-block;
  padding: 4px 12px;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.difficulty-option:hover {
  background-color: #e9ecef;
}

.difficulty-option.selected {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}

.difficulty-0 .difficulty-option {
  color: #28a745;
  font-weight: bold;
}

.difficulty-1 .difficulty-option {
  color: #ffc107;
  font-weight: bold;
}

.difficulty-2 .difficulty-option {
  color: #dc3545;
  font-weight: bold;
}

.difficulty-0 .difficulty-option.selected,
.difficulty-1 .difficulty-option.selected,
.difficulty-2 .difficulty-option.selected {
  color: white;
}

/* 添加滚动条样式 */
.tags-content::-webkit-scrollbar {
  width: 6px;
}

.tags-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.tags-content::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.tags-content::-webkit-scrollbar-thumb:hover {
  background: #555;
}

.tag-item label.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  pointer-events: none;
}

.tag-item input[type="checkbox"]:checked + label {
  background-color: #3498db;
  color: white;
  border-color: #3498db;
}
</style>
