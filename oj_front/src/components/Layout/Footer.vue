<template>
  <footer class="custom-footer" v-if="!isLoginPage">
    <div class="footer-container">
      <div class="footer-content">
        <div class="copyright">
          © 2025 编程练习平台 版权所有 | 备案号: xxxx
        </div>
        <div class="footer-links">
          <router-link to="/privacy">myGitHub</router-link>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
  computed: {
    isLoginPage() {
      return this.$route.path === '/login-or-register'
    }
  }
}
</script>

<style scoped>
.custom-footer {
  background-color: #e8e8e8;
  color: #333;
  padding: 15px 5%;
  font-size: 14px;
  border-top: 1px solid #ddd;
  border-bottom: 1px solid #000;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.copyright {
  color: #555;
}

.footer-links {
  display: flex;
  gap: 20px;
}

.footer-links a {
  color: #555;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #09bb07;
}

@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style>
