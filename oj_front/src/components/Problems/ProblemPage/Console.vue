<template>
  <div>
    <div id="console-container"
         ref="consoleContainer"
         :class="{ 'maximized': isMaximized, 'dragging': isDragging, 'resizing': isResizing }"
         :style="{
           left: position.left + 'px',
           top: position.top + 'px',
           width: size.width + 'px',
           height: size.height + 'px',
           display: isMinimized ? 'none' : 'flex'
         }">
      <div id="tab-bar" ref="tabBar">
        <div class="tab" :class="{ 'active': activeTab === 'result' }" @click="switchTab('result')">
          运行结果
          <span class="tab-count" v-show="resultCount > 0">{{ resultCount }}</span>
        </div>
        <div class="tab" :class="{ 'active': activeTab === 'input' }" @click="switchTab('input')">
          自测输入
          <span class="tab-count" v-show="inputCount > 0">{{ inputCount }}</span>
        </div>
        <div style="flex: 1;"></div>
        <div class="tab-action" @click="toggleMinimize">
          <i class="fas fa-minus"></i>
        </div>
        <div class="tab-action" @click="toggleMaximize">
          <i class="fas" :class="isMaximized ? 'fa-compress' : 'fa-expand'"></i>
        </div>
        <div class="tab run-tab" @click="executeCode">
          <i class="fas fa-play"></i> 自测运行
        </div>
      </div>
      <div id="display-area">
        <div class="tab-content" :class="{ 'active': activeTab === 'result' }" id="result-content">
          <div v-if="latestResult" class="execution-result">
            <pre class="result-text">{{ latestResult }}</pre>
          </div>
          <div v-else-if="submissionResultDisplay" class="submission-result">
            <div v-if="submissionResultDisplay.message">
              <span v-if="submissionResultDisplay.message === '提交成功'" style="color: #2ecc71; font-weight: bold; font-size: 16px;">提交成功</span>
              <span v-else style="font-weight: bold;">{{ submissionResultDisplay.message }}</span>
            </div>
            <pre class="result-text" style="white-space: pre-wrap; word-break: break-all;">
<span v-if="submissionResultDisplay.status !== undefined">状态: {{ submissionResultDisplay.status }}

</span><span v-if="submissionResultDisplay.output">输出:
  <span v-if="Array.isArray(submissionResultDisplay.output)">{{ JSON.stringify(submissionResultDisplay.output, null, 2) }}</span>
  <span v-else>{{ submissionResultDisplay.output }}</span>

</span><span v-if="submissionResultDisplay.error">错误: {{ submissionResultDisplay.error }}

</span><span v-if="submissionResultDisplay.time !== undefined">执行时间: {{
                submissionResultDisplay.time
              }}ms

</span><span v-if="submissionResultDisplay.memory !== undefined">内存使用: {{
                (submissionResultDisplay.memory / 1024 / 1024).toFixed(2)
              }}MB

