<template>
  <div class="problems-container">
    <div class="main-content">
      <SearchBar />
      <ProblemTable />
    </div>
    <div class="sidebar">
      <RecentWA />
      <TestPlatform />
    </div>
  </div>
</template>


<script setup>
import SearchBar from '@/components/Problems/SearchBar.vue'
import ProblemTable from '@/components/Problems/ProblemTable.vue'
import RecentWA from '@/components/Problems/RecentWA.vue'
import TestPlatform from '@/components/Home/TestPlatform.vue'
</script>

<style scoped>
.problems-container {
  display: flex;
  flex-direction: column;
  padding: 16px;
  min-height: calc(100vh - 64px); /* Assuming header height is 64px */
}

@media (min-width: 992px) {
  .problems-container {
    flex-direction: row;
    max-width: 1400px;
    margin: 0 auto;
    padding: 20px;
    gap: 24px;
  }
}

.main-content {
  flex: 1;
  margin-bottom: 24px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 16px;
}

@media (min-width: 992px) {
  .main-content {
    margin-bottom: 0;
  }
}

.sidebar {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

@media (min-width: 992px) {
  .sidebar {
    width: 320px;
  }
}
</style>
