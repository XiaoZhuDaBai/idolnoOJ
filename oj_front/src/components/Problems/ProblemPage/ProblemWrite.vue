<template>
  <div class="code-panel">
    <div class="code-header">
      <select class="language-selector" v-model="selectedLanguage" @change="changeLanguage">
        <option value="javascript">JavaScript</option>
        <option value="python">Python</option>
        <option value="java">Java</option>
        <option value="csharp">C#</option>
        <option value="cpp">C++</option>
      </select>
      <div class="code-actions">
        <button class="btn btn-outline" @click="resetCode">
          <i class="fas fa-redo"></i>
          重置
        </button>
        <button
          class="btn btn-success"
          @click="submitCode"
          :disabled="isSubmitting"
        >
          <i class="fas" :class="isSubmitting ? 'fa-spinner fa-spin' : 'fa-check'"></i>
          {{ isSubmitting ? '提交中...' : '提交' }}
        </button>
      </div>
    </div>
    <div class="code-editor" ref="editorContainer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useCodeStore } from '@/stores/codeStore.js'
import { Submit, GetJudgeResult } from '@/api/ProblemApi.js'
import { useResultStore } from '@/stores/resultStroe.js'
import { useUserStore } from '@/stores/userStore.js'
import * as monaco from 'monaco-editor'

const emit = defineEmits(['code-change', 'language-change'])
const codeStore = useCodeStore()
const resultStore = useResultStore()
const userStore = useUserStore()

const selectedLanguage = ref('javascript')
const languageDisplay = ref('JavaScript')
const editorContainer = ref(null)
let editor = null

// 添加轮询相关的状态
const isPolling = ref(false)
let pollingTimer = null
const currentCommitId = ref(null)
const pollingStartTime = ref(null)
const MAX_POLLING_TIME = 60000 // 最大轮询时间60秒
const MAX_POLLING_COUNT = 30 // 最大轮询次数30次
const pollingCount = ref(0) // 当前轮询次数

const changeLanguage = () => {
  const languageMap = {
    javascript: 'JavaScript',
    python: 'Python',
    java: 'Java',
    csharp: 'C#',
    cpp: 'C++'
  }

  languageDisplay.value = languageMap[selectedLanguage.value]

  // 更新 store
  codeStore.setLanguage(selectedLanguage.value, languageDisplay.value)

  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel(), selectedLanguage.value)
    // 发送语言变更事件
    emit('language-change', {
      value: selectedLanguage.value,
      display: languageDisplay.value
    })
  }
}

const resetCode = () => {
  if (editor) {
    const defaultCode = getDefaultCode(selectedLanguage.value)
    editor.setValue(defaultCode)
    // 更新 store 中的代码
    codeStore.setCode(defaultCode)
  }
}

const props = defineProps({
  problemId: {
    type: String,
    required: true
  }
})

const isSubmitting = ref(false)
const submitCode = async () => {
  if (!editor || isSubmitting.value) return

  // 登录校验
  if (!userStore.isLogin || !userStore.user || !userStore.user.uuid || !userStore.token) {
    window.$message ? window.$message.error('请先登录后再提交') : alert('请先登录后再提交')
    return
  }

  isSubmitting.value = true
  resultStore.setStatus('running')

  try {
    // 准备提交数据
    const currentCode = editor.getValue()
    const requestData = {
      code: currentCode,
      userInput: '',  // 改为空字符串
      language: selectedLanguage.value,
      problemId: props.problemId,
      uuid: userStore.user.uuid
    }

    // 更新 store 中的代码
    codeStore.setCode(currentCode)

    // 调用提交 API
    const response = await Submit(requestData)

    // 检查响应中是否包含 commitId
    if (response && response.data && response.data.commitId) {
      currentCommitId.value = response.data.commitId
      // 开始轮询获取结果
      startPolling(response.data.commitId)

      // 发送事件，通知父组件开始轮询
      emit('code-change', {
        code: requestData.code,
        language: requestData.language,
        type: 'submit',
        commitId: response.data.commitId,
        status: 'polling'
      })
    } else {
      // 如果没有 commitId，直接显示响应结果
      emit('code-change', {
        code: requestData.code,
        language: requestData.language,
        type: 'submit',
        response: response
      })
    }

  } catch (error) {
    emit('code-change', {
      code: editor.getValue(),
      language: selectedLanguage.value,
      type: 'submit',
      response: {
        status: 'error',
        output: error.message || '提交失败'
      }
    })
  } finally {
    isSubmitting.value = false
  }
}

