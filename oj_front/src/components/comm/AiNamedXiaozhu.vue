<template>
  <div class="ai-assistant">
    <div v-if="isOpen"
         class="chat-window"
         :class="{ maximized: isMaximized, dragging: isDragging }"
         :style="{
           left: isMaximized ? '2.5vw' : position.left + 'px',
           top: isMaximized ? '5vh' : position.top + 'px',
           width: isMaximized ? '95vw' : chatWindowSize.width + 'px',
           height: isMaximized ? '90vh' : chatWindowSize.height + 'px',
           display: isMinimized ? 'none' : 'flex'
         }">
      <div class="chat-header" @mousedown="startDrag">
        <span>小助</span>
        <div class="chat-actions">
          <button class="min-btn" @click="minimizeChat" title="最小化">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="5" y="11" width="14" height="2" rx="1" fill="#fff"/></svg>
          </button>
          <button class="max-btn" @click="toggleMaximize" title="最大化/还原">
            <span v-if="!isMaximized">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="4" y="4" width="16" height="16" rx="2" stroke="#fff" stroke-width="2" fill="none"/></svg>
            </span>
            <span v-else>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><rect x="7" y="7" width="10" height="10" rx="2" stroke="#fff" stroke-width="2" fill="none"/></svg>
            </span>
          </button>
          <button class="close-btn" @click="closeChat" title="关闭">&times;</button>
        </div>
      </div>
      <div class="messages" ref="messagesContainer">
        <div v-for="(message, index) in messages"
             :key="index"
             :class="['chat-item', message.type]">
          <img v-if="message.type === 'ai'" src="@/icon/ai.jpg" alt="AI Avatar" class="avatar">
          <div :class="['message', message.type, { 'streaming': message.isStreaming }]">
            <div v-if="message.type === 'ai'" v-html="marked(message.content)" class="message-content"></div>
            <div v-else class="message-content">{{ message.content }}</div>
            <span v-if="message.isStreaming" class="cursor">|</span>
          </div>
        </div>
      </div>
      <div class="input-area">
        <input
          v-model="inputMessage"
          @keyup.enter="sendMessage"
          placeholder="请输入您的问题..."
          :disabled="isLoading"
        >
        <button
          v-if="!isLoading"
          @click="sendMessage"
          :disabled="!inputMessage.trim()"
        >
          发送
        </button>
        <button
          v-else
          @click="cancelCurrentRequest"
          class="cancel-btn"
        >
          取消
        </button>
      </div>
      <div class="resize-handle" @mousedown="startResize"></div>
    </div>
    <button v-else-if="isMinimized"
            class="float-btn"
            @click="restoreChat"
            style="right: 20px; bottom: 20px; position: fixed;"
            :class="{ 'disabled': !isLoggedIn }">
      <img src="@/icon/ai.jpg" alt="AI Icon" class="ai-icon-img">
      <div v-if="!isLoggedIn" class="login-required-overlay">
        <span>请先登录</span>
      </div>
    </button>
    <button v-else
            class="float-btn"
            @click="openChat"
            style="right: 20px; bottom: 20px; position: fixed;"
            :class="{ 'disabled': !isLoggedIn }">
      <img src="@/icon/ai.jpg" alt="AI Icon" class="ai-icon-img">
      <div v-if="!isLoggedIn" class="login-required-overlay">
        <span>请先登录</span>
      </div>
    </button>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, computed } from 'vue'
import { marked } from 'marked'
import { useUserStore } from '@/stores/userStore'

// 获取用户状态
const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLogin)
const currentUser = computed(() => userStore.user)

const isOpen = ref(false)
const isMaximized = ref(false)
const isMinimized = ref(false)
const isDragging = ref(false)
const isLoading = ref(false)
const currentAiMessage = ref('')
const inputMessage = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const chatWindowSize = ref({ width: 350, height: 500 })
const lastSize = ref({ width: 350, height: 500 })
const position = ref({ left: window.innerWidth - 370, top: window.innerHeight - 520 })
const dragState = ref({ startX: 0, startY: 0, offsetX: 0, offsetY: 0 })
const resizeState = ref({ resizing: false, startX: 0, startY: 0, startWidth: 0, startHeight: 0 })