</span>
            </pre>
            <div
              v-if="submissionResultDisplay.testCases && submissionResultDisplay.testCases.length > 0"
              class="test-cases">
              <h4>测试用例结果</h4>
              <div v-for="(testCase, index) in submissionResultDisplay.testCases" :key="index"
                   class="test-case">
                <div class="test-case-header">
                  <span>测试用例 #{{ index + 1 }}</span>
                  <span :class="['test-status', testCase.passed ? 'passed' : 'failed']">
                    {{ testCase.passed ? '通过' : '失败' }}
                  </span>
                </div>
                <div class="test-case-details">
                  <p>输入: {{ JSON.stringify(testCase.input) }}</p>
                  <p>输出: {{ testCase.output }}</p>
                  <p v-if="testCase.expected">预期: {{ testCase.expected }}</p>
                </div>
              </div>
            </div>
            </div>
          <div v-else class="welcome-message">
            <p>欢迎使用代码测试控制台！</p>
            <p>请点击"运行"按钮执行您的代码。</p>
          </div>
        </div>
        <div class="tab-content"
             :class="{ 'active': activeTab === 'input' }"
             id="input-content"
             contenteditable="true"
             @input="updateInputCount"
             :data-placeholder="'请在按题目描述输入测试数据！'">
        </div>
      </div>
      <div class="status-bar">
        <div class="status-item">
          <i class="fas fa-circle" :class="statusClass"></i>
          <span>{{ statusText }}</span>
        </div>

        <div class="status-item">
          <i class="fas fa-code"></i>
          <span>语言: {{ currentLanguage }}</span>
        </div>

        <div class="status-item">
          <i class="fas fa-clock"></i>
          <span>耗时: {{ timeElapsed }}ms</span>
        </div>
        <div class="status-item">
          <i class="fas fa-memory"></i>
          <span>内存: {{ memoryUsage }}MB</span>
        </div>

      </div>
      <div class="resize-handle right" @mousedown="startResize('right', $event)"></div>
      <div class="resize-handle bottom" @mousedown="startResize('bottom', $event)"></div>
      <div class="resize-handle corner" @mousedown="startResize('corner', $event)"></div>
    </div>
    <div id="mini-console"
         v-show="isMinimized"
         ref="miniConsole"
         @mousedown="startMiniConsoleDrag"
         @mouseup="handleMiniConsoleClick"
         :style="{
           left: miniConsolePosition.left + 'px',
           top: miniConsolePosition.top + 'px',
           display: isMinimized ? 'block' : 'none'
         }">
      <i class="fas fa-terminal"></i> 控制台
    </div>
  </div>
</template>

<script>
import {ref, onMounted, onUnmounted, computed, watch} from 'vue';
import {useCodeStore} from '@/stores/codeStore.js'
import {UserTest} from '@/api/ProblemApi.js'
import { useUserStore } from '@/stores/userStore.js'

const codeStore = useCodeStore()
const userStore = useUserStore()