// 开始轮询函数
const startPolling = (commitId) => {
  if (isPolling.value) {
    stopPolling()
  }

  isPolling.value = true
  currentCommitId.value = commitId
  pollingStartTime.value = Date.now()
  pollingCount.value = 0 // 重置轮询次数

  // 立即执行一次查询
  pollResult(commitId)

  // 设置定时器，每2秒查询一次
  pollingTimer = setInterval(() => {
    pollResult(commitId)
  }, 2000)
}

// 停止轮询函数
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
  isPolling.value = false
  currentCommitId.value = null
  pollingStartTime.value = null
  pollingCount.value = 0 // 重置轮询次数
}

// 轮询获取结果
const pollResult = async (commitId) => {
  pollingCount.value++ // 增加轮询次数

  try {
    // 检查是否超时
    if (pollingStartTime.value && Date.now() - pollingStartTime.value > MAX_POLLING_TIME) {
      console.log('轮询超时，停止轮询')
      stopPolling()
      emit('code-change', {
        code: editor.getValue(),
        language: selectedLanguage.value,
        type: 'submit',
        response: {
          status: 'error',
          output: '判题超时，请稍后重试'
        },
        commitId: commitId,
        status: 'error'
      })
      return
    }

    // 检查是否达到最大轮询次数
    if (pollingCount.value >= MAX_POLLING_COUNT) {
      console.log('达到最大轮询次数，停止轮询')
      stopPolling()
      emit('code-change', {
        code: editor.getValue(),
        language: selectedLanguage.value,
        type: 'submit',
        response: {
          status: 'error',
          output: '判题超时，请稍后重试'
        },
        commitId: commitId,
        status: 'error'
      })
      return
    }

    const result = await GetJudgeResult(commitId)
    console.log('轮询结果:', result) // 添加调试日志

    // 检查结果状态
    if (result && result.data) {
      const status = result.data.status || result.data.message || ''
      const isProcessing = result.data.processing !== undefined ? result.data.processing : true

      console.log('判题状态:', status, '是否处理中:', isProcessing) // 添加调试日志

      if (!isProcessing) {
        console.log('判题完成，停止轮询')
        stopPolling()

        // 发送最终结果
        emit('code-change', {
          code: editor.getValue(),
          language: selectedLanguage.value,
          type: 'submit',
          response: result,
          commitId: commitId,
          status: 'completed'
        })
      } else {
        // 结果还在处理中，继续轮询
        console.log('判题进行中，继续轮询')
        emit('code-change', {
          code: editor.getValue(),
          language: selectedLanguage.value,
          type: 'submit',
          response: result,
          commitId: commitId,
          status: 'polling'
        })
      }
    } else {
      // 如果返回的数据格式不正确，检查是否应该停止轮询
      console.log('返回数据格式不正确:', result)

      // 如果连续多次返回空数据，可能是后端问题，停止轮询
      if (!result || !result.data) {
        console.log('返回空数据，停止轮询')
        stopPolling()
        emit('code-change', {
          code: editor.getValue(),
          language: selectedLanguage.value,
          type: 'submit',
          response: {
            status: 'error',
            output: '获取判题结果失败，请稍后重试'
          },
          commitId: commitId,
          status: 'error'
        })
      } else {
        // 继续轮询
        emit('code-change', {
          code: editor.getValue(),
          language: selectedLanguage.value,
          type: 'submit',
          response: result,
          commitId: commitId,
          status: 'polling'
        })
      }
    }
  } catch (error) {
    console.error('轮询获取结果失败:', error)

    // 如果是网络错误，继续轮询；如果是其他错误，停止轮询
    if (error.name === 'NetworkError' || error.message.includes('network')) {
      console.log('网络错误，继续轮询')
      emit('code-change', {
        code: editor.getValue(),
        language: selectedLanguage.value,
        type: 'submit',
        response: {
          status: 'polling',
          output: '网络连接中...'
        },
        commitId: commitId,
        status: 'polling'
      })
    } else {
      // 停止轮询并显示错误
      console.log('其他错误，停止轮询')
      stopPolling()
      emit('code-change', {
        code: editor.getValue(),
        language: selectedLanguage.value,
        type: 'submit',
        response: {
          status: 'error',
          output: '获取结果失败: ' + error.message
        },
        commitId: commitId,
        status: 'error'
      })
    }
  }
}

