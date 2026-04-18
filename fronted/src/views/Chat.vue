<template>
  <div class="chat-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">SmartNote</div>
      <div class="nav-btns">
        <el-button type="primary" :icon="Plus" @click="showAddDialog = true">添加好友</el-button>
        <el-button @click="router.push('/note')">返回笔记</el-button>
      </div>
    </header>

    <div class="main">
      <!-- 左侧好友列表 -->
      <aside class="sidebar">
        <div class="sidebar-header">
          <span>好友 ({{ friends.length }})</span>
          <el-badge :value="chatStore.unreadCount" :hidden="chatStore.unreadCount === 0" />
        </div>

        <!-- 好友申请提醒 -->
        <div v-if="pendingCount > 0" class="request-notice" @click="showRequestsDialog = true">
          <el-icon><Bell /></el-icon>
          <span>{{ pendingCount }} 条待处理的好友申请</span>
          <el-icon><ArrowRight /></el-icon>
        </div>

        <el-empty v-if="friends.length === 0" description="暂无好友，快去添加吧" :image-size="60">
          <el-button type="primary" size="small" @click="showAddDialog = true">添加好友</el-button>
        </el-empty>

        <div v-else class="friend-list">
          <div
            v-for="friend in friends"
            :key="friend.friendId"
            :class="['friend-item', { active: chatStore.currentFriend?.friendId === friend.friendId }]"
            @click="selectFriend(friend)"
          >
            <el-avatar :size="40" :src="getAvatarUrl(friend.friendAvatar)" />
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
            <el-avatar :size="36" :src="getAvatarUrl(chatStore.currentFriend.friendAvatar)" />
            <span class="chat-title">{{ chatStore.currentFriend.friendName }}</span>
            <el-dropdown trigger="click">
              <el-icon class="more-icon"><MoreFilled /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showFriendInfo">查看资料</el-dropdown-item>
                  <el-dropdown-item divided @click="handleDeleteFriend">删除好友</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 消息列表 -->
          <div ref="messageListRef" class="message-list">
            <div
              v-for="(msg, index) in chatStore.messages"
              :key="index"
              :class="['message-item', { mine: msg.fromId === chatStore.currentUserId }]"
            >
              <el-avatar
                v-if="msg.fromId !== chatStore.currentUserId"
                :size="32"
                :src="getAvatarUrl(chatStore.currentFriend.friendAvatar)"
                class="msg-avatar"
              />
              <div class="msg-body">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
              </div>
              <el-avatar
                v-if="msg.fromId === chatStore.currentUserId"
                :size="32"
                :src="getAvatarUrl(currentUserAvatar)"
                class="msg-avatar"
              />
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

        <div v-else class="empty-chat">
          <el-empty description="选择好友开始聊天" />
        </div>
      </main>
    </div>

    <!-- 添加好友弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加好友" width="400px">
      <el-form>
        <el-form-item label="手机号/邮箱">
          <el-input v-model="addForm.contact" placeholder="请输入对方手机号或邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAddFriend">发送申请</el-button>
      </template>
    </el-dialog>

    <!-- 好友申请弹窗 -->
    <el-dialog v-model="showRequestsDialog" title="好友申请" width="500px">
      <el-tabs>
        <el-tab-pane :label="`收到的申请 (${receivedRequests.length})`">
          <el-empty v-if="receivedRequests.length === 0" description="暂无申请" :image-size="60" />
          <div v-else class="request-list">
            <div v-for="request in receivedRequests" :key="request.id" class="request-item">
              <el-avatar :size="40" :src="getAvatarUrl(request.requesterAvatar)" />
              <div class="request-info">
                <div class="request-name">{{ request.requesterName }}</div>
                <div class="request-time">{{ formatTime(request.createdAt) }}</div>
              </div>
              <div v-if="request.status === 'PENDING'" class="request-actions">
                <el-button type="primary" size="small" @click="handleRequest(request.id, true)">同意</el-button>
                <el-button size="small" @click="handleRequest(request.id, false)">拒绝</el-button>
              </div>
              <el-tag v-else :type="request.status === 'ACCEPTED' ? 'success' : 'info'" size="small">
                {{ request.status === 'ACCEPTED' ? '已同意' : '已拒绝' }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 好友详情弹窗 -->
    <el-dialog v-model="showFriendDetail" title="好友详情" width="400px">
      <div v-if="currentFriendDetail" class="friend-detail">
        <div class="detail-avatar">
          <el-avatar :size="80" :src="getAvatarUrl(currentFriendDetail.friendAvatar)" />
        </div>
        <div class="detail-info">
          <div class="detail-item">
            <span class="label">用户名：</span>
            <span class="value">{{ currentFriendDetail.friendName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">座右铭：</span>
            <span class="value">{{ currentFriendDetail.friendMotto || '这个人很懒，什么都没写' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">分组：</span>
            <span v-if="!editingGroup" class="value">{{ currentFriendDetail.groupName || '默认' }}</span>
            <el-input
              v-else
              v-model="newGroupName"
              size="small"
              style="width: 120px"
              placeholder="输入分组名"
            />
            <el-button
              v-if="!editingGroup"
              type="primary"
              text
              size="small"
              @click="startEditGroup"
            >
              修改
            </el-button>
            <template v-else>
              <el-button type="primary" size="small" @click="saveGroup">保存</el-button>
              <el-button size="small" @click="editingGroup = false">取消</el-button>
            </template>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="danger" @click="handleDeleteFriend">删除好友</el-button>
        <el-button @click="showFriendDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat.js'
import { get, post, put, del } from '@/utils/request.js'
import { getAvatarUrl } from '@/utils/common.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Bell, ArrowRight, MoreFilled } from '@element-plus/icons-vue'

const router = useRouter()
const chatStore = useChatStore()

// 好友列表
const friends = ref([])

// 收到的申请
const receivedRequests = ref([])

// 待处理的申请数量
const pendingCount = computed(() => receivedRequests.value.filter(r => r.status === 'PENDING').length)

// 输入的消息
const inputMessage = ref('')

// 消息列表引用
const messageListRef = ref(null)

// 当前用户头像
const currentUserAvatar = ref('')

// 弹窗状态
const showAddDialog = ref(false)
const showRequestsDialog = ref(false)
const showFriendDetail = ref(false)
const currentFriendDetail = ref(null)
const editingGroup = ref(false)
const newGroupName = ref('')

// 添加好友表单
const addForm = ref({
  contact: ''
})

// 获取好友列表
const fetchFriends = async () => {
  try {
    const res = await get('/friends')
    friends.value = res.data || []
  } catch (error) {
    console.error('获取好友列表失败:', error)
  }
}

// 获取收到的好友申请
const fetchReceivedRequests = async () => {
  try {
    const res = await get('/friend-requests/received')
    receivedRequests.value = res.data || []
  } catch (err) {
    console.error('获取申请列表失败:', err)
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

// 添加好友
const handleAddFriend = async () => {
  if (!addForm.value.contact.trim()) {
    ElMessage.warning('请输入手机号或邮箱')
    return
  }
  try {
    await post('/friend-requests', { account: addForm.value.contact })
    ElMessage.success('好友申请已发送')
    showAddDialog.value = false
    addForm.value.contact = ''
  } catch (err) {
    if (err.msg) ElMessage.error(err.msg)
  }
}

// 处理好友申请
const handleRequest = async (requestId, accept) => {
  try {
    await put(`/friend-requests/${requestId}`, { accept })
    ElMessage.success(accept ? '已同意好友申请' : '已拒绝好友申请')
    fetchReceivedRequests()
    fetchFriends()
  } catch (err) {
    if (err.msg) ElMessage.error(err.msg)
  }
}

// 查看好友资料
const showFriendInfo = () => {
  if (!chatStore.currentFriend) return
  currentFriendDetail.value = chatStore.currentFriend
  editingGroup.value = false
  newGroupName.value = ''
  showFriendDetail.value = true
}

// 开始编辑分组
const startEditGroup = () => {
  newGroupName.value = currentFriendDetail.value.groupName || '默认'
  editingGroup.value = true
}

// 保存分组
const saveGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('分组名不能为空')
    return
  }
  try {
    await put(`/friends/group/${currentFriendDetail.value.friendId}`, { group: newGroupName.value.trim() })
    ElMessage.success('分组修改成功')
    currentFriendDetail.value.groupName = newGroupName.value.trim()
    editingGroup.value = false
    fetchFriends()
  } catch (err) {
    if (err.msg) ElMessage.error(err.msg)
  }
}

// 删除好友
const handleDeleteFriend = async () => {
  if (!chatStore.currentFriend) return
  try {
    await ElMessageBox.confirm(`确定要删除好友 ${chatStore.currentFriend.friendName} 吗？`, '提示', { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' })
    await del(`/friends/${chatStore.currentFriend.friendId}`)
    ElMessage.success('已删除好友')
    chatStore.setCurrentFriend(null)
    showFriendDetail.value = false
    fetchFriends()
  } catch (err) {
    if (err !== 'cancel' && err.msg) {
      ElMessage.error(err.msg)
    }
  }
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
      currentUserAvatar.value = res.data.avatar || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }

  // 获取好友列表
  await fetchFriends()

  // 获取好友申请
  fetchReceivedRequests()

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
  width: 280px;
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

.request-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fdf6ec;
  color: #e6a23c;
  cursor: pointer;
  transition: background 0.2s;
}

.request-notice:hover {
  background: #faecd8;
}

.request-notice span {
  flex: 1;
  font-size: 13px;
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
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
}

.chat-title {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
}

.more-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.more-icon:hover {
  background: #f5f7fa;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 70%;
}

.message-item.mine {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.msg-avatar {
  flex-shrink: 0;
}

.msg-body {
  display: flex;
  flex-direction: column;
}

.message-item.mine .msg-body {
  align-items: flex-end;
}

.message-content {
  padding: 10px 14px;
  border-radius: 12px;
  word-break: break-word;
  line-height: 1.5;
}

.message-item.mine .message-content {
  background: #409eff;
  color: #fff;
}

.message-item:not(.mine) .message-content {
  background: #fff;
  color: #303133;
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
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

.empty-chat {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 弹窗样式 */
.request-list {
  max-height: 300px;
  overflow-y: auto;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.request-info {
  flex: 1;
}

.request-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.request-time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.request-actions {
  display: flex;
  gap: 8px;
}

/* 好友详情弹窗 */
.friend-detail {
  text-align: center;
}

.detail-avatar {
  margin-bottom: 20px;
}

.detail-info {
  text-align: left;
}

.detail-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-item .label {
  width: 70px;
  color: #909399;
  font-size: 14px;
}

.detail-item .value {
  flex: 1;
  color: #303133;
  font-size: 14px;
}
</style>
