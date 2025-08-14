<template>
  <div class="result-container">
    <div class="result-card">
      <h2>提交结果</h2>

      <!-- 轮询状态显示 -->
      <div v-if="isPolling" class="polling-status">
        <div class="loading-spinner"></div>
        <span class="polling-text">正在判题中，请稍候...</span>
        <div class="polling-info" v-if="finalResult && finalResult.data">
          <span v-if="finalResult.data.message">状态: {{ finalResult.data.message }}</span>
          <span v-if="finalResult.data.processing !== undefined">
            处理状态: {{ finalResult.data.processing ? '处理中' : '已完成' }}
          </span>
        </div>
      </div>

      <!-- 提示信息 -->
      <div v-if="tipToShow || showEncourage" class="top-tip-message" :class="detailMessageClass">
        <span v-if="tipToShow">{{ tipToShow }}</span>
        <span v-else-if="showEncourage">{{ encourageTip }}</span>
      </div>

      <!-- 判题结果 -->
      <div v-if="finalResult && finalResult.data">
        <div class="result-row" v-if="finalResult.data.time !== undefined">
          <span class="label">执行时间：</span>
          <span>{{ finalResult.data.time }} ms</span>
        </div>
        <div class="result-row" v-if="finalResult.data.memory !== undefined">
          <span class="label">内存：</span>
          <span>{{ (finalResult.data.memory / 1024 / 1024).toFixed(2) }} MB</span>
        </div>
        <div class="result-row" v-if="finalResult.data.message">
          <span class="label">详细信息：</span>
          <span class="detail-message" :class="detailMessageClass" :style="detailMessageStyle">{{ finalResult.data.message }}</span>
        </div>
        <div class="result-row" v-if="finalResult.data.output !== undefined && finalResult.data.output !== null">
          <span class="label">输出：</span>
          <div class="output">
            <template v-if="Array.isArray(finalResult.data.output)">
              <div v-for="(item, idx) in finalResult.data.output" :key="idx" class="output-line">
                <!-- 布尔数组的特殊处理 -->
                <template v-if="isBooleanArray(item)">
                  <div class="boolean-array-container">
                    <div class="boolean-array-title">测试案例组 {{ idx + 1 }}:</div>
                    <div class="test-cases-list">
                      <div
                        v-for="(testResult, testIdx) in parseBooleanArray(item)"
                        :key="testIdx"
                        class="test-case-item"
                      >
                        <span class="test-case-number">案例 {{ testIdx + 1 }}:</span>
                        <span
                          class="test-case-status"
                          :class="testResult ? 'test-case-pass' : 'test-case-fail'"
                        >
                          {{ testResult ? '通过' : '失败' }}
                        </span>
                        <span class="test-case-icon">
                          {{ testResult ? '✓' : '✗' }}
                        </span>
                      </div>
                    </div>
                    <div class="test-summary">
                      <span class="pass-count">通过: {{ getPassCount(parseBooleanArray(item)) }}</span>
                      <span class="fail-count">失败: {{ getFailCount(parseBooleanArray(item)) }}</span>
                    </div>
                  </div>
                </template>
                <!-- 其他类型的数组项 -->
                <template v-else>
                  <span v-if="typeof item === 'object'">{{ JSON.stringify(item, null, 2) }}</span>
                  <span v-else>{{ item }}</span>
                </template>
              </div>
            </template>
            <template v-else-if="typeof finalResult.data.output === 'object'">
              <div v-for="(val, key) in finalResult.data.output" :key="key" class="output-line">
                <strong>{{ key }}:</strong> <span>{{ val }}</span>
              </div>
            </template>
            <template v-else>
              <span>{{ finalResult.data.output }}</span>
            </template>
          </div>
        </div>
        <div class="result-row error-row" v-if="finalResult.data.errorMessages">
          <span class="label">错误信息：</span>
          <pre class="error-message">{{ finalResult.data.errorMessages }}</pre>
        </div>
      </div>
      <div v-else-if="!isPolling">
        <p>暂无结果</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { computed } from 'vue'

const props = defineProps({
  result: Object,
  isPolling: {
    type: Boolean,
    default: false
  }
})

const route = useRoute()

// 使用 computed 来确保 finalResult 是响应式的
const finalResult = computed(() => {
  if (props.result) {
    return props.result
  }

  if (route.params.result) {
    try {
      return JSON.parse(route.params.result)
    } catch {
      return null
    }
  }

  return null
})

const checkTips = [
  '出现非零异常，建议多检查下输入输出格式和边界情况。',
  '非零异常常见于格式错误或未考虑特殊情况，建议再检查下代码。',
  '遇到非零异常，建议多打印调试信息定位问题。',
]

