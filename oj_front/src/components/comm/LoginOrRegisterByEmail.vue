<template>
  <div class="login-container">
    <h2 class="login-title">邮箱登录</h2>
    <div class="login-divider"></div>
    <div v-if="message" class="message-msg">{{ message }}</div>
    <div class="input-group icon-input">
      <span class="input-icon email-icon"></span>
      <input type="email" v-model="email" placeholder="输入邮箱（自动注册）">
    </div>
    <div class="input-group verification-code icon-input">
      <span class="input-icon code-icon"></span>
      <input type="text" v-model="verificationCode" placeholder="请输入验证码">
      <button
        class="get-code-btn"
        :disabled="isGettingCode"
        @click="getVerificationCode"
      >
        {{ isGettingCode ? `重新获取(${codeTimer})` : '获取验证码' }}
      </button>
    </div>
    <button class="login-btn" @click="handleLogin" :disabled="loading">
      {{ loading ? '登录中...' : '登录' }}
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getVerifyCode, LoginOrRegister, GetJwt } from '@/api/UserApi'
import { useUserStore } from '@/stores/userStore'
import { useRouter } from 'vue-router'

const email = ref('')
const verificationCode = ref('')
const isGettingCode = ref(false)
const codeTimer = ref(0)
let timerInterval = null
const loading = ref(false)
const message = ref('')
const userStore = useUserStore()
const router = useRouter()

const getVerificationCode = async () => {
  if (isGettingCode.value) return
  message.value = ''
  if (!email.value) {
    message.value = '请输入邮箱';
    return
  }
  try {
    isGettingCode.value = true
    codeTimer.value = 60
    const res = await getVerifyCode(email.value)
    if (res && res.message) {
      message.value = res.message
    } else {
      message.value = '验证码已发送，请查收邮箱'
    }
    timerInterval = setInterval(() => {
      codeTimer.value--
      if (codeTimer.value <= 0) {
        isGettingCode.value = false
        clearInterval(timerInterval)
      }
    }, 1000)
  } catch (err) {
    if (err && err.response && err.response.data && err.response.data.message) {
      message.value = err.response.data.message
    } else {
      message.value = '验证码发送失败，请重试'
    }
    isGettingCode.value = false
  }
}

const handleLogin = async () => {
  message.value = ''
  if (!email.value || !verificationCode.value) {
    message.value = '请输入邮箱和验证码';
    return
  }
  loading.value = true
  try {
    const res = await LoginOrRegister({ email: email.value, input_code: verificationCode.value })
    if (res && res.code === 200 && res.status === true) {
      // data 是字符串，需要 parse
      const user = JSON.parse(res.data)
      let token = null
      if (user.uuid) {
        try {
          token = await GetJwt(user.uuid)
        } catch (e) {
          console.warn('获取token失败', e)
        }
      }
      userStore.setUser(user)
      if (token) {
        userStore.setToken(token)
      }
      router.push('/')
      return
    }
    // 失败时只展示 message
    if (res && res.message) {
      message.value = res.message
    } else {
      message.value = '未知错误';
    }
  } catch (err) {
    if (err && err.response && err.response.data && err.response.data.message) {
      message.value = err.response.data.message
    } else {
      message.value = '登录失败，请检查验证码或邮箱';
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 20px 30px 20px;
  margin: 32px auto;
  max-width: 400px;
  min-height: 532px;
  border: 1px solid #e6e6e6;
  border-radius: 18px;
  background: linear-gradient(135deg, #f9f9f9 60%, #e6f7ff 100%);
  box-shadow: 0 6px 24px rgba(0,0,0,0.08);
}

.login-title {
  font-size: 2rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.login-divider {
  width: 60px;
  height: 4px;
  background: linear-gradient(90deg, #09bb07 0%, #1890ff 100%);
  border-radius: 2px;
  margin-bottom: 32px;
}

.input-group {
  width: 100%;
  margin-bottom: 28px;
  position: relative;
}

.icon-input input {
  padding-left: 44px;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 22px;
  opacity: 0.5;
  pointer-events: none;
}
.email-icon {
  background: url('data:image/svg+xml;utf8,<svg fill="%23999" height="22" viewBox="0 0 24 24" width="22" xmlns="http://www.w3.org/2000/svg"><path d="M20 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>') no-repeat center/contain;
}
.code-icon {
  background: url('data:image/svg+xml;utf8,<svg fill="%23999" height="22" viewBox="0 0 24 24" width="22" xmlns="http://www.w3.org/2000/svg"><path d="M12 17a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm6-2c0-3.31-2.69-6-6-6S6 11.69 6 15H4c0-4.08 3.05-7.44 7-7.93V5h2v2.07c3.95.49 7 3.85 7 7.93h-2z"/></svg>') no-repeat center/contain;
}

input {
  width: 100%;
  padding: 15px 16px;
  border: 1px solid #eee;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s;
  height: 50px;
  border-radius: 8px;
  background: #fff;
  color: #222;
  font-family: inherit;
}

input:focus {
  outline: none;
  border-color: #1890ff;
  background: #f0faff;
}

input::placeholder {
  color: #bbb;
  font-size: 15px;
}

.verification-code {
  display: flex;
  gap: 16px;
  align-items: center;
}

.verification-code input {
  flex: 1;
}

.get-code-btn {
  background-color: #fff;
  border: 1px solid #09bb07;
  color: #09bb07;
  padding: 0 24px;
  height: 50px;
  cursor: pointer;
  font-size: 15px;
  white-space: nowrap;
  transition: all 0.3s;
  min-width: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(24,144,255,0.08);
  font-weight: 500;
}
.get-code-btn[disabled], .get-code-btn:disabled {
  color: #eee;
  background: #e0e0e0;
  border: none;
  cursor: not-allowed;
  box-shadow: none;
}
.get-code-btn:hover:not(:disabled) {
  background-color: #f6fff6;
  border-color: #07a105;
  color: #07a105;
}
.get-code-btn:active:not(:disabled) {
  background-color: #e6f7e6;
  border-color: #07a105;
  color: #07a105;
}

.login-btn {
  width: 100%;
  padding: 15px;
  background-color: #09bb07;
  color: white;
  border: none;
  font-size: 17px;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
  height: 50px;
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24,144,255,0.08);
  letter-spacing: 1px;
}
.login-btn:hover {
  background-color: #07a105;
}
.login-btn:active {
  background-color: #059104;
}

.message-msg {
  color: #222;
  background: #fff0f0;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 12px;
  width: 100%;
  text-align: center;
  font-size: 15px;
}

@media (max-width: 500px) {
  .login-container {
    max-width: 98vw;
    padding: 18px 2vw 18px 2vw;
    min-height: 400px;
  }
  .login-title {
    font-size: 1.3rem;
  }
}
</style>
