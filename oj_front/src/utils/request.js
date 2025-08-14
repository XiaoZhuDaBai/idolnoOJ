import axios from 'axios'
import { ElMessage } from 'element-plus' // 假设使用 Element Plus 的消息提示

// 创建 axios 实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // 从环境变量获取基础URL，默认为空
  timeout: 0, // 默认超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果响应直接是数据，直接返回
    if (!Object.prototype.hasOwnProperty.call(res, 'code')) {
      return res
    }

    // 根据后端约定的状态码判断请求是否成功
    if (res.code === 200) {
      return res
    }

    // 处理特定的错误码
    switch (res.code) {
      case 401:
        // token 过期或未登录
        ElMessage.error('请先登录')
        // 可以在这里处理登出逻辑
        localStorage.removeItem('token')
        window.location.href = '/login'
        break
      case 403:
        ElMessage.error('没有权限访问')
        break
      case 500:
        ElMessage.error('服务器错误')
        break
      default:
        ElMessage.error(res.message || '请求失败')
    }

    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    // 处理 HTTP 错误状态
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = '请求错误'
          break
        case 401:
          message = '未授权，请登录'
          // 可以在这里处理登出逻辑
          localStorage.removeItem('token')
          window.location.href = '/login'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址出错'
          break
        case 408:
          message = '请求超时'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 501:
          message = '服务未实现'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        case 505:
          message = 'HTTP版本不受支持'
          break
        default:
          message = `连接错误${error.response.status}`
      }
    } else if (error.request) {
      message = '网络错误，请检查您的网络连接'
    } else {
      message = error.message
    }

    ElMessage.error(message)
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 封装 GET 请求
export function get(url, params) {
  return service({
    url,
    method: 'get',
    params
  })
}

// 封装 POST 请求
export function post(url, data) {
  return service({
    url,
    method: 'post',
    data
  })
}

// 封装 PUT 请求
export function put(url, data) {
  return service({
    url,
    method: 'put',
    data
  })
}

// 封装 DELETE 请求
export function del(url) {
  return service({
    url,
    method: 'delete'
  })
}

// 导出 axios 实例
export default service