const errorTips = [
  '别忘了考虑所有边界情况哦！',
  '再认真读一遍题目描述，或许会有新发现。',
  '调试时可以多打印一些中间变量，帮助定位问题。',
  '有时候换个思路，问题就迎刃而解了！',
  '别灰心，失败是成功之母！',
]

const encourageTips = [
  '太棒了，继续保持！',
  '优秀，离ACM大神又近了一步！',
  '做得好，继续挑战更难的题目吧！',
]

const tipToShow = computed(() => {
  if (finalResult.value && finalResult.value.data) {
    const msg = finalResult.value.data.errorMessages || '';
    const detail = finalResult.value.data.message || '';

    // 非零异常
    if (msg.includes('非零异常')) {
      return checkTips[Math.floor(Math.random() * checkTips.length)];
    }

    // 超时相关
    if (
      msg.includes('超时') || msg.includes('Time Limit Exceeded') || msg.includes('TLE') ||
      detail === '超时' || detail === 'Time Limit Exceeded' || detail === 'TLE'
    ) {
      return '算法时间复杂度过高，建议优化算法或检查是否有死循环。';
    }

    // 内存溢出相关
    if (
      msg.includes('内存溢出') || msg.includes('Memory Limit Exceeded') || msg.includes('MLE') ||
      detail === '内存溢出' || detail === 'Memory Limit Exceeded' || detail === 'MLE'
    ) {
      return '内存使用过多，建议优化数据结构或减少不必要的变量。';
    }

    // 其他错误状态（包括答案错误）
    if (
      msg.includes('错误') || msg.includes('Error') || msg.includes('失败') || msg.includes('Fail') ||
      detail.includes('错误') || detail.includes('Error') || detail.includes('失败') || detail.includes('Fail')
    ) {
      return errorTips[Math.floor(Math.random() * errorTips.length)];
    }
  }
  return '';
})
const showEncourage = computed(() => {
  return finalResult.value && finalResult.value.status === true && finalResult.value.data && finalResult.value.data.message === '通过' && (!finalResult.value.data.errorMessages)
})
const encourageTip = computed(() => {
  if (showEncourage.value) {
    return encourageTips[Math.floor(Math.random() * encourageTips.length)]
  }
  return ''
})

const detailMessageClass = computed(() => {
  if (finalResult.value && finalResult.value.data && finalResult.value.data.message) {
    const message = finalResult.value.data.message;

    // 调试信息
    console.log('Message:', message, 'Type:', typeof message, 'Length:', message.length);
    console.log('Message === "通过":', message === '通过');
    console.log('Message === "Accepted":', message === 'Accepted');
    console.log('Message === "AC":', message === 'AC');

    // 成功状态
    if (message === '通过' || message === 'Accepted' || message === 'AC') {
      console.log('Returning msg-success');
      return 'msg-success';
    }

    // 答案错误
    if (message === '答案错误' || message === 'Wrong Answer' || message === 'WA') {
      console.log('Returning msg-error');
      return 'msg-error';
    }

    // 超时
    if (message === '超时' || message === 'Time Limit Exceeded' || message === 'TLE') {
      console.log('Returning msg-timeout');
      return 'msg-timeout';
    }

    // 内存溢出
    if (message === '内存溢出' || message === 'Memory Limit Exceeded' || message === 'MLE') {
      console.log('Returning msg-memory');
      return 'msg-memory';
    }

    // 其他错误
    if (message.includes('错误') || message.includes('Error') || message.includes('失败') || message.includes('Fail')) {
      console.log('Returning msg-error');
      return 'msg-error';
    }

    // 默认警告色
    console.log('Returning msg-warning (default)');
    return 'msg-warning';
  }
  console.log('No message, returning empty string');
  return '';
})

// 添加内联样式计算属性，直接设置颜色
const detailMessageStyle = computed(() => {
  if (finalResult.value && finalResult.value.data && finalResult.value.data.message) {
    const message = finalResult.value.data.message;

    // 调试信息
    console.log('detailMessageStyle - Message:', message, 'Type:', typeof message, 'Length:', message.length);
    console.log('detailMessageStyle - Message === "通过":', message === '通过');

    // 成功状态
    if (message === '通过' || message === 'Accepted' || message === 'AC') {
      console.log('detailMessageStyle - Returning success color');
      return { color: '#2ecc71' };
    }

    // 答案错误
    if (message === '答案错误' || message === 'Wrong Answer' || message === 'WA') {
      console.log('detailMessageStyle - Returning error color');
      return { color: '#e74c3c' };
    }

    // 超时
    if (message === '超时' || message === 'Time Limit Exceeded' || message === 'TLE') {
      console.log('detailMessageStyle - Returning timeout color');
      return { color: '#e67e22' };
    }

    // 内存溢出
    if (message === '内存溢出' || message === 'Memory Limit Exceeded' || message === 'MLE') {
      console.log('detailMessageStyle - Returning memory color');
      return { color: '#9b59b6' };
    }

    // 其他错误
    if (message.includes('错误') || message.includes('Error') || message.includes('失败') || message.includes('Fail')) {
      console.log('detailMessageStyle - Returning error color');
      return { color: '#e74c3c' };
    }

    // 默认警告色
    console.log('detailMessageStyle - Returning warning color (default)');
    return { color: '#f39c12' };
  }
  console.log('detailMessageStyle - No message, returning empty object');
  return {};
})

