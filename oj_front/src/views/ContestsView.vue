<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useContestStore } from '@/stores/contestStore'

const contestStore = useContestStore()

const contestStatusText = {
  upcoming: '未开始',
  running: '进行中',
  ended: '已结束'
}

const filteredContests = computed(() => {
  return contestStore.contests
})

let timer = null

onMounted(async () => {
  await contestStore.fetchContests()
  // 每分钟更新一次比赛状态和倒计时
  timer = setInterval(() => {
    contestStore.updateContestStatus()
  }, 60000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div class="contests-view">
    <div class="contests-header">
      <div class="header-main">
        <h1>比赛列表</h1>
        <div v-if="contestStore.getLastUpdateTime()" class="update-time">
          最后更新: {{ contestStore.getLastUpdateTime() }}
        </div>
      </div>
    </div>

    <div v-if="contestStore.loading" class="loading">
      <div class="loading-spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="contestStore.error" class="error">
      <div class="error-icon">!</div>
      <div class="error-content">
        <div class="error-message">{{ contestStore.error }}</div>
      </div>
    </div>

    <div v-else class="contests-list">
      <div
        v-for="contest in filteredContests"
        :key="contest.id"
        class="contest-item"
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
            <span class="contest-oj-tag" :data-oj="contest.oj">{{ contest.oj }}</span>
          </a>
          <div class="contest-meta">
            <span class="contest-status" :class="contest.status">
              {{ contestStatusText[contest.status] }}
            </span>
            <span
              v-if="contest.phase"
              class="contest-phase"
              :data-phase="contest.phase"
            >
              {{ contest.phase }}
            </span>
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

    <div v-if="!contestStore.loading && !contestStore.error && filteredContests.length === 0" class="no-contests">
      <div class="no-contests-icon">📅</div>
      <div class="no-contests-text">当前没有比赛</div>
    </div>
  </div>
</template>

<style scoped>
.contests-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.contests-header {
  margin-bottom: 30px;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.contests-header h1 {
  font-size: 24px;
  color: #2c3e50;
  margin: 0;
}

.update-time {
  font-size: 14px;
  color: #666;
  font-style: italic;
}

.contests-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.contest-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  background-color: #fff;
  transition: all 0.2s;
  border: 1px solid #eee;
}

.contest-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.contest-time {
  min-width: 180px;
  text-align: center;
  margin-right: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.contest-date {
  font-weight: bold;
  color: #2c3e50;
}

.contest-duration {
  font-size: 13px;
  color: #7f8c8d;
}

.countdown {
  font-size: 13px;
  color: #e74c3c;
  font-weight: 500;
}

.contest-info {
  flex: 1;
}

.contest-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #27ae60;
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  transition: color 0.2s;
}

.contest-name:hover {
  color: #219653;
}

.contest-oj-tag {
  font-size: 12px;
  padding: 2px 8px;
  background: #f0f0f0;
  border-radius: 4px;
  color: #666;
}

/* AtCoder specific styles */
.contest-oj-tag[data-oj="AtCoder"] {
  background: #2c3e50;
  color: white;
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

.contest-meta {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #7f8c8d;
}

.contest-status {
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 500;
  margin-right: 12px;
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
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.contest-action {
  flex-shrink: 0;
  width: 100px;
  text-align: right;
}

.btn {
  border: none;
  padding: 6px 14px;
  border-radius: 4px;
  font-size: 13px;
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

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px;
  color: #666;
}

.loading-spinner {
  width: 30px;
  height: 30px;
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
  gap: 15px;
  padding: 20px;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  color: #ff4d4f;
}

.error-icon {
  width: 24px;
  height: 24px;
  background: #ff4d4f;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.error-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.no-contests {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 40px;
  background: #f8f9fa;
  border-radius: 8px;
  color: #7f8c8d;
}

.no-contests-icon {
  font-size: 32px;
}

.no-contests-text {
  font-style: italic;
}
</style>