export default {
  name: 'ProblemConsole',
  props: {
    initialTestInput: {
      type: String,
      default: ''
    },
    currentCode: {
      type: String,
      required: true
    },
    currentLanguage: {
      type: String,
      default: 'JavaScript'
    },
    problemId: {
      type: String,
      required: true
    },
    submissionResult: {
      type: Object,
      default: null
    }
  },

  setup(props) {
    // 状态
    const isMaximized = ref(false);
    const isMinimized = ref(false);
    const isDragging = ref(false);
    const isResizing = ref(false);
    const activeTab = ref('result');
    const statusText = ref('就绪');
    const statusClass = ref('success-icon');
    const timeElapsed = ref('0');
    const memoryUsage = ref('0');
    const resultCount = ref(0);
    const inputCount = ref(0);
    const latestResult = ref(null);
    const testInput = ref('');

    // 位置和大小
    const initialSize = {width: 500, height: 400};
    const size = ref({...initialSize});
    const lastSize = ref({...initialSize});

    // 计算初始位置在右下角
    const calculateInitialPosition = () => {
      return {
        left: Math.max(0, window.innerWidth - initialSize.width - 20), // 20px是右边距
        top: Math.max(0, window.innerHeight - initialSize.height - 20)  // 20px是底部边距
      };
    };

    const position = ref(calculateInitialPosition());
    const miniConsolePosition = ref(calculateInitialPosition());

    // DOM引用
    const consoleContainer = ref(null);
    const tabBar = ref(null);
    const miniConsole = ref(null);

    // 拖拽状态
    const dragState = ref({
      startX: 0,
      startY: 0,
      offsetX: 0,
      offsetY: 0,
      startWidth: 0,
      startHeight: 0
    });

    // 调整大小状态
    const resizeState = ref({
      direction: null,
      startX: 0,
      startY: 0,
      startWidth: 0,
      startHeight: 0
    });

    // 迷你控制台状态
    const miniConsoleDragState = ref({
      isDragging: false,
      hasDragged: false,
      offsetX: 0,
      offsetY: 0
    });

    // 方法
    const switchTab = (tab) => {
      if (activeTab.value === tab) return;
      activeTab.value = tab;
    };

    const toggleMinimize = () => {
      if (!isMinimized.value) {
        // 最小化时保存当前位置到迷你控制台位置
        miniConsolePosition.value = {
          left: position.value.left,
          top: position.value.top
        };
        isMinimized.value = true;
      }
    };

    const toggleMaximize = () => {
      if (isMinimized.value) return;
      isMaximized.value = !isMaximized.value;

      if (isMaximized.value) {
        lastSize.value = {...size.value};
        size.value = {
          width: window.innerWidth * 0.95,
          height: window.innerHeight * 0.9
        };
        position.value = {
          left: window.innerWidth * 0.025,
          top: window.innerHeight * 0.05
        };
      } else {
        size.value = {...lastSize.value};
      }
    };

    const startDrag = (e) => {
      if (isMaximized.value || e.target.closest('.tab-action') || e.target.closest('.run-tab')) return;

      const rect = consoleContainer.value.getBoundingClientRect();
      dragState.value = {
        startX: e.clientX,
        startY: e.clientY,
        offsetX: e.clientX - rect.left,
        offsetY: e.clientY - rect.top,
        startWidth: size.value.width,
        startHeight: size.value.height
      };
      isDragging.value = true;
      document.body.style.userSelect = 'none';
      document.body.style.cursor = 'grabbing';
    };

    const startResize = (direction, e) => {
      e.stopPropagation();
      if (isMaximized.value) return;

      resizeState.value = {
        direction,
        startX: e.clientX,
        startY: e.clientY,
        startWidth: size.value.width,
        startHeight: size.value.height
      };
      isResizing.value = true;
      document.body.style.userSelect = 'none';
    };

    const startMiniConsoleDrag = (e) => {
      if (e.button !== 0) return;

      const rect = miniConsole.value.getBoundingClientRect();
      miniConsoleDragState.value = {
        isDragging: true,
        hasDragged: false,
        offsetX: e.clientX - rect.left,
        offsetY: e.clientY - rect.top
      };
      e.preventDefault();
    };

    const handleMiniConsoleClick = () => {
      if (!miniConsoleDragState.value.hasDragged) {
        // 计算展开后的位置，确保不会超出屏幕
        const expandedWidth = lastSize.value.width;
        const expandedHeight = lastSize.value.height;

        // 计算新的左侧位置，确保不超出右边界
        let newLeft = miniConsolePosition.value.left;
        if (newLeft + expandedWidth > window.innerWidth) {
          newLeft = Math.max(0, window.innerWidth - expandedWidth);
        }

        // 计算新的顶部位置，确保不超出底部边界
        let newTop = miniConsolePosition.value.top;
        if (newTop + expandedHeight > window.innerHeight) {
          newTop = Math.max(0, window.innerHeight - expandedHeight);
        }

        // 更新位置并展开
        position.value = {
          left: newLeft,
          top: newTop
        };
        isMinimized.value = false;
      }
    };

    const handleMouseMove = (e) => {
      if (isDragging.value) {
        requestAnimationFrame(() => {
        const newX = e.clientX - dragState.value.offsetX;
        const newY = e.clientY - dragState.value.offsetY;

          // 使用当前实际大小而不是初始大小
          const currentWidth = size.value.width;
          const currentHeight = size.value.height;

        position.value = {
            left: Math.max(0, Math.min(newX, window.innerWidth - currentWidth)),
            top: Math.max(0, Math.min(newY, window.innerHeight - currentHeight))
        };
        });
      }

      if (isResizing.value) {
        requestAnimationFrame(() => {
        const dx = e.clientX - resizeState.value.startX;
        const dy = e.clientY - resizeState.value.startY;

          if (resizeState.value.direction === 'corner' || resizeState.value.direction === 'right') {
            const newWidth = Math.max(350, Math.min(
              resizeState.value.startWidth + dx,
              window.innerWidth - position.value.left
            ));
            size.value.width = newWidth;
            lastSize.value.width = newWidth;
        }

          if (resizeState.value.direction === 'corner' || resizeState.value.direction === 'bottom') {
            const newHeight = Math.max(250, Math.min(
              resizeState.value.startHeight + dy,
              window.innerHeight - position.value.top
            ));
            size.value.height = newHeight;
            lastSize.value.height = newHeight;
        }
        });
      }

      if (miniConsoleDragState.value.isDragging) {
        const dx = Math.abs(e.clientX - (miniConsoleDragState.value.offsetX + miniConsolePosition.value.left));
        const dy = Math.abs(e.clientY - (miniConsoleDragState.value.offsetY + miniConsolePosition.value.top));

        if (dx > 3 || dy > 3) {
        miniConsoleDragState.value.hasDragged = true;
        }

        requestAnimationFrame(() => {
        const newX = e.clientX - miniConsoleDragState.value.offsetX;
        const newY = e.clientY - miniConsoleDragState.value.offsetY;
        miniConsolePosition.value = {
          left: Math.max(0, Math.min(newX, window.innerWidth - 100)),
          top: Math.max(0, Math.min(newY, window.innerHeight - 40))
        };
        });
      }
    };

    const handleMouseUp = () => {
      if (isDragging.value) {
        isDragging.value = false;
        document.body.style.cursor = '';
        // 保存当前大小到lastSize
        lastSize.value = {...size.value};
      }

      if (isResizing.value) {
        isResizing.value = false;
        // 保存当前大小到lastSize
        lastSize.value = {...size.value};
      }

      if (miniConsoleDragState.value.isDragging) {
        miniConsoleDragState.value = {
          isDragging: false,
          hasDragged: false,
          offsetX: 0,
          offsetY: 0
        };
      }

      document.body.style.userSelect = '';
    };

        const updateInputCount = () => {
      const content = document.getElementById('input-content')?.textContent || '';
      inputCount.value = content.trim() !== '' ? 1 : 0;

      // 同步更新 testInput 的值，这样 executeCode 就能获取到最新的输入内容
      if (content.trim() !== '') {
        testInput.value = content;
      }
    };

    // 使用 computed 获取 store 中的值
    const currentCode = computed(() => codeStore.code)
    const currentLanguage = computed(() => codeStore.languageDisplay)
    const localSubmissionResult = computed(() => props.submissionResult)

    // 监听提交结果变化，清空自测结果
    watch(() => props.submissionResult, () => {
      latestResult.value = null
    })

    const executeCode = async () => {
      // 登录校验
      if (!userStore.isLogin || !userStore.user || !userStore.user.uuid || !userStore.token) {
        window.$message ? window.$message.error('请先登录后再自测') : alert('请先登录后再自测')
        return
      }

      statusText.value = '运行中...'
      statusClass.value = 'loading-icon'
      timeElapsed.value = '0'
      memoryUsage.value = '0'

      try {
        // 获取用户实际输入的内容
        const inputElement = document.getElementById('input-content')
        let inputContent = inputElement ? inputElement.textContent || inputElement.innerText : testInput.value

        // 如果输入内容为空，使用默认内容
        if (!inputContent || inputContent.trim() === '') {
          inputContent = testInput.value
        }

        // 如果输入内容是对象，转换为字符串
        if (typeof inputContent === 'object') {
          inputContent = JSON.stringify(inputContent, null, 2)
        }



        // 准备发送给后端的数据 - userInput应该是字符串
        const requestData = {
          code: currentCode.value || props.currentCode, // 优先使用store中的值，如果没有则使用props
          language: (currentLanguage.value || props.currentLanguage).toLowerCase(), // 转换为小写，如 "Java" -> "java"
          userInput: inputContent, // 直接发送字符串内容
          problemId: props.problemId,
          uuid: userStore.user.uuid
        }

        // 调试信息
        console.log('发送给后端的数据:', requestData)
        console.log('代码内容 (store):', currentCode.value)
        console.log('代码内容 (props):', props.currentCode)
        console.log('语言 (store):', currentLanguage.value)
        console.log('语言 (props):', props.currentLanguage)
        console.log('发送给后端的语言:', requestData.language)
        console.log('用户输入 (从DOM获取):', inputContent)
        console.log('testInput.value:', testInput.value)
        console.log('题目ID:', props.problemId)
        console.log('用户UUID:', userStore.user.uuid)

        // 调用测试 API
        const response = await UserTest(requestData)

        // 处理后端返回的数据
        const responseData = response.data || response
        const output = responseData.output || []
        const errorMessages = responseData.errorMessages || null
        const time = responseData.time || 0
        const memory = responseData.memory || 0
        const message = responseData.message || null
        const processing = responseData.processing || false

        // 构建结果显示
        let resultText = `输入数据：\n${inputContent}\n\n`

        if (errorMessages) {
          resultText += `错误信息：\n${errorMessages}\n\n`
        }

        if (output && output.length > 0) {
          resultText += `输出结果：\n${Array.isArray(output) ? output.join('\n') : output}\n\n`
        }

        if (message) {
          resultText += `消息：${message}\n\n`
        }

        resultText += `执行时间：${time}ms\n内存消耗：${(memory / 1024 / 1024).toFixed(2)}MB`

        // 更新自测结果
        latestResult.value = resultText

        resultCount.value = 1
        timeElapsed.value = time
        memoryUsage.value = (memory / 1024 / 1024).toFixed(2)

        // 根据是否有错误信息来设置状态
        if (errorMessages) {
          statusText.value = '执行错误'
          statusClass.value = 'error-icon'
        } else if (processing) {
          statusText.value = '处理中'
          statusClass.value = 'loading-icon'
        } else {
          statusText.value = '已完成'
          statusClass.value = 'success-icon'
        }

        activeTab.value = 'result'

      } catch (error) {
        // 处理错误
        let errorMessage = '执行失败'
        if (error.response && error.response.data) {
          // 如果是API错误响应，尝试获取详细信息
          const errorData = error.response.data
          if (errorData.errorMessages) {
            errorMessage = errorData.errorMessages
          } else if (errorData.message) {
            errorMessage = errorData.message
          } else if (error.message) {
            errorMessage = error.message
          }
        } else if (error.message) {
          errorMessage = error.message
        }

        latestResult.value = `执行错误：\n${errorMessage}\n\n输入数据：\n${typeof testInput.value === 'object' ? JSON.stringify(testInput.value, null, 2) : testInput.value}`

        resultCount.value = 1
        statusText.value = '执行错误'
        statusClass.value = 'error-icon'
        activeTab.value = 'result'
      }
    }

    const handleWindowResize = () => {
      if (isMaximized.value) {
        size.value = {
          width: window.innerWidth * 0.95,
          height: window.innerHeight * 0.9
        };
        position.value = {
          left: window.innerWidth * 0.025,
          top: window.innerHeight * 0.05
        };
      } else if (!isMinimized.value && !isDragging.value) {
        // 如果不是最大化状态，且不是最小化状态，也不在拖动中，
        // 则确保控制台始终在可视区域内
        position.value = {
          left: Math.min(position.value.left, window.innerWidth - size.value.width),
          top: Math.min(position.value.top, window.innerHeight - size.value.height)
        };
      }
    };

    // 生命周期钩子
    onMounted(() => {
      window.addEventListener('mousemove', handleMouseMove);
      window.addEventListener('mouseup', handleMouseUp);
      window.addEventListener('resize', handleWindowResize);

      if (tabBar.value) {
        tabBar.value.addEventListener('mousedown', startDrag);
      }
    });

    onUnmounted(() => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
      window.removeEventListener('resize', handleWindowResize);

      if (tabBar.value) {
        tabBar.value.removeEventListener('mousedown', startDrag);
      }
    });

    return {
      // 状态
      isMaximized,
      isMinimized,
      isDragging,
      isResizing,
      activeTab,
      statusText,
      statusClass,
      timeElapsed,
      memoryUsage,
      resultCount,
      inputCount,
      latestResult,
      testInput,
      size,
      position,
      miniConsolePosition,
      lastSize,

      // DOM引用
      consoleContainer,
      tabBar,
      miniConsole,

      // 方法
      switchTab,
      toggleMinimize,
      toggleMaximize,
      startResize,
      startMiniConsoleDrag,
      handleMiniConsoleClick,
      executeCode,
      updateInputCount,
      submissionResultDisplay: localSubmissionResult
    };
  }
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Roboto+Mono:wght@400;500&family=Roboto:wght@400;500&display=swap");
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css");
/* CSS 变量定义 */
:root {
  --primary-color: #4361ee;
  --primary-light: #e6ebff;
  --dark-color: #2b2d42;
  --light-color: #f8f9fa;
  --success-color: #4cc9f0;
  --error-color: #f72585;
  --warning-color: #f8961e;
  --border-radius: 6px;
  --shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

/* 控制台容器 */
#console-container {
  width: 500px;
  height: 400px;
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  position: absolute;
  left: 50px;
  top: 50px;
  resize: none;
  overflow: hidden;
  min-width: 350px;
  min-height: 250px;
  transform: translate3d(0, 0, 0);
  will-change: transform;
  transition: transform 0.16s ease-out;
  cursor: move;
  user-select: none; /* 只在容器级别禁用选择 */
  border: 1px solid rgba(0,0,0,0.08); /* 添加淡淡的黑色边框 */
}