// Helper functions for boolean array display
const isBooleanArray = (item) => {
  // 检查是否为字符串形式的布尔数组
  if (typeof item === 'string' && item.startsWith('[') && item.endsWith(']')) {
    try {
      const parsed = JSON.parse(item);
      return Array.isArray(parsed) && parsed.length > 0 &&
             (typeof parsed[0] === 'boolean' || parsed.every(val => val === 'true' || val === 'false'));
    } catch {
      return false;
    }
  }
  // 检查是否为直接的布尔数组
  return Array.isArray(item) && item.length > 0 && typeof item[0] === 'boolean';
};

const parseBooleanArray = (item) => {
  // 如果是字符串，先解析
  if (typeof item === 'string') {
    try {
      const parsed = JSON.parse(item);
      return parsed.map(val => val === true || val === 'true');
    } catch {
      return [];
    }
  }
  // 如果已经是数组，直接返回
  return item.map(val => val === true || val === 'true');
};

const getPassCount = (results) => {
  return results.filter(result => result).length;
};

const getFailCount = (results) => {
  return results.filter(result => !result).length;
};
</script>

<style scoped>
.result-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.08);
  padding: 40px 32px;
  min-width: 350px;
  text-align: center;
  position: relative;
}
.top-tip-message {
  margin-top: 12px;
  margin-bottom: 18px;
  font-size: 18px;
  font-weight: bold;
  color: #f39c12;
  text-align: center;
  min-height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.top-tip-message .encourage-message {
  color: #2ecc71;
}
.result-row {
  margin: 10px 0;
  text-align: left;
}
.label {
  font-weight: bold;
  margin-right: 8px;
}
.output {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 8px;
  margin: 0;
  font-family: 'Roboto Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  text-align: left;
}
.output-line {
  margin-bottom: 2px;
}
.error-row {
  margin-top: 16px;
}
.error-message {
  color: #e74c3c;
  background: #fff0f0;
  border-radius: 4px;
  padding: 8px;
  margin: 0;
  font-family: 'Roboto Mono', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
}
/* 基础样式 */
.detail-message {
  font-weight: 600;
}

/* 轮询状态样式 */
.polling-status {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
  margin-bottom: 20px;
  color: #555;
  font-size: 16px;
}

.polling-text {
  font-weight: 500;
}

.polling-info {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.polling-info span {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Roboto Mono', monospace;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* New styles for boolean array display */
.boolean-array-container {
  margin-top: 8px;
  margin-bottom: 8px;
  padding: 8px;
  background-color: #f0f0f0;
  border-radius: 4px;
  border: 1px solid #ccc;
}

.boolean-array-title {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.boolean-array {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.boolean-item {
  font-size: 16px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

.boolean-true {
  background-color: #d4edda; /* Light green */
  color: #155724; /* Dark green */
}

.boolean-false {
  background-color: #f8d7da; /* Light red */
  color: #721c24; /* Dark red */
}

.boolean-summary {
  font-size: 13px;
  color: #555;
  display: flex;
  justify-content: space-between;
  padding-top: 4px;
}

/* New styles for test case result display */
.test-case-result {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: bold;
}

.test-case-status {
  padding: 4px 8px;
  border-radius: 6px;
  font-family: 'Roboto Mono', monospace;
}

.test-case-pass {
  background-color: #d4edda; /* Light green */
  color: #155724; /* Dark green */
}

.test-case-fail {
  background-color: #f8d7da; /* Light red */
  color: #721c24; /* Dark red */
}

.test-case-detail {
  font-size: 12px;
  color: #555;
}

/* New styles for test cases list */
.test-cases-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
}

.test-case-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: bold;
}

.test-case-number {
  font-weight: bold;
  color: #555;
}

.test-case-icon {
  font-size: 18px;
  font-weight: bold;
}

.test-summary {
  font-size: 13px;
  color: #555;
  display: flex;
  justify-content: space-between;
  padding-top: 4px;
}

.pass-count {
  color: #2ecc71;
}

.fail-count {
  color: #e74c3c;
}
</style>
