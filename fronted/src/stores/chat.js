import {defineStore} from 'pinia'
import {ref} from 'vue'
import {get, post} from '@/utils/request.js'
import {wsClient} from '@/utils/websocket.js'

export const useChatStore = defineStore('chat', () => {
  // 当前聊天的好友
  const currentFriend = ref(null)

  // 聊天记录
  const messages = ref([])

  // 未读消息数
  const unreadCount = ref(0)

  // 未读消息详情（按好友分组）
  const unreadMap = ref({})

  // 当前用户ID
  const currentUserId = ref(null)

  // 初始化 WebSocket
  const initWebSocket = (token) => {
    wsClient.connect(token)

    // 设置消息处理器
    wsClient.onMessage((data) => {
      console.log('收到推送消息:', data)

      // 更新未读数
      unreadCount.value++

      // 如果是当前聊天对象发来的消息，添加到消息列表
      if (currentFriend.value && data.fromId === currentFriend.value.friendId) {
        messages.value.push({
          fromId: data.fromId,
          toId: data.toId,
          content: data.content,
          msgType: data.msgType,
          createdAt: new Date().toISOString()
        })
      } else {
        // 否则更新未读消息 map
        if (!unreadMap.value[data.fromId]) {
          unreadMap.value[data.fromId] = 0
        }
        unreadMap.value[data.fromId]++
      }
    })
  }

  // 断开 WebSocket
  const disconnectWebSocket = () => {
    wsClient.disconnect()
  }

  // 发送消息（后端从 token 获取 fromId，前端只需传 toId 和 content）
  const sendMessage = async (toId, content, msgType = 1) => {
    try {
      await post('/chat/send', {
        toId,
        content,
        msgType
      })

      // 添加到本地消息列表（fromId 从当前用户获取）
      messages.value.push({
        fromId: currentUserId.value,
        toId,
        content,
        msgType,
        createdAt: new Date().toISOString()
      })

      return true
    } catch (error) {
      console.error('发送消息失败:', error)
      return false
    }
  }

  // 获取聊天记录
  const fetchHistory = async (friendId) => {
    try {
      const res = await get(`/chat/history/${friendId}`)
      messages.value = res.data || []
    } catch (error) {
      console.error('获取聊天记录失败:', error)
      messages.value = []
    }
  }

  // 获取未读消息数
  const fetchUnreadCount = async () => {
    try {
      const res = await get('/chat/unread')
      unreadCount.value = res.data || 0
    } catch (error) {
      console.error('获取未读数失败:', error)
    }
  }

  // 标记已读
  const markAsRead = async (friendId) => {
    try {
      await post(`/chat/read/${friendId}`)
      // 清除该好友的未读数
      if (unreadMap.value[friendId]) {
        unreadCount.value -= unreadMap.value[friendId]
        delete unreadMap.value[friendId]
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  // 设置当前聊天好友
  const setCurrentFriend = async (friend) => {
    currentFriend.value = friend
    if (friend) {
      await fetchHistory(friend.friendId)
      await markAsRead(friend.friendId)
    } else {
      messages.value = []
    }
  }

  // 设置当前用户ID
  const setCurrentUserId = (userId) => {
    currentUserId.value = userId
  }

  return {
    currentFriend,
    messages,
    unreadCount,
    unreadMap,
    currentUserId,
    initWebSocket,
    disconnectWebSocket,
    sendMessage,
    fetchHistory,
    fetchUnreadCount,
    markAsRead,
    setCurrentFriend,
    setCurrentUserId
  }
})