#console-container.maximized {
  width: 95% !important;
  height: 90% !important;
  left: 2.5% !important;
  top: 2.5% !important;
  resize: none;
  transform: none !important;
}

#console-container.dragging {
  transition: none;
  will-change: transform;
}

#console-container.resizing {
  transition: none;
  will-change: width, height;
}

/* 标签栏 */
#tab-bar {
  display: flex;
  background-color: #f1f3f5;
  border-bottom: 1px solid #e9ecef;
  cursor: move;
  user-select: none;
}

.tab {
  padding: 10px 16px;
  text-align: center;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  color: #8a94a0;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
  position: relative;
}

.tab.active {
  color: #4a9eff;
  border-bottom-color: #4a9eff;
  background-color: white;
}

.tab:hover:not(.active) {
  background-color: rgba(0, 0, 0, 0.03);
  color: #6c757d;
}

.tab.run-tab {
  margin-left: auto;
  background-color: #3498db;
  color: white;
  border-radius: 0 var(--border-radius) 0 0;
  padding: 10px 24px;
}

.tab.run-tab:hover {
  background-color: #2980b9;
}

.tab-action {
  padding: 10px 12px;
  cursor: pointer;
  color: #6c757d;
  transition: all 0.2s ease;
}

.tab-action:hover {
  color: var(--primary-color);
  background-color: rgba(0, 0, 0, 0.05);
}

