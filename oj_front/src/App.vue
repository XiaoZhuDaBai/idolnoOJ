<template>
  <div class="app-container">
    <Navbar v-if="showLayout" />
    <main class="main-content">
      <router-view />
    </main>
    <Footer v-if="showLayout"/>
  </div>
</template>

<script setup>
import Navbar from '@/components/Layout/Navbar.vue'
import Footer from '@/components/Layout/Footer.vue'
import {computed} from "vue";
import {useRoute} from "vue-router";
const route = useRoute()
const showLayout = computed(() => route.meta.showLayout ?? true)
</script>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f5f5;
  /* 添加CSS变量定义 */
  --navbar-height: 60px;
  --footer-height: 50px;
}

/* 为包含布局的容器添加类名 */
.app-container:has(.show-layout) {
  min-height: 100vh;
}

.main-content {
  flex: 1;
}

</style>
