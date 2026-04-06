import axios from 'axios'
import router from '@/router/index.js'

// 创建axios实例
const instance = axios.create({
    baseURL: 'http://localhost:8081',
    timeout: 10000,
})

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
            localStorage.removeItem('token')
            router.push('/login')
            alert('登录已过期，请重新登录')
        } else {
            console.error(msg)
            return Promise.reject(response.data)
        }
    },
    (error) => {
        return Promise.reject(error)
    },
)

// 封装get和post和put和patch和delete请求
export const get = function (url, params) {
    // url: 路径地址  params: 参数
    return instance.get(url, { params })
}

export const post = function (url, data) {
    // url: 路径地址  data: 数据
    return instance.post(url, data)
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
