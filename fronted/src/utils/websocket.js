/**
 * WebSocket 工具类
 */
class WebSocketClient {
  constructor() {
    this.ws = null
    this.url = 'ws://localhost:8081/ws/chat'
    this.reconnectInterval = 5000
    this.heartbeatInterval = 30000
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.messageHandler = null
    this.isConnecting = false
  }

  // 连接
  connect(token) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket 已连接')
      return
    }

    if (this.isConnecting) {
      console.log('WebSocket 正在连接中...')
      return
    }

    this.isConnecting = true
    const url = `${this.url}?token=${token}`

    try {
      this.ws = new WebSocket(url)

      this.ws.onopen = () => {
        console.log('WebSocket 连接成功')
        this.isConnecting = false
        this.startHeartbeat()
      }

      this.ws.onmessage = (event) => {
        console.log('收到消息:', event.data)
        if (this.messageHandler) {
          try {
            const data = JSON.parse(event.data)
            this.messageHandler(data)
          } catch (e) {
            console.error('解析消息失败:', e)
          }
        }
      }

      this.ws.onclose = () => {
        console.log('WebSocket 连接关闭')
        this.isConnecting = false
        this.stopHeartbeat()
        this.reconnect(token)
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket 错误:', error)
        this.isConnecting = false
      }
    } catch (error) {
      console.error('WebSocket 连接失败:', error)
      this.isConnecting = false
      this.reconnect(token)
    }
  }

  // 断开连接
  disconnect() {
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  // 重连
  reconnect(token) {
    if (this.reconnectTimer) return
    console.log(`${this.reconnectInterval / 1000}秒后尝试重连...`)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect(token)
    }, this.reconnectInterval)
  }

  // 开始心跳
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'heartbeat' }))
      }
    }, this.heartbeatInterval)
  }

  // 停止心跳
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  // 设置消息处理器
  onMessage(handler) {
    this.messageHandler = handler
  }

  // 发送消息
  send(data) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(data))
      return true
    }
    console.error('WebSocket 未连接')
    return false
  }

  // 检查是否已连接
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 单例模式
export const wsClient = new WebSocketClient()
