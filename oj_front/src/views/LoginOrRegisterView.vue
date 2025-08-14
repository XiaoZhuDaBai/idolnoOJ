<!--登录扫码或邮箱-->
<template>
  <div class="login-view">
    <div class="login-tabs">
      <div
        class="tab-item"
        :class="{ active: activeTab === 'qrcode' }"
        @click="activeTab = 'qrcode'"
      >
        扫码登录
      </div>
      <div
        class="tab-item"
        :class="{ active: activeTab === 'email' }"
        @click="activeTab = 'email'"
      >
        邮箱登录
      </div>
    </div>

    <div class="login-content">
      <LoginOrRegisterByQrcode v-if="activeTab === 'qrcode'" />
      <LoginOrRegisterByEmail v-if="activeTab === 'email'" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LoginOrRegisterByQrcode from "@/components/comm/LoginOrRegisterByQrcode.vue";
import LoginOrRegisterByEmail from "@/components/comm/LoginOrRegisterByEmail.vue";

const activeTab = ref('qrcode')
</script>

<style scoped>
.login-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
  background-color: white;
  min-height: 400px;
}

.login-tabs {
  display: flex;
  margin-bottom: 25px;
  width: 100%;
  max-width: 400px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px;
  cursor: pointer;
  color: #666;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
  position: relative;
}

.tab-item.active {
  color: #1890ff;
  border-bottom: 2px solid #1890ff;
}

.tab-item:hover {
  color: #1890ff;
}

.login-content {
  width: 100%;
  max-width: 400px;
  flex: 1;
}
</style>
