import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/global.css' // 导入全局样式
import { useUserStore } from '@/stores/userStore'
import { GetJwt } from '@/api/UserApi'

const app = createApp(App)
app.use(router)
app.use(createPinia())
app.use(ElementPlus)

const userStore = useUserStore()
userStore.restoreFromStorage()

// 校验 token 是否有效
if (userStore.user && userStore.user.uuid) {
  GetJwt(userStore.user.uuid).then(token => {
    if (!token) {
      userStore.logout()
    } else {
      userStore.setToken(token)
    }
  }).catch(() => {
    userStore.logout()
  })
}

app.mount('#app')