// 组件卸载时清理轮询
onUnmounted(() => {
  stopPolling()
})

onMounted(() => {
  // 直接用 monaco.editor.create 初始化
  editor = monaco.editor.create(editorContainer.value, {
    value: getDefaultCode(selectedLanguage.value),
    language: selectedLanguage.value,
    theme: 'vs',
    fontFamily: "'Fira Mono', 'JetBrains Mono', 'Consolas', 'Microsoft YaHei', 'PingFang SC', 'WenQuanYi Micro Hei', 'monospace'", // 支持中英文
    fontSize: 15,
    lineHeight: 26,
    letterSpacing: 0.5,
    lineNumbersMinChars: 3,
    padding: { top: 16, bottom: 16 },
    automaticLayout: true,
    scrollBeyondLastLine: false,
    minimap: { enabled: false },
    scrollbar: {
      verticalScrollbarSize: 10,
      horizontalScrollbarSize: 10,
      useShadows: false
    },
    renderLineHighlight: 'all',
    renderWhitespace: 'selection',
    roundedSelection: true,
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: true,
    tabSize: 4,
    insertSpaces: false,
    detectIndentation: false,
    autoIndent: 'none',
    glyphMargin: true,
    bracketPairColorization: {
      enabled: true,
      independentColorPoolPerBracketType: true
    },
    guides: {
      indentation: true,
      highlightActiveIndentation: true
    }
  })

  // 初始化 store 中的代码和语言
  const initialCode = getDefaultCode(selectedLanguage.value)
  codeStore.setCode(initialCode)
  codeStore.setLanguage(selectedLanguage.value, languageDisplay.value)

  // 监听代码变化
  editor.onDidChangeModelContent(() => {
    const currentCode = editor.getValue()
    // 更新 store 中的代码
    codeStore.setCode(currentCode)

    emit('code-change', {
      code: currentCode,
      language: selectedLanguage.value
    })
  })
})

// 获取默认代码的函数
const getDefaultCode = (lang) => {
  const defaultCodes = {
    javascript: 'function solution(input) {\n   // 在这里编写你的代码\n  \n}',
    python: 'def solution(input):\n    # 在这里编写你的代码\n    pass',
    java: 'public class Main {\n' +
      '    public static void main(String[] args) {\n' +
      '        // 在这里编写你的代码\n' +
      '        \n' +
      '    }\n' +
      '}',
    csharp: 'public class Solution {\n    public int[] SolutionMethod(int[] input) {\n        // 在这里编写你的代码\n        return null;\n    }\n}',
    cpp: 'class Solution {\npublic:\n    vector<int> solution(vector<int>& input) {\n        // 在这里编写你的代码\n        return {};\n    }\n};'
  }
  return defaultCodes[lang]
}

// 暴露给父组件的方法和属性
defineExpose({
  selectedLanguage,
  languageDisplay,
  getCurrentCode: () => editor?.getValue() || '',
  setLanguage: (lang) => {
    selectedLanguage.value = lang
    changeLanguage()
  }
})
</script>

<style scoped>
@import url('https://cdn.jsdelivr.net/npm/highlight.js@10.7.2/styles/github.min.css');

.code-panel {
  flex: 1;
  min-width: 300px;
  display: flex;
  flex-direction: column;
  background-color: white;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
}

.code-header {
  padding: 10px 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.language-selector {
  padding: 6px 10px;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  background-color: white;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  outline: none;
  transition: border-color 0.2s;
}

.language-selector:hover {
  border-color: #3498db;
}

.language-selector:focus {
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.1);
}

.code-actions {
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 12px;
  border-radius: 4px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.btn i {
  margin-right: 5px;
}

.btn-success {
  background-color: #2ecc71;
  color: white;
}

.btn-success:hover {
  background-color: #27ae60;
}

.btn-outline {
  background-color: transparent;
  border: 1px solid #e0e0e0;
  color: #333;
}

.btn-outline:hover {
  background-color: #f5f5f5;
}

.code-editor {
  flex: 1;
  min-height: 200px;
}


/* 添加禁用状态的样式 */
.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* 添加加载动画样式 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.fa-spinner {
  animation: spin 1s linear infinite;
}
</style>