.tab-count {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: var(--primary-color);
  color: white;
  border-radius: 10px;
  font-size: 10px;
  padding: 2px 4px;
  min-width: 16px;
  text-align: center;
}

/* 显示区域 */
#display-area {
  flex: 1;
  padding: 0;
  overflow: hidden;
  background-color: white;
  position: relative;
  display: flex;
  flex-direction: column;
}

.tab-content {
  display: none;
  height: 100%;
  overflow: auto;
}

.tab-content.active {
  display: block;
  height: 100%;
}

/* 结果内容 */
#result-content {
  font-family: 'Roboto Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
}

.execution-result, .submission-result {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 24px;
  background-color: #f8f9fa;
  border-radius: 4px;
  box-sizing: border-box;
}

.result-text {
  flex: 1;
  font-family: 'Roboto Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  padding: 12px;
  margin: 0;
  background-color: #f8f9fa;
  border-radius: 4px;
  color: #2b2d42;
  line-height: 1.5;
  overflow: auto;
  user-select: text; /* 明确允许文本选择 */
  cursor: text; /* 使用文本光标 */
}

.welcome-message {
  color: #6c757d;
  padding: 20px;
  text-align: center;
}

/* 输入区域 */
#input-content {
  width: 100%;
  height: 100%;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Roboto Mono', monospace;
  padding: 12px;
  font-size: 14px;
  background-color: #f8f9fa;
  border-radius: 0 0 var(--border-radius) var(--border-radius);
  white-space: pre;
  user-select: text;
  cursor: text;
  caret-color: #4a9eff; /* 设置光标颜色 */
  caret-shape: bar; /* 设置光标形状为竖线 */
  position: relative;
}

