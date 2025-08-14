<template>
  <div class="section contest-container">
    <div class="contest-header">
      <h2>近期比赛</h2>
      <router-link to="/contests" class="view-all">查看全部</router-link>
    </div>

    <div v-if="contestStore.loading" class="loading">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="contestStore.error" class="error">
      <div class="error-icon">!</div>
      <div class="error-message">{{ contestStore.error }}</div>
    </div>

    <div v-else class="contest-list">
      <div
        v-for="contest in recentContests"
        :key="contest.id"
        class="contest-item"
        :class="{
          'active': contest.status === 'running',
          'upcoming': contest.status === 'upcoming',
          'ended': contest.status === 'ended'
        }"
      >
        <div class="contest-time">
          <div class="contest-date">{{ contestStore.formatDate(contest.startTime) }}</div>
          <div class="contest-duration">{{ contest.duration.toFixed(1) }}小时</div>
          <div v-if="contest.status === 'upcoming'" class="countdown">
            {{ contestStore.formatCountdown(contest.startTime) }}
          </div>
        </div>
        <div class="contest-info">
          <a
            :href="contest.link"
            target="_blank"
            class="contest-name"
          >
            {{ contest.name }}
            <span class="contest-oj-tag">{{ contest.oj }}</span>
          </a>
          <div class="contest-meta">
            <span class="contest-status" :class="contest.status">
              {{ contestStatusText[contest.status] }}
            </span>
            <span class="contest-phase" v-if="contest.phase">{{ contest.phase }}</span>
          </div>
        </div>
        <div class="contest-action">
          <a
            v-if="contest.status === 'upcoming'"
            :href="contest.link"
            target="_blank"
            class="btn register"
          >
            立即查看
          </a>
          <a
            v-else-if="contest.status === 'running'"
            :href="contest.link"
            target="_blank"
            class="btn enter"
          >
            进入比赛
          </a>
          <div v-else class="contest-ended">已结束</div>
        </div>
      </div>
    </div>

    <div v-if="!contestStore.loading && !contestStore.error && recentContests.length === 0" class="no-contests">
      <div class="no-contests-icon">📅</div>
      <div class="no-contests-text">当前没有近期比赛</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useContestStore } from '@/stores/contestStore'

const contestStore = useContestStore()

const contestStatusText = {
  upcoming: '未开始',
  running: '进行中',
  ended: '已结束'
}

// 获取最近的5条比赛数据
const recentContests = computed(() => {
  return contestStore.contests.slice(0, 5)
})

onMounted(async () => {
  if (contestStore.contests.length === 0) {
    await contestStore.fetchContests()
  }
})
</script>

<style scoped>
.contest-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.contest-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.view-all {
  font-size: 14px;
  color: #3498db;
  text-decoration: none;
}

.view-all:hover {
  text-decoration: underline;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  color: #666;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 6px;
  color: #ff4d4f;
}

.error-icon {
  width: 20px;
  height: 20px;
  background: #ff4d4f;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.contest-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.contest-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-radius: 6px;
  background-color: #fff;
  margin-bottom: 0;
  transition: all 0.2s;
  border: 1px solid #eee;
  flex-shrink: 0;
}

.contest-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.contest-time {
  min-width: 100px;
  text-align: center;
  margin-right: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.contest-date {
  font-weight: bold;
  color: #2c3e50;
}

.contest-duration {
  font-size: 12px;
  color: #7f8c8d;
}

.countdown {
  font-size: 12px;
  color: #e74c3c;
  font-weight: 500;
}

.contest-info {
  flex: 1;
}

.contest-name {
  font-weight: bold;
  margin-bottom: 5px;
  color: #2c3e50;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: color 0.2s;
}

.contest-name:hover {
  color: #3498db;
}

.contest-oj-tag {
  font-size: 11px;
  padding: 2px 6px;
  background: #f0f0f0;
  border-radius: 3px;
  color: #666;
}

.contest-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #7f8c8d;
}

.contest-status {
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: bold;
  margin-right: 10px;
}

.contest-status.upcoming {
  background-color: #fff3e0;
  color: #f57c00;
}

.contest-status.running {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.contest-status.ended {
  background-color: #f5f5f5;
  color: #757575;
}

.contest-phase {
  font-size: 11px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.contest-phase[data-phase="初级"] {
  background: #3498db;
  color: white;
}

.contest-phase[data-phase="常规"] {
  background: #e74c3c;
  color: white;
}

.contest-phase[data-phase="高级"] {
  background: #2ecc71;
  color: white;
}

.contest-phase[data-phase="启发式算法"] {
  background: #9b59b6;
  color: white;
}

.contest-phase[data-phase="其他"] {
  background: #95a5a6;
  color: white;
}

.contest-action {
  flex-shrink: 0;
  width: 90px;
  text-align: right;
}

.btn {
  border: none;
  padding: 5px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-block;
}

.btn.register {
  background-color: #3498db;
  color: white;
}

.btn.register:hover {
  background-color: #2878b5;
}

.btn.enter {
  background-color: #27ae60;
  color: white;
}

.btn.enter:hover {
  background-color: #219653;
}

.contest-ended {
  color: #a0a0a0;
  font-size: 14px;
}

.no-contests {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
  color: #7f8c8d;
}

.no-contests-icon {
  font-size: 24px;
}

.no-contests-text {
  font-style: italic;
}
</style>
