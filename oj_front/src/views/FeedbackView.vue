<script setup>
import { ref, computed } from 'vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { SendFeedback } from '@/api/UserApi.js'

const feedback = ref({
  type: 'bug',
  title: '',
  content: '',
  contact: ''
})

const previewContent = ref('')

const loading = ref(false)
const error = ref(null)

// 更新预览内容
const updatePreview = () => {
  const rawMarkdown = feedback.value.content
  const htmlContent = marked(rawMarkdown)
  previewContent.value = DOMPurify.sanitize(htmlContent)
}

// 上传图片到服务器，返回图片URL
async function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);
  const res = await fetch('/api/upload', {
    method: 'POST',
    body: formData
  });
  const data = await res.json();
  return data.url;
}

// 拖拽图片
const handleDrop = async (e) => {
  e.preventDefault();
  const files = e.dataTransfer.files;
  if (files && files[0] && files[0].type.startsWith('image/')) {
    const url = await uploadImage(files[0]);
    insertImageMarkdown(url);
  }
}

// 粘贴图片
const handlePaste = async (e) => {
  const items = e.clipboardData.items;
  for (let item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile();
      const url = await uploadImage(file);
      insertImageMarkdown(url);
      e.preventDefault();
      break;
    }
  }
}

// 在光标处插入图片 markdown
function insertImageMarkdown(url) {
  const textarea = document.querySelector('.markdown-editor');
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const text = feedback.value.content;
  const insert = `![图片描述](${url})`;
  feedback.value.content = text.substring(0, start) + insert + text.substring(end);
  updatePreview();
}

// 插入 Markdown 语法
const insertMarkdown = (type) => {
  const textarea = document.querySelector('.markdown-editor')
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const text = feedback.value.content
  let insert = ''

  switch (type) {
    case 'bold':
      insert = `**${text.substring(start, end) || '加粗文本'}**`
      break
    case 'italic':
      insert = `*${text.substring(start, end) || '斜体文本'}*`
      break
    case 'code':
      insert = `\`${text.substring(start, end) || '代码'}\``
      break
    case 'link':
      insert = `[${text.substring(start, end) || '链接文本'}](url)`
      break
    case 'image':
      insert = `![图片描述](图片URL)`
      break
    case 'list':
      insert = `- ${text.substring(start, end) || '列表项'}`
      break
    case 'quote':
      insert = `> ${text.substring(start, end) || '引用文本'}`
      break
  }

  feedback.value.content = text.substring(0, start) + insert + text.substring(end)
  updatePreview()
}

// 表单验证
const isValid = computed(() => {
  return feedback.value.title.trim() !== '' &&
         feedback.value.content.trim() !== ''
})

// 提交反馈
const submitFeedback = async () => {
  if (!isValid.value) return
  loading.value = true
  error.value = null
  try {
    await SendFeedback({
      type: feedback.value.type,
      title: feedback.value.title,
      content: feedback.value.content,
      contact: feedback.value.contact
    })
    alert('感谢您的反馈！')
    feedback.value = {
      type: 'bug',
      title: '',
      content: '',
      contact: ''
    }
    updatePreview()
  } catch (err) {
    console.error('提交失败:', err)
    error.value = '提交失败，请稍后重试'
    alert(error.value)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="feedback-container">
    <div class="feedback-header">
      <h1>反馈建议</h1>
      <p class="subtitle">您的反馈对我们很重要，帮助我们改进平台</p>
    </div>

    <div class="feedback-form">
      <div class="form-group">
        <label>反馈类型</label>
        <select v-model="feedback.type" class="form-select">
          <option value="bug">问题报告</option>
          <option value="feature">功能建议</option>
          <option value="improvement">改进建议</option>
          <option value="other">其他</option>
        </select>
      </div>

      <div class="form-group">
        <label>标题</label>
        <input
          type="text"
          v-model="feedback.title"
          placeholder="请简要描述您的反馈"
          class="form-input"
        >
      </div>

      <div class="form-group">
        <label>详细描述</label>
        <div class="editor-container">
          <div class="editor-toolbar">
            <button @click="insertMarkdown('bold')" title="加粗">B</button>
            <button @click="insertMarkdown('italic')" title="斜体">I</button>
            <button @click="insertMarkdown('code')" title="代码">`</button>
            <button @click="insertMarkdown('link')" title="链接">🔗</button>
            <button @click="insertMarkdown('image')" title="图片">🖼️</button>
            <button @click="insertMarkdown('list')" title="列表">•</button>
            <button @click="insertMarkdown('quote')" title="引用">❝</button>
          </div>
          <textarea
            v-model="feedback.content"
            class="markdown-editor"
            placeholder="请详细描述您的反馈，支持 Markdown 格式"
            @input="updatePreview"
            @drop="handleDrop"
            @paste="handlePaste"
          ></textarea>
        </div>
      </div>

      <div class="form-group">
        <label>预览</label>
        <div class="preview-container markdown-body" v-html="previewContent"></div>
      </div>

      <div class="form-group">
        <label>联系方式（选填）</label>
        <input
          type="text"
          v-model="feedback.contact"
          placeholder="邮箱或其他联系方式"
          class="form-input"
        >
      </div>

      <div class="form-actions">
        <button
          class="submit-btn"
          @click="submitFeedback"
          :disabled="!isValid"
        >
          提交反馈
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feedback-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.feedback-header {
  text-align: center;
  margin-bottom: 40px;
}

.feedback-header h1 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  color: #666;
  font-size: 16px;
}

.feedback-form {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #2c3e50;
}

.form-input,
.form-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus {
  border-color: #3498db;
  outline: none;
}

.editor-container {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  padding: 8px;
  background: #f8f9fa;
  border-bottom: 1px solid #ddd;
  display: flex;
  gap: 8px;
}

.editor-toolbar button {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.editor-toolbar button:hover {
  background: #f0f0f0;
  border-color: #3498db;
  color: #3498db;
}

.markdown-editor {
  width: 100%;
  height: 200px;
  padding: 12px;
  border: none;
  resize: vertical;
  font-family: monospace;
  font-size: 14px;
  line-height: 1.5;
}

.markdown-editor:focus {
  outline: none;
}

.preview-container {
  min-height: 100px;
  max-height: 300px;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow-y: auto;
  background: #f8f9fa;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
}

.submit-btn {
  padding: 12px 30px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #2980b9;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* Markdown 样式 */
.markdown-body {
  font-size: 14px;
  line-height: 1.6;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-body code {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27,31,35,0.05);
  border-radius: 3px;
}

.markdown-body pre {
  padding: 16px;
  overflow: auto;
  font-size: 85%;
  line-height: 1.45;
  background-color: #f6f8fa;
  border-radius: 3px;
}

.markdown-body blockquote {
  padding: 0 1em;
  color: #6a737d;
  border-left: 0.25em solid #dfe2e5;
  margin: 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 2em;
}

.markdown-body img {
  max-width: 100%;
  box-sizing: content-box;
}
</style>
