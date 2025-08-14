<script setup>
import { ref, onMounted } from 'vue'
import { formatDistanceToNow } from 'date-fns'
import { zhCN } from 'date-fns/locale'

// 状态
const posts = ref([])
const selectedPost = ref(null)
const showCreatePost = ref(false)
const showCommentInput = ref(false)
const newComment = ref('')
const newPost = ref({
  title: '',
  tag: 'discussion',
  content: ''
})

// 格式化时间
const formatTime = (time) => {
  return formatDistanceToNow(new Date(time), { addSuffix: true, locale: zhCN })
}

// 选择帖子
const selectPost = (post) => {
  selectedPost.value = post
}

// 点赞帖子
const likePost = () => {
  if (selectedPost.value) {
    selectedPost.value.likes++
  }
}

// 提交评论
const submitComment = () => {
  if (!newComment.value.trim()) return

  const comment = {
    id: Date.now(),
    content: newComment.value,
    author: {
      name: '当前用户',
      avatar: '/default-avatar.png'
    },
    createTime: new Date().toISOString(),
    likes: 0
  }

  selectedPost.value.commentList.push(comment)
  selectedPost.value.comments++
  newComment.value = ''
  showCommentInput.value = false
}

// 回复评论
const replyToComment = (comment) => {
  newComment.value = `@${comment.author.name} `
  showCommentInput.value = true
}

// 点赞评论
const likeComment = (comment) => {
  comment.likes++
}

// 提交新帖子
const submitPost = () => {
  if (!newPost.value.title.trim() || !newPost.value.content.trim()) return

  const post = {
    id: Date.now(),
    ...newPost.value,
    author: {
      name: '当前用户',
      avatar: '/default-avatar.png'
    },
    createTime: new Date().toISOString(),
    views: 0,
    comments: 0,
    likes: 0,
    commentList: []
  }

  posts.value.unshift(post)
  showCreatePost.value = false
  newPost.value = {
    title: '',
    tag: 'discussion',
    content: ''
  }
}

// 模拟获取数据
onMounted(() => {
  // 这里应该调用API获取数据
  posts.value = [
    {
      id: 1,
      title: '如何优化动态规划的时间复杂度？',
      content: '最近在做一道动态规划的题目，但是时间复杂度总是超时，有没有什么好的优化方法？',
      tag: 'question',
      author: {
        name: '张三',
        avatar: '/default-avatar.png'
      },
      createTime: '2024-03-15T10:00:00Z',
      views: 123,
      comments: 5,
      likes: 8,
      commentList: [
        {
          id: 1,
          content: '可以尝试使用滚动数组优化空间复杂度',
          author: {
            name: '李四',
            avatar: '/default-avatar.png'
          },
          createTime: '2024-03-15T10:30:00Z',
          likes: 3
        }
      ]
    }
  ]
})
</script>

