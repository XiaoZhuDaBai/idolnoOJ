<template>
  <div class="wechat-login-container">
    <h3>微信登录</h3>
    <div id="qrCode" ref="qrCodeRef" class="qr-code">
      <!-- 二维码将在这里显示 -->
    </div>
    <div class="instructions">请使用微信扫描二维码登录</div>
    <div class="instructions">二维码有效期5分钟</div>
    <button @click="refreshQRCode" class="refresh-btn">刷新二维码</button>
  </div>
</template>

<script>
import { defineComponent, onMounted, ref } from 'vue';

export default defineComponent({
  name: 'WeChatLogin',
  setup() {
    const qrCodeRef = ref(null);

    const config = {
      appid: "你的微信开放平台AppID",
      redirect_uri: encodeURIComponent("/"),
      state: "random_state", // 防止CSRF攻击的随机字符串
      self_redirect: false, // 是否在自己页面跳转
      scope: "snsapi_login", // 授权作用域
      style: "black", // 按钮样式
      href: "" // 自定义样式链接
    };

    // 动态加载微信JS-SDK
    const loadWeChatScript = () => {
      return new Promise((resolve) => {
        if (typeof WxLogin !== 'undefined') {
          resolve();
          return;
        }

        const script = document.createElement('script');
        script.src = 'https://res.wx.qq.com/connect/zh_CN/htmledition/js/wxLogin.js';
        script.onload = () => resolve();
        document.head.appendChild(script);
      });
    };

    // 生成微信登录二维码
    const generateQRCode = () => {
      if (!qrCodeRef.value) return;

      new WxLogin({
        self_redirect: config.self_redirect,
        id: "qrCode",
        appid: config.appid,
        scope: config.scope,
        redirect_uri: config.redirect_uri,
        state: config.state,
        style: config.style,
        href: config.href
      });
    };

    // 刷新二维码
    const refreshQRCode = () => {
      if (qrCodeRef.value) {
        qrCodeRef.value.innerHTML = '';
        generateQRCode();
      }
    };

    // 检查登录状态 (可选)
    const checkLoginStatus = () => {
      // 这里可以添加轮询检查登录状态的逻辑
      // 通常由后端接口返回
    };

    onMounted(async () => {
      await loadWeChatScript();
      generateQRCode();
    });

    return {
      qrCodeRef,
      refreshQRCode,
      checkLoginStatus
    };
  }
});
</script>

<style scoped>
.wechat-login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px; /* 增加内边距 */
  margin: 20px auto;
  max-width: 400px; /* 增大最大宽度 */
  border: 1px solid #ddd;
  border-radius: 12px; /* 增大圆角 */
  background-color: #f9f9f9;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); /* 添加阴影效果 */
}
.wechat-login-container h3 {
  font-size: 1.5rem; /* 增大标题字体 */
  margin-bottom: 20px; /* 调整标题间距 */
}
.qr-code {
  width: 280px; /* 增大二维码宽度 */
  height: 280px; /* 增大二维码高度 */
  margin-bottom: 20px; /* 调整下边距 */
  background-color: white;
  padding: 20px; /* 增大内边距 */
  border: 1px solid #eee; /* 添加边框 */
  border-radius: 8px; /* 二维码容器圆角 */
}
.instructions {
  text-align: center;
  margin-top: 15px;
  font-size: 16px; /* 增大说明文字 */
  color: #666;
}
.refresh-btn {
  margin-top: 20px;
  padding: 10px 24px; /* 增大按钮尺寸 */
  background-color: #09bb07;
  color: white;
  border: none;
  border-radius: 6px; /* 增大按钮圆角 */
  cursor: pointer;
  font-size: 16px; /* 增大按钮文字 */
  transition: background-color 0.3s; /* 添加过渡效果 */
}
.refresh-btn:hover {
  background-color: #07a105; /* 悬停颜色 */
}
</style>
