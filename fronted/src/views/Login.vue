<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <h1 class="title">欢迎使用 SmartNote</h1>

      <!-- 切换登录/注册 -->
      <el-radio-group v-model="currentView" size="large" style="width: 100%; margin-bottom: 20px">
        <el-radio-button value="登录" style="width: 50%">登录</el-radio-button>
        <el-radio-button value="注册" style="width: 50%">注册</el-radio-button>
      </el-radio-group>

      <!-- 登录表单 -->
      <el-form v-if="currentView === '登录'" ref="loginFormRef" :model="loginForm"
               :rules="loginRules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item prop="account" label="账号">
          <el-input v-model="loginForm.account" placeholder="请输入手机号或邮箱" size="large"/>
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"
                    size="large" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" size="large" :loading="loading"
                     style="width: 100%">登录
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 注册表单 -->
      <el-form v-else ref="registerFormRef" :model="registerForm" :rules="registerRules"
               label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="注册方式">
          <el-radio-group v-model="registerForm.type" size="large" style="width: 100%">
            <el-radio-button value="手机号" style="width: 50%">手机号</el-radio-button>
            <el-radio-button value="邮箱" style="width: 50%">邮箱</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item prop="account" :label="registerForm.type">
          <el-input v-model="registerForm.account" :placeholder="'请输入' + registerForm.type"
                    size="large"/>
        </el-form-item>
        <el-form-item prop="password" label="密码">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"
                    size="large" show-password/>
        </el-form-item>
        <el-form-item prop="confirmPassword" label="确认密码">
          <el-input v-model="registerForm.confirmPassword" type="password"
                    placeholder="请再次输入密码" size="large" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" native-type="submit" size="large" :loading="loading"
                     style="width: 100%">注册
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {post} from '@/utils/request.js'
import {ElMessage} from 'element-plus'

const router = useRouter()

// 当前视图
const currentView = ref('登录')

// 加载状态
const loading = ref(false)

// 表单引用
const loginFormRef = ref(null)
const registerFormRef = ref(null)

// 登录表单
const loginForm = reactive({
  account: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  type: '手机号',
  account: '',
  password: '',
  confirmPassword: ''
})

// 登录校验规则
const loginRules = {
  account: [{required: true, message: '请输入账号', trigger: 'blur'}],
  password: [{required: true, message: '请输入密码', trigger: 'blur'}]
}

// 注册校验规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  account: [{required: true, message: '请输入账号', trigger: 'blur'}],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'},
    {min: 8, max: 20, message: '密码长度为8-20位', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请确认密码', trigger: 'blur'},
    {validator: validateConfirmPassword, trigger: 'blur'}
  ]
}

// 登录
const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = {password: loginForm.password}
    if (/^1[3-9]\d{9}$/.test(loginForm.account)) {
      data.phone = loginForm.account
    } else {
      data.email = loginForm.account
    }
    const res = await post('/users/login', data)
    if (res.data) {
      localStorage.setItem('token', res.data)
    }
    ElMessage.success('登录成功！')
    router.push('/note')
  } catch (err) {
    ElMessage.error(err.msg || '登录失败！')
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const data = {password: registerForm.password}
    if (registerForm.type === '手机号') {
      data.phone = registerForm.account
    } else {
      data.email = registerForm.account
    }
    await post('/users', data)
    ElMessage.success('注册成功！')
    currentView.value = '登录'
  } catch (err) {
    ElMessage.error(err.msg || '注册失败！')
  } finally {
    loading.value = false
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
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 12px;
}

.title {
  text-align: center;
  font-size: 24px;
  color: #303133;
  margin-bottom: 24px;
  font-weight: 500;
  letter-spacing: 2px;
}

:deep(.el-radio-group) {
  display: flex;
}

:deep(.el-radio-button__inner) {
  width: 100%;
}
</style>
