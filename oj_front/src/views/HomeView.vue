<template>
  <div class="home-container">
    <div class="left-column">
      <SubmissionAnalysis />
      <RecentSubmissions />
    </div>
    <div class="right-column">
      <RecentContests />
    </div>
    <AiNamedXiaozhu />
  </div>
</template>

<script setup>
import SubmissionAnalysis from '@/components/Home/SubmissionAnalysis.vue'
import RecentSubmissions from '@/components/Home/RecentSubmissions.vue'
import RecentContests from '@/components/Home/RecentContests.vue'
import AiNamedXiaozhu from '@/components/comm/AiNamedXiaozhu.vue'
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  padding: 20px;
  min-height: calc(100vh - 60px);
}

@media (min-width: 992px) {
  .home-container {
    flex-direction: row;
    padding: 20px 5%;
    gap: 20px;
  }
}

.left-column {
  flex: 2;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (min-width: 992px) {
  .left-column {
    margin-right: 0;
    margin-bottom: 0;
  }
}

.right-column {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 确保子组件不会溢出 */
.left-column > *,
.right-column > * {
  flex-shrink: 0;
}

:deep(.ai-assistant) {
  position: fixed !important;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}
</style>
