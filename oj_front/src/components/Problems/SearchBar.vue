<template>
  <div class="search-container">
    <TagFilter ref="tagFilterRef" />

    <div class="search-bar">
      <input
        type="text"
        v-model="searchQuery"
        placeholder="搜索题目..."
        @keyup.enter="doSearch"
      >
      <button @click="doSearch" type="button">搜索</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TagFilter from './TagFilter.vue'
import { useProblemStore } from '@/stores/problemStore'

const problemStore = useProblemStore()
const searchQuery = ref('')
const tagFilterRef = ref(null)

function doSearch() {
  // 获取当前选中的过滤器
  const selectedFilters = tagFilterRef.value.getSelectedFilters()

  // 构建搜索参数，完全匹配 SearchQuestionVo 结构
  const searchParams = {
    platform: selectedFilters.platforms && selectedFilters.platforms.length > 0
      ? selectedFilters.platforms[0]  // 使用第一个选中的平台
      : '',
    difficulty: selectedFilters.difficulties && selectedFilters.difficulties.length > 0
      ? selectedFilters.difficulties[0]
      : '',
    resource: searchQuery.value || '',
    tags: selectedFilters.tags || []
  }

  console.log('发送搜索参数:', searchParams)
  problemStore.searchProblems(searchParams)
}
</script>

<style scoped>
.search-container {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20px;
  position: relative;
  width: 100%;
}

.search-bar {
  flex: 1;
  display: flex;
  min-width: 200px;
  margin-bottom: 10px;
}

@media (min-width: 576px) {
  .search-bar {
    margin-bottom: 0;
  }
}

.search-bar input {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  font-size: 16px;
  outline: none;
}

.search-bar button {
  padding: 10px 15px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s;
}

.search-bar button:hover {
  background-color: #2980b9;
}
</style>
