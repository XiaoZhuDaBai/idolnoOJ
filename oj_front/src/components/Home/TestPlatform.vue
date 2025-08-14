<template>
  <div class="section platforms-container">
    <h2>题目来源</h2>
    <div class="platform-intro">
      本平台支持以下主流在线测评平台题目导入和评测
    </div>
    <div class="platform-list">
      <div
        v-for="platform in platforms"
        :key="platform.id"
        class="platform-item"
        @click="visitPlatform(platform.url)"
      >
        <div class="platform-logo">
          <img :src="platform.logo" :alt="platform.name + 'logo'">
        </div>
        <div class="platform-info">
          <div class="platform-name">{{ platform.name }}</div>
          <div class="platform-desc">{{ platform.description }}</div>
          <div class="platform-stats">
            <span class="stat">
              <i class="stat-icon">📊</i>
              {{ platform.problemCount }}题
            </span>
            <span class="stat">
              <i class="stat-icon">👥</i>
              {{ platform.userCount }}
            </span>
          </div>
        </div>
        <div class="platform-link">
          <i class="external-icon">↗</i>
        </div>
      </div>
    </div>
    <div class="sync-tip">
      <i class="tip-icon">🔄</i>
      题目模式均为<h3>ACM</h3>模式
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";

const platforms = ref([
  {
    id: 1,
    name: 'LeetCode',
    description: '全球知名的算法题库平台',
    logo: 'https://assets.leetcode.com/static_assets/public/webpack_bundles/images/logo-dark.e99485d9b.svg',
    url: 'https://leetcode.com',
    problemCount: '2000+',
    userCount: '1000万+'
  },
  {
    id: 2,
    name: 'Codeforces',
    description: '竞技编程和算法竞赛平台',
    logo: 'https://codeforces.org/s/0/favicon-96x96.png',
    url: 'https://codeforces.com',
    problemCount: '8000+',
    userCount: '300万+'
  },
  {
    id: 3,
    name: 'AtCoder',
    description: '日本著名编程竞赛平台',
    logo: 'https://img.atcoder.jp/assets/favicon.png',
    url: 'https://atcoder.jp',
    problemCount: '5000+',
    userCount: '500万+'
  },
  {
    id: 4,
    name: 'NowCoder',
    description: '国内知名IT笔试面试平台',
    logo: 'https://www.nowcoder.com/favicon.ico',
    url: 'https://www.nowcoder.com',
    problemCount: '3000+',
    userCount: '600万+'
  }
])

function visitPlatform(url) {
  window.open(url, '_blank')
}
</script>

<style scoped>
.platforms-container {
  margin-top: 20px;
  margin-bottom: 0;
}

.platform-intro {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 15px;
}

.platform-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.platform-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.2s;
  cursor: pointer;
  border: 1px solid transparent;
}

.platform-item:hover {
  border-color: #3498db;
  background-color: rgba(52, 152, 219, 0.05);
}

.platform-logo {
  width: 40px;
  height: 40px;
  margin-right: 12px;
  flex-shrink: 0;
}

.platform-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.platform-info {
  flex: 1;
}

.platform-name {
  font-weight: bold;
  margin-bottom: 2px;
}

.platform-desc {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.platform-stats {
  display: flex;
  gap: 10px;
}

.stat {
  font-size: 12px;
  color: #5f6a7a;
}

.stat-icon {
  margin-right: 3px;
}

.platform-link {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #3498db;
  opacity: 0;
  transition: opacity 0.2s;
}

.platform-item:hover .platform-link {
  opacity: 1;
}

.sync-tip {
  margin-top: 15px;
  font-size: 12px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tip-icon {
  margin-right: 5px;
  font-size: 14px;
}
</style>