const openChat = () => {
  // 检查用户是否已登录
  if (!isLoggedIn.value) {
    // 显示登录提示
    alert('请先登录后再使用AI助手')
    return
  }
  isOpen.value = true
  isMinimized.value = false
}
const closeChat = () => {
  isOpen.value = false
}
const minimizeChat = () => {
  isMinimized.value = true
  isOpen.value = false
}
const restoreChat = () => {
  // 检查用户是否已登录
  if (!isLoggedIn.value) {
    alert('请先登录后再使用AI助手')
    return
  }
  isMinimized.value = false
  isOpen.value = true
}
const toggleMaximize = () => {
  if (!isMaximized.value) {
    lastSize.value = { ...chatWindowSize.value }
    isMaximized.value = true
  } else {
    isMaximized.value = false
    chatWindowSize.value = { ...lastSize.value }
  }
}
const startDrag = (e) => {
  if (isMaximized.value) return
  isDragging.value = true
  dragState.value = {
    startX: e.clientX,
    startY: e.clientY,
    offsetX: e.clientX - position.value.left,
    offsetY: e.clientY - position.value.top
  }
  document.body.style.userSelect = 'none'
}
const handleMouseMove = (e) => {
  if (isDragging.value) {
    position.value.left = Math.max(0, Math.min(e.clientX - dragState.value.offsetX, window.innerWidth - chatWindowSize.value.width))
    position.value.top = Math.max(0, Math.min(e.clientY - dragState.value.offsetY, window.innerHeight - chatWindowSize.value.height))
  }
  if (resizeState.value.resizing) {
    const dx = e.clientX - resizeState.value.startX
    const dy = e.clientY - resizeState.value.startY
    chatWindowSize.value.width = Math.max(300, resizeState.value.startWidth + dx)
    chatWindowSize.value.height = Math.max(350, resizeState.value.startHeight + dy)
  }
}
const handleMouseUp = () => {
  isDragging.value = false
  resizeState.value.resizing = false
  document.body.style.userSelect = ''
}
const startResize = (e) => {
  if (isMaximized.value) return
  resizeState.value = {
    resizing: true,
    startX: e.clientX,
    startY: e.clientY,
    startWidth: chatWindowSize.value.width,
    startHeight: chatWindowSize.value.height
  }
  e.stopPropagation()
  document.body.style.userSelect = 'none'
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 使用 AbortController 来管理请求取消
const abortController = ref(null)

// 处理单个 SSE 数据块
const processSSEChunk = (chunk, lastMessage) => {
  try {
    const jsonStr = chunk.slice(5).trim() // 移除 'data:' 前缀
    if (!jsonStr) return false

    const data = JSON.parse(jsonStr)
    if (!data.output) return false

    // 处理完成信号
    if (data.output.finish_reason === 'stop') {
      return 'finished'
    }

    // 处理文本内容
    if (data.output.text) {
      currentAiMessage.value += data.output.text
      if (lastMessage) {
        lastMessage.content = currentAiMessage.value
      }
      return true
    }

    return false
  } catch (e) {
    console.error('解析 SSE 数据失败:', e, '原始数据:', chunk)
    return false
  }
}

// 使用 ReadableStream 处理流式响应
const handleStreamResponse = async (reader, lastMessage) => {
  try {
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()

      if (done) {
        // 处理缓冲区中剩余的数据
        if (buffer.trim()) {
          const lines = buffer.split('\n')
          for (const line of lines) {
            if (line.startsWith('data:')) {
              processSSEChunk(line, lastMessage)
            }
          }
        }
        break
      }

      // 将新数据添加到缓冲区
      const chunk = new TextDecoder().decode(value)
      buffer += chunk

      // 按行处理数据
      const lines = buffer.split('\n')

      // 保留最后一行（可能不完整）
      buffer = lines.pop() || ''

      // 处理完整的行
      let isFinished = false
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const result = processSSEChunk(line, lastMessage)
          if (result === 'finished') {
            isFinished = true
            break
          }
        }
      }

      if (isFinished) break

      // 滚动到底部
      await scrollToBottom()
    }

    // 完成处理
    if (lastMessage) {
      lastMessage.isStreaming = false
    }
    isLoading.value = false
    currentAiMessage.value = ''
    await scrollToBottom()

  } catch (error) {
    console.error('处理流数据时出错:', error)
    if (lastMessage) {
      lastMessage.content = currentAiMessage.value || '抱歉，处理响应时出错。'
      lastMessage.isStreaming = false
    }
    isLoading.value = false
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return

  // 检查用户是否已登录
  if (!isLoggedIn.value) {
    alert('请先登录后再使用AI助手')
    return
  }

  // 如果有正在进行的请求，取消它
  if (abortController.value) {
    abortController.value.abort()
  }

  // 创建新的 AbortController
  abortController.value = new AbortController()

  messages.value.push({
    type: 'user',
    content: inputMessage.value
  })

  const userMessage = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true
  currentAiMessage.value = ''

  // 添加 AI 消息占位符
  messages.value.push({
    type: 'ai',
    content: '',
    isStreaming: true
  })

  await scrollToBottom()

  try {
    // 获取用户UUID
    const uuid = currentUser.value?.uuid

    if (!uuid) {
      throw new Error('用户信息不完整，无法发送请求')
    }

    // 使用 fetch API 直接处理流式响应
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        prompt: userMessage,
        uuid: uuid  // 添加用户UUID
      }),
      signal: abortController.value.signal
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const lastMessage = messages.value[messages.value.length - 1]

    // 处理流式响应
    await handleStreamResponse(reader, lastMessage)

  } catch (error) {
    // 如果是取消请求导致的错误，不做处理
    if (error.name === 'AbortError') {
      console.log('请求已被取消')
      return
    }

    console.error('AI回复失败:', error)
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.type === 'ai') {
      lastMessage.content = '抱歉，我现在无法回答这个问题。请稍后再试。'
      lastMessage.isStreaming = false
    }
    isLoading.value = false
    currentAiMessage.value = ''
    await scrollToBottom()
  } finally {
    abortController.value = null
  }
}