/* 输入区域的 placeholder 样式 */
#input-content:empty::before {
  content: attr(data-placeholder);
  position: absolute;
  top: 12px;
  left: 12px;
  color: #999;
  font-style: italic;
  pointer-events: none;
  user-select: none;
}

/* 状态栏 */
.status-bar {
  display: flex;
  justify-content: space-between;
  padding: 6px 12px;
  font-size: 11px;
  background-color: #f1f3f5;
  border-top: 1px solid #e9ecef;
  color: #6c757d;
  position: relative;
  z-index: 1;
  user-select: none;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-item i {
  font-size: 10px;
}

.success-icon {
  color: var(--success-color);
}

.error-icon {
  color: var(--error-color);
}

.loading-icon {
  color: var(--warning-color);
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 调整大小手柄 */
.resize-handle {
  position: absolute;
  background-color: transparent;
  z-index: 10;
}

.resize-handle.right {
  width: 6px;
  right: 0;
  top: 0;
  bottom: 0;
  cursor: e-resize;
}

.resize-handle.bottom {
  height: 6px;
  bottom: 0;
  left: 0;
  right: 0;
  cursor: s-resize;
}

.resize-handle.corner {
  width: 14px;
  height: 14px;
  right: 0;
  bottom: 0;
  cursor: se-resize;
  background-color: transparent;
  z-index: 11;
}

/* 迷你控制台 */
#mini-console {
  position: fixed;
  background-color: rgb(67, 97, 238); /* 使用不透明的 RGB 颜色 */
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: move;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  font-size: 13px;
  user-select: none;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s;
  min-width: 100px;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  opacity: 1;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

#mini-console:hover {
  background-color: rgb(58, 86, 213); /* 使用不透明的 RGB 颜色 */
}

#mini-console i {
  font-size: 14px;
  opacity: 1;
}

