<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">欢迎使用 SmartNote</h1>

      <!-- 切换登录/注册 -->
      <div class="tab">
        <span :class="{ active: currentView === 'login' }" @click="switchView('login')">登录</span>
        <span :class="{ active: currentView === 'register' }" @click="switchView('register')">注册</span>
      </div>

      <!-- 登录表单 -->
      <form v-if="currentView === 'login'" @submit.prevent="handleLogin">
        <div class="form-item">
          <label>账号</label>
          <input v-model="loginForm.account" placeholder="请输入手机号或邮箱"/>
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="loginForm.password" type="password" placeholder="请输入密码"/>
        </div>
        <button type="submit">登录</button>
      </form>

      <!-- 注册表单 -->
      <form v-else @submit.prevent="handleRegister">
        <div class="form-item">
          <div class="register-type">
            <span :class="{ active: registerForm.type === 'phone' }" @click="registerForm.type = 'phone'">手机号</span>
            <span :class="{ active: registerForm.type === 'email' }" @click="registerForm.type = 'email'">邮箱</span>
          </div>
        </div>
        <div class="form-item">
          <label>{{ registerForm.type === 'phone' ? '手机号' : '邮箱' }}</label>
          <input
            v-model="registerForm.account"
            :placeholder="registerForm.type === 'phone' ? '请输入手机号' : '请输入邮箱'"
          />
        </div>
        <div class="form-item">
          <label>密码</label>
          <input v-model="registerForm.password" type="password" placeholder="请输入密码"/>
        </div>
        <div class="form-item">
          <label>确认密码</label>
          <input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"/>
        </div>
        <button type="submit">注册</button>
      </form>
    </div>

    <!-- 消息提示 -->
    <div class="message" :class="[messageType, { show: messageVisible }]">
      <span class="message-icon">{{ messageType === 'success' ? '✓' : '✕' }}</span>
      <span>{{ messageText }}</span>
    </div>
  </div>
</template>

<script setup>
import {ref, reactive} from 'vue'
import {post} from '@/utils/request.js'

// 当前视图：login 或 register
const currentView = ref('login')

// 登录表单
const loginForm = reactive({
  account: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  type: 'phone',
  account: '',
  password: '',
  confirmPassword: ''
})

// 消息提示
const messageText = ref('')
const messageVisible = ref(false)
const messageType = ref('success')
let messageTimer = null

// 显示消息提示
const showMessage = (msg, type = 'success') => {
  if (messageTimer) {
    clearTimeout(messageTimer)
  }
  messageText.value = msg
  messageType.value = type
  messageVisible.value = true
  messageTimer = setTimeout(() => {
    messageVisible.value = false
  }, 2500)
}

// 切换视图
const switchView = (view) => {
  currentView.value = view
}

// 登录
const handleLogin = async () => {
  if (!loginForm.account || !loginForm.password) {
    showMessage('请填写账号和密码', 'error')
    return
  }

  try {
    const data = {password: loginForm.password}
    if (/^1[3-9]\d{9}$/.test(loginForm.account)) {
      data.phone = loginForm.account
    } else {
      data.email = loginForm.account
    }
    await post('/users/login', data)
    showMessage('登录成功！', 'success')
  } catch (err) {
    showMessage(err.msg || '请求失败，请稍后重试', 'error')
  }
}

// 注册
const handleRegister = async () => {
  if (!registerForm.account) {
    showMessage(registerForm.type === 'phone' ? '请输入手机号' : '请输入邮箱', 'error')
    return
  }
  if (!registerForm.password) {
    showMessage('请输入密码', 'error')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    showMessage('两次密码不一致', 'error')
    return
  }

  try {
    const data = {password: registerForm.password}
    if (registerForm.type === 'phone') {
      data.phone = registerForm.account
    } else {
      data.email = registerForm.account
    }
    await post('/users', data)
    showMessage('注册成功！', 'success')
    switchView('login')
  } catch (err) {
    showMessage(err.msg || '请求失败，请稍后重试', 'error')
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.08'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.login-box {
  width: 360px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  font-size: 24px;
  color: #2c3e50;
  margin-bottom: 30px;
  font-weight: normal;
  letter-spacing: 2px;
}

.tab {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  border-bottom: 1px solid #eee;
}

.tab span {
  padding: 10px 30px;
  cursor: pointer;
  color: #999;
  transition: all 0.3s;
}

.tab span.active {
  color: #2c3e50;
  border-bottom: 2px solid #2c3e50;
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 6px;
  color: #666;
  font-size: 14px;
}

.form-item input {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-item input:focus {
  outline: none;
  border-color: #2c3e50;
}

.register-type {
  display: flex;
  gap: 12px;
}

.register-type span {
  flex: 1;
  height: 40px;
  line-height: 40px;
  text-align: center;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  color: #999;
  transition: all 0.3s;
}

.register-type span.active {
  border-color: #2c3e50;
  color: #2c3e50;
  background: #f8f9fa;
}

button {
  width: 100%;
  height: 42px;
  margin-top: 10px;
  background: #2c3e50;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

button:hover {
  background: #1a252f;
}

.message {
  position: fixed;
  top: 30px;
  left: 50%;
  transform: translateX(-50%) translateY(-30px);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 28px;
  border-radius: 6px;
  font-size: 15px;
  opacity: 0;
  transition: all 0.4s ease;
  pointer-events: none;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.message.show {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.message-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 13px;
  font-weight: bold;
}

/* 成功提示 - 水墨青绿 */
.message.success {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border: 1px solid #a5d6a7;
  color: #2e7d32;
}

.message.success .message-icon {
  background: #4caf50;
  color: #fff;
}

/* 错误提示 - 水墨淡红 */
.message.error {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border: 1px solid #ef9a9a;
  color: #c62828;
}

.message.error .message-icon {
  background: #e53935;
  color: #fff;
}
</style>
