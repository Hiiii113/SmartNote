import axios from 'axios'
import router from '@/router/index.js'
import { ElMessage } from 'element-plus'

// 创建axios实例
const instance = axios.create({
  baseURL: 'http://localhost:8081',
  timeout: 10000,
})

// 防止多次弹窗跳转
let isRedirecting = false
// 防止网络错误重复弹窗
let isShowingNetworkError = false

// 请求拦截器
instance.interceptors.request.use(
  // 成功则返回让axios继续请求
  (config) => {
    // 添加 satoken
    config.headers['satoken'] = localStorage.getItem('token')
    return config
  },
  // 失败则抛出异常error
  (error) => Promise.reject(error),
)

// 响应拦截器
instance.interceptors.response.use(
  (response) => {
    const { code, msg } = response.data
    if (code === 200 || code === 201) {
      return response.data
    } else if (code === 401) {
      handleUnauthorized()
      return Promise.reject(response.data)
    } else {
      ElMessage.error(msg || '操作失败')
      return Promise.reject(response.data)
    }
  },
  (error) => {
    if (error.response?.status === 401) {
      handleUnauthorized()
    } else if (!isShowingNetworkError) {
      // 防止网络错误重复弹窗
      isShowingNetworkError = true
      ElMessage.error('网络异常，请稍后重试')
      setTimeout(() => {
        isShowingNetworkError = false
      }, 3000)
    }
    return Promise.reject(error)
  },
)

// 统一处理未授权
function handleUnauthorized() {
  // 立即设置标志，防止其他请求重复进入
  if (isRedirecting) return
  isRedirecting = true

  localStorage.removeItem('token')
  // 先关闭所有已有的消息，再显示新的
  ElMessage.closeAll()
  ElMessage.warning('登录已过期，请重新登录')

  // 延迟重置，确保跳转完成
  setTimeout(() => {
    router.push('/')
    isRedirecting = false
  }, 100)
}

// 封装get和post和put和patch和delete请求
export const get = function (url, params) {
  // url: 路径地址  params: 参数
  return instance.get(url, { params })
}

export const post = function (url, data, config = {}) {
  // url: 路径地址  data: 数据  config: 额外配置（如timeout）
  return instance.post(url, data, config)
}

export const put = function (url, data) {
  // url: 路径地址  data: 数据
  return instance.put(url, data)
}

export const patch = function (url, data) {
  // url: 路径地址  data: 数据
  return instance.patch(url, data)
}

export const del = function (url, data) {
  // url: 路径地址  data: 数据
  return instance.delete(url, { data })
}