// 取消当前请求的方法
const cancelCurrentRequest = () => {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
    isLoading.value = false

    // 更新最后一条消息
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.type === 'ai' && lastMessage.isStreaming) {
      lastMessage.content += '\n\n[用户已取消请求]'
      lastMessage.isStreaming = false
    }
  }
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mouseup', handleMouseUp)

  // 如果组件挂载时已经打开，但用户未登录，则关闭聊天窗口
  if (isOpen.value && !isLoggedIn.value) {
    isOpen.value = false
  }

  // 添加初始欢迎消息
  if (isLoggedIn.value && messages.value.length === 0) {
    messages.value.push({
      type: 'ai',
      content: '你好！我是小助，有什么可以帮到你的吗？',
      isStreaming: false
    })
  }
})
onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mouseup', handleMouseUp)
})
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.float-btn {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #3498db;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s;
  overflow: hidden;
  position: fixed;
  bottom: 20px;
  right: 20px;
}

.ai-icon-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.float-btn:hover {
  transform: scale(1.1);
}

.float-btn.disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.float-btn.disabled:hover {
  transform: none;
}

.login-required-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 12px;
  text-align: center;
}

.chat-window {
  position: fixed;
  z-index: 1000;
  min-width: 300px;
  min-height: 350px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.15);
  background: white;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  /* left/top/width/height 由绑定的 style 控制 */
}
.chat-window.maximized {
  width: 95vw !important;
  height: 90vh !important;
  left: 2.5vw !important;
  top: 5vh !important;
}
.chat-header {
  padding: 15px;
  background: #3498db;
  color: white;
  border-radius: 10px 10px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: move;
  user-select: none;
}
.chat-actions {
  display: flex;
  gap: 4px;
}
.min-btn, .max-btn, .close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
  margin-left: 4px;
}
.resize-handle {
  position: absolute;
  width: 16px;
  height: 16px;
  right: 0;
  bottom: 0;
  cursor: se-resize;
  z-index: 10;
}
.messages {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  width: 100%;
}

.chat-item.user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.chat-item.ai {
  justify-content: flex-start;
  flex-direction: row;
  align-self: flex-start;
}

.message {
  max-width: 85%;
  padding: 10px 15px;
  border-radius: 15px;
  word-break: break-word;
  position: relative;
}

.message-content {
  white-space: pre-wrap;
  overflow-wrap: break-word;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.message.user {
  background: #3498db;
  color: white;
  text-align: right;
}

.message.ai {
  background: #f0f0f0;
  color: #333;
}

.message.streaming {
  background: #f8f9fa;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background-color: #333;
  margin-left: 2px;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.input-area {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  flex-direction: row;
  gap: 10px;
}

.input-area input {
  flex: 1;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  outline: none;
}

.input-area button {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  flex-shrink: 0;
}

.input-area button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.input-area .cancel-btn {
  background: #e74c3c;
}

.input-area .cancel-btn:hover {
  background: #c0392b;
}

.message.ai :deep(h1) { font-size: 1.5em; margin: 0.5em 0; }
.message.ai :deep(h2) { font-size: 1.3em; margin: 0.4em 0; }
.message.ai :deep(h3) { font-size: 1.1em; margin: 0.3em 0; }
.message.ai :deep(p) { margin: 0.5em 0; }
.message.ai :deep(ul), .message.ai :deep(ol) { margin: 0.5em 0; padding-left: 1.5em; }
.message.ai :deep(li) { margin: 0.2em 0; }
.message.ai :deep(code) { background-color: #f0f0f0; padding: 0.2em 0.4em; border-radius: 3px; font-family: monospace; }
.message.ai :deep(pre) { background-color: #f0f0f0; padding: 1em; border-radius: 5px; overflow-x: auto; }
.message.ai :deep(blockquote) { border-left: 4px solid #ddd; margin: 0.5em 0; padding-left: 1em; color: #666; }
.message.ai :deep(hr) { border: none; border-top: 1px solid #ddd; margin: 1em 0; }
.message.ai :deep(strong) { font-weight: bold; }
.message.ai :deep(em) { font-style: italic; }
</style>