<template>
  <div class="discussion-view">
    <!-- 讨论列表 -->
    <div class="discussion-list" v-if="!selectedPost">
      <div class="discussion-header">
        <h1>讨论区</h1>
        <button class="create-post-btn" @click="showCreatePost = true">
          <i class="fas fa-plus"></i> 发布讨论
        </button>
      </div>

      <!-- 讨论列表内容 -->
      <div class="posts-container">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="selectPost(post)">
          <div class="post-header">
            <h3 class="post-title">{{ post.title }}</h3>
            <span class="post-tag" :class="post.tag">{{ post.tag }}</span>
          </div>
          <p class="post-preview">{{ post.content }}</p>
          <div class="post-meta">
            <span class="author">
              <img :src="post.author.avatar" :alt="post.author.name" class="avatar">
              {{ post.author.name }}
            </span>
            <span class="time">{{ formatTime(post.createTime) }}</span>
            <span class="stats">
              <i class="fas fa-eye"></i> {{ post.views }}
              <i class="fas fa-comment"></i> {{ post.comments }}
              <i class="fas fa-thumbs-up"></i> {{ post.likes }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 讨论详情 -->
    <div class="discussion-detail" v-else>
      <div class="detail-header">
        <button class="back-btn" @click="selectedPost = null">
          <i class="fas fa-arrow-left"></i> 返回列表
        </button>
        <h1>{{ selectedPost.title }}</h1>
      </div>

      <div class="post-content">
        <div class="post-info">
          <div class="author-info">
            <img :src="selectedPost.author.avatar" :alt="selectedPost.author.name" class="avatar">
            <div class="author-details">
              <span class="author-name">{{ selectedPost.author.name }}</span>
              <span class="post-time">{{ formatTime(selectedPost.createTime) }}</span>
            </div>
          </div>
          <div class="post-actions">
            <button class="action-btn" @click="likePost">
              <i class="fas fa-thumbs-up"></i> {{ selectedPost.likes }}
            </button>
            <button class="action-btn" @click="showCommentInput = true">
              <i class="fas fa-comment"></i> 评论
            </button>
          </div>
        </div>

        <div class="content-body" v-html="selectedPost.content"></div>

        <!-- 评论区 -->
        <div class="comments-section">
          <h3>评论 ({{ selectedPost.comments }})</h3>

          <!-- 评论输入框 -->
          <div class="comment-input" v-if="showCommentInput">
            <textarea
              v-model="newComment"
              placeholder="写下你的评论..."
              rows="3"
            ></textarea>
            <div class="comment-actions">
              <button class="cancel-btn" @click="showCommentInput = false">取消</button>
              <button class="submit-btn" @click="submitComment">发表评论</button>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list">
            <div v-for="comment in selectedPost.commentList" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <img :src="comment.author.avatar" :alt="comment.author.name" class="avatar">
                <div class="comment-info">
                  <span class="author-name">{{ comment.author.name }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-actions">
                <button class="reply-btn" @click="replyToComment(comment)">回复</button>
                <button class="like-btn" @click="likeComment(comment)">
                  <i class="fas fa-thumbs-up"></i> {{ comment.likes }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发布讨论弹窗 -->
    <div class="modal" v-if="showCreatePost">
      <div class="modal-content">
        <div class="modal-header">
          <h2>发布讨论</h2>
          <button class="close-btn" @click="showCreatePost = false">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>标题</label>
            <input type="text" v-model="newPost.title" placeholder="请输入标题">
          </div>
          <div class="form-group">
            <label>分类</label>
            <select v-model="newPost.tag">
              <option value="question">问题</option>
              <option value="share">分享</option>
              <option value="discussion">讨论</option>
            </select>
          </div>
          <div class="form-group">
            <label>内容</label>
            <textarea
              v-model="newPost.content"
              placeholder="请输入内容..."
              rows="6"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-btn" @click="showCreatePost = false">取消</button>
          <button class="submit-btn" @click="submitPost">发布</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discussion-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.discussion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.create-post-btn {
  padding: 8px 16px;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-post-btn:hover {
  background-color: #2980b9;
}

.posts-container {
  display: grid;
  gap: 20px;
}

.post-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.post-title {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.post-tag {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-tag.question {
  background-color: #e3f2fd;
  color: #1976d2;
}

.post-tag.share {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.post-tag.discussion {
  background-color: #fff3e0;
  color: #f57c00;
}

.post-preview {
  color: #666;
  margin: 10px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #999;
  font-size: 14px;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.stats {
  display: flex;
  gap: 15px;
}

.stats i {
  margin-right: 4px;
}

/* 详情页样式 */
.discussion-detail {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.detail-header {
  margin-bottom: 20px;
}

.back-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 15px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-btn:hover {
  background-color: #f5f5f5;
}

.post-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: 600;
  color: #2c3e50;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 6px 12px;
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.action-btn:hover {
  background: #e9ecef;
}

.content-body {
  line-height: 1.6;
  color: #2c3e50;
  margin: 20px 0;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 评论区样式 */
.comments-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.comment-input {
  margin: 20px 0;
}

.comment-input textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  margin-bottom: 10px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn {
  padding: 8px 16px;
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.submit-btn {
  padding: 8px 16px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  padding: 15px;
  margin: 10px 0;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.comment-item:hover {
  background-color: #f1f3f5;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.comment-info {
  display: flex;
  flex-direction: column;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  margin: 8px 0;
  color: #2c3e50;
}

.comment-item .comment-actions {
  display: flex;
  gap: 15px;
}

.reply-btn, .like-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s;
}

.reply-btn:hover, .like-btn:hover {
  background-color: #e9ecef;
  color: #3498db;
}

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #2c3e50;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