/* 确保父元素不会影响透明度 */
#mini-console * {
  opacity: 1;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 移动端优化 */
@media (hover: none) {
  .resize-handle {
    width: 20px;
    height: 20px;
  }
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.result-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2b2d42;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.success {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.error {
  background-color: #f8d7da;
  color: #721c24;
}

.status-badge.running {
  background-color: #fff3cd;
  color: #856404;
}

.result-details {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.result-details p {
  margin: 0;
  font-size: 14px;
  color: #6c757d;
}

.test-cases {
  margin-top: 16px;
}

.test-cases h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #2b2d42;
}

.test-case {
  background-color: white;
  border: 1px solid #e9ecef;
  border-radius: 4px;
  padding: 12px;
  margin-bottom: 8px;
}

.test-case-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.test-status {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.test-status.passed {
  background-color: #d4edda;
  color: #155724;
}

.test-status.failed {
  background-color: #f8d7da;
  color: #721c24;
}

.test-case-details p {
  margin: 4px 0;
  font-size: 13px;
  color: #495057;
}

.error-message {
  margin-top: 16px;
  padding: 12px;
  background-color: #f8d7da;
  border-radius: 4px;
}

.error-message h4 {
  margin: 0 0 8px 0;
  color: #721c24;
  font-size: 14px;
}

.error-message p {
  margin: 0;
  color: #721c24;
  font-size: 13px;
}
</style>

