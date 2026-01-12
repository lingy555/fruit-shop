
/**
 * WebSocket配置文件
 * 用于前后端实时通信，如订单状态更新、新消息通知等
 */

// WebSocket连接配置
export const websocketConfig = {
  // WebSocket服务器地址，根据实际环境修改
  url: process.env.NODE_ENV === 'production' 
    ? 'wss://your-domain.com/ws' 
    : 'ws://localhost:8080/ws',

  // 重连间隔（毫秒）
  reconnectInterval: 30000,

  // 最大重连次数
  maxReconnectAttempts: 5,

  // 心跳间隔（毫秒）
  heartbeatInterval: 30000,

  // 消息类型
  messageType: {
    // 心跳消息
    HEARTBEAT: 'heartbeat',

    // 订单状态更新
    ORDER_STATUS_UPDATE: 'order_status_update',

    // 新消息通知
    NEW_MESSAGE: 'new_message',

    // 聊天消息
    CHAT_MESSAGE: 'chat_message',

    // 系统通知
    SYSTEM_NOTIFICATION: 'system_notification'
  }
}

// WebSocket事件类型
export const websocketEvent = {
  // 连接成功
  ON_OPEN: 'onopen',

  // 连接关闭
  ON_CLOSE: 'onclose',

  // 连接错误
  ON_ERROR: 'onerror',

  // 收到消息
  ON_MESSAGE: 'onmessage'
}

// 默认导出配置
export default {
  install(app) {
    // 全局注册WebSocket实例
    app.config.globalProperties.$websocket = new WebSocket(websocketConfig.url)

    // 监听WebSocket事件
    app.config.globalProperties.$websocket.onopen = () => {
      console.log('WebSocket连接已建立')
      app.config.globalProperties.$isConnected = true
    }

    app.config.globalProperties.$websocket.onclose = () => {
      console.log('WebSocket连接已关闭')
      app.config.globalProperties.$isConnected = false

      // 尝试重连
      if (websocketConfig.maxReconnectAttempts > 0) {
        setTimeout(() => {
          this.connect()
        }, websocketConfig.reconnectInterval)
      }
    }

    app.config.globalProperties.$websocket.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
      app.config.globalProperties.$isConnected = false
    }

    app.config.globalProperties.$websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)

        // 根据消息类型处理
        switch (data.type) {
          case websocketConfig.messageType.HEARTBEAT:
            // 处理心跳响应
            console.log('收到心跳响应')
            break

          case websocketConfig.messageType.ORDER_STATUS_UPDATE:
            // 处理订单状态更新
            console.log('收到订单状态更新:', data)
            // 触发全局事件，通知相关组件更新
            app.config.globalProperties.$eventBus?.emit('order-status-update', data)
            break

          case websocketConfig.messageType.NEW_MESSAGE:
            // 处理新消息通知
            console.log('收到新消息:', data)
            // 触发全局事件
            app.config.globalProperties.$eventBus?.emit('new-message', data)
            break

          case websocketConfig.messageType.SYSTEM_NOTIFICATION:
            // 处理系统通知
            console.log('收到系统通知:', data)
            // 触发全局事件
            app.config.globalProperties.$eventBus?.emit('system-notification', data)
            break

          default:
            console.log('收到未知消息类型:', data.type)
        }
      } catch (error) {
        console.error('解析WebSocket消息失败:', error)
      }
    }

    // 发送消息方法
    app.config.globalProperties.$sendMessage = (data) => {
      if (app.config.globalProperties.$isConnected && app.config.globalProperties.$websocket.readyState === 1) {
        const message = JSON.stringify(data)
        app.config.globalProperties.$websocket.send(message)
      } else {
        console.warn('WebSocket未连接，无法发送消息')
      }
    }

    // 关闭连接方法
    app.config.globalProperties.$close = () => {
      if (app.config.globalProperties.$isConnected) {
        app.config.globalProperties.$isConnected = false
        app.config.globalProperties.$websocket.close()
      }
    }
  }
}
