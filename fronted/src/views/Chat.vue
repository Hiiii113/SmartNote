<template>
  <div class="chat-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">SmartNote</div>
      <div class="nav-btns">
        <el-button @click="router.push('/note')">返回笔记</el-button>
        <el-button @click="router.push('/friends')">好友管理</el-button>
      </div>
    </header>

    <div class="main">
      <!-- 左侧好友列表 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <span>好友列表</span>
          <el-badge :value="chatStore.unreadCount" :hidden="chatStore.unreadCount === 0" />
        </div>
        <el-empty v-if="friends.length === 0" description="暂无好友" :image-size="60" />
        <div v-else class="friend-list">
          <div
            v-for="friend in friends"
            :key="friend.friendId"
            :class="['friend-item', { active: chatStore.currentFriend?.friendId === friend.friendId }]"
            @click="selectFriend(friend)"
          >
            <el-avatar :size="40" :src="friend.friendAvatar || defaultAvatar" />
            <div class="friend-info">
              <div class="friend-name">{{ friend.friendName }}</div>
              <div class="friend-motto">{{ friend.friendMotto || '这个人很懒' }}</div>
            </div>
            <el-badge
              v-if="chatStore.unreadMap[friend.friendId]"
              :value="chatStore.unreadMap[friend.friendId]"
            />
          </div>
        </div>
      </aside>

      <!-- 右侧聊天区域 -->
      <main class="chat-area">
        <template v-if="chatStore.currentFriend">
          <!-- 聊天头部 -->
          <div class="chat-header">
            <span>与 {{ chatStore.currentFriend.friendName }} 的聊天</span>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list">
            <div
              v-for="(msg, index) in chatStore.messages"
              :key="index"
              :class="['message-item', { mine: msg.fromId === chatStore.currentUserId }]"
            >
              <div class="message-content">
                {{ msg.content }}
              </div>
              <div class="message-time">
                {{ formatTime(msg.createdAt) }}
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="input-area">
            <el-input
              v-model="inputMessage"
              placeholder="输入消息..."
              @keyup.enter="handleSend"
            />
            <el-button type="primary" @click="handleSend" :disabled="!inputMessage.trim()">
              发送
            </el-button>
          </div>
        </template>

        <el-empty v-else description="选择一个好友开始聊天" />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat.js'
import { get } from '@/utils/request.js'

const router = useRouter()
const chatStore = useChatStore()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 好友列表
const friends = ref([])

// 输入的消息
const inputMessage = ref('')

// 消息列表引用
const messageListRef = ref(null)

// 获取好友列表
const fetchFriends = async () => {
  try {
    const res = await get('/friends')
    friends.value = res.data || []
  } catch (error) {
    console.error('获取好友列表失败:', error)
  }
}

// 选择好友
const selectFriend = async (friend) => {
  await chatStore.setCurrentFriend(friend)
  scrollToBottom()
}

// 发送消息
const handleSend = async () => {
  if (!inputMessage.value.trim() || !chatStore.currentFriend) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''

  await chatStore.sendMessage(chatStore.currentFriend.friendId, content)
  scrollToBottom()
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()

  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  // 初始化 WebSocket
  const token = localStorage.getItem('token')
  if (token) {
    chatStore.initWebSocket(token)
  }

  // 获取当前用户信息
  try {
    const res = await get('/users/info')
    if (res.data) {
      chatStore.setCurrentUserId(res.data.id)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }

  // 获取好友列表
  await fetchFriends()

  // 获取未读消息数
  chatStore.fetchUnreadCount()
})

onUnmounted(() => {
  chatStore.disconnectWebSocket()
})
</script>

<style scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.nav-btns {
  display: flex;
  gap: 10px;
}

.main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
}

.friend-list {
  flex: 1;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
}

.friend-item:hover {
  background: #f5f7fa;
}

.friend-item.active {
  background: #ecf5ff;
}

.friend-info {
  flex: 1;
  overflow: hidden;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.friend-motto {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.chat-header {
  height: 50px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-weight: 500;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  flex-direction: column;
  max-width: 60%;
}

.message-item.mine {
  align-self: flex-end;
}

.message-item.mine .message-content {
  background: #409eff;
  color: #fff;
}

.message-item:not(.mine) .message-content {
  background: #fff;
  color: #303133;
}

.message-content {
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.input-area {
  padding: 16px;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  gap: 12px;
}

.input-area .el-input {
  flex: 1;
}
</style>
