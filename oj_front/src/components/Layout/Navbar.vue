<template>
  <nav class="navbar">
    <div class="logo">
      <img
        :src="logoUrl"
        alt="平台Logo"
        class="logo-image"
        @click="showZoomedLogo = true"
      >
      <div>
        <span class="name">不知道取什么</span>
        <span class="small-name">在线练习</span>
      </div>
    </div>

    <div v-if="showZoomedLogo" class="logo-zoom-overlay" @click="showZoomedLogo = false">
      <div class="zoomed-logo-container" @click.stop>
        <img :src="logoUrl" alt="平台Logo" class="zoomed-logo">
        <button class="close-btn" @click="showZoomedLogo = false">&times;</button>
      </div>
    </div>

    <ul class="nav-links">
      <li v-for="(link, index) in navLinks" :key="index">
        <router-link :to="{ name: link.route }" active-class="active">
          {{ link.text }}
        </router-link>
      </li>
    </ul>

    <div class="user-actions">
      <template v-if="isLogin">
        <div class="user-menu-wrapper" ref="menuRef">
          <img :src="userAvatar" class="user-avatar" style="width:48px;height:48px;border-radius:50%;object-fit:cover;vertical-align:middle;cursor:pointer;" @click.stop="toggleMenu" />
          <span style="margin-left:8px;vertical-align:middle;cursor:pointer;" @click.stop="toggleMenu">{{ userNickname }}</span>
          <div v-if="showMenu" class="user-menu">
            <div class="user-menu-item" @click="goToSettings">设置</div>
            <div class="user-menu-item" @click="logout">退出登录</div>
          </div>
        </div>
      </template>
      <template v-else>
        <a href="/loginORegister">登录</a>
      </template>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import logoImage from '@/icon/logo.jpg'
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'

defineOptions({
  name: 'MainNavbar'
})

const logoUrl = ref(logoImage)
const showZoomedLogo = ref(false)
const userStore = useUserStore()
const router = useRouter()

const navLinks = ref([
  { text: '首页', route: 'home' },
  { text: '题库', route: 'problems' },
  { text: '比赛', route: 'contests' },
  { text: '提交记录', route: 'submissions' },
  { text: '反馈', route: 'feedback'}
])

const isLogin = computed(() => userStore.isLogin && userStore.user)
const userAvatar = computed(() => userStore.user?.avatar || '/default-avatar.png')
const userNickname = computed(() => userStore.user?.nickname || userStore.user?.email || '用户')

const showMenu = ref(false)
const menuRef = ref(null)
const toggleMenu = () => {
  showMenu.value = !showMenu.value
}
const goToSettings = () => {
  showMenu.value = false
  router.push('/settings') // 你可以根据实际路由调整
}
const logout = () => {
  showMenu.value = false
  userStore.logout()
  router.push('/loginORegister')
}

const handleClickOutside = (event) => {
  if (menuRef.value && !menuRef.value.contains(event.target)) {
    showMenu.value = false
  }
}
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 5%;
  background-color: #ffffff;
  color: #2c3e50;
  flex-wrap: wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.logo {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  width: 100%;
  justify-content: center;
}

@media (min-width: 768px) {
  .logo {
    width: auto;
    margin-bottom: 0;
    justify-content: flex-start;
  }
}

.logo img {
  height: 40px;
  margin-right: 10px;
}

.logo .name {
  font-size: 16px;
  line-height: 1.2;
  color: #2c3e50;
  font-weight: 600;
}

.logo .small-name {
  font-size: 12px;
  display: block;
  color: #666;
}

.nav-links {
  display: flex;
  flex-wrap: wrap;
  list-style: none;
  justify-content: center;
  margin: 10px 0;
  width: 100%;
}

@media (min-width: 768px) {
  .nav-links {
    width: auto;
    margin: 0;
  }
}

.nav-links li {
  margin: 5px 10px;
}

.nav-links a {
  color: #2c3e50;
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-links a:hover {
  color: #3498db;
}

.nav-links a.active {
  color: #3498db;
  font-weight: 600;
}

.user-actions {
  display: flex;
  align-items: center;
  width: 100%;
  justify-content: center;
}

@media (min-width: 768px) {
  .user-actions {
    width: auto;
    justify-content: flex-end;
  }
}

.user-actions a {
  color: #2c3e50;
  text-decoration: none;
  margin-left: 20px;
  font-size: 16px;
  font-weight: 500;
  transition: color 0.3s ease;
}

.user-actions a:hover {
  color: #3498db;
}

.logo-image {
  height: 40px;
  width: auto;
  margin-right: 10px;
  object-fit: contain;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.logo-image:hover {
  transform: scale(1.05);
}

.logo-zoom-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  cursor: pointer;
}

.zoomed-logo-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.zoomed-logo {
  max-width: 100%;
  max-height: 80vh;
  object-fit: contain;
}

.close-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 30px;
  height: 30px;
  border: none;
  background-color: #f1f1f1;
  border-radius: 50%;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #e0e0e0;
}

.user-menu-wrapper {
  position: relative;
  display: inline-block;
}
.user-menu {
  position: absolute;
  right: 0;
  top: 40px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  min-width: 120px;
  z-index: 100;
  padding: 6px 0;
}
.user-menu-item {
  padding: 10px 20px;
  cursor: pointer;
  color: #333;
  font-size: 15px;
  transition: background 0.2s;
}
.user-menu-item:hover {
  background: #f5f5f5;
}
.user-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  vertical-align: middle;
  cursor: pointer;
}
</style>
