<template>
  <div class="friends-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">SmartNote</div>
      <div class="nav-btns">
        <el-button type="primary" @click="router.push('/chat')">聊天</el-button>
        <el-button @click="router.push('/note')">返回笔记</el-button>
      </div>
    </header>

    <div class="main">
      <!-- 左侧标签页 -->
      <aside class="sidebar">
        <el-menu :default-active="activeTab" @select="handleTabSelect">
          <el-menu-item index="friends">
            <el-icon><User /></el-icon>
            <span>好友列表</span>
          </el-menu-item>
          <el-menu-item index="requests">
            <el-icon><Bell /></el-icon>
            <span>好友申请</span>
            <el-badge v-if="pendingCount > 0" :value="pendingCount" class="badge" />
          </el-menu-item>
        </el-menu>

        <!-- 添加好友按钮 -->
        <div class="add-friend-btn">
          <el-button type="primary" :icon="Plus" @click="showAddDialog = true">添加好友</el-button>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <main class="content">
        <!-- 好友列表 -->
        <div v-if="activeTab === 'friends'" class="friend-list">
          <div class="list-header">
            <span>我的好友 ({{ friends.length }})</span>
          </div>
          <el-empty v-if="friends.length === 0" description="暂无好友" />
          <div v-else class="friend-cards">
            <div v-for="friend in friends" :key="friend.id" class="friend-card">
              <el-avatar :size="50" :src="friend.friendAvatar || defaultAvatar" />
              <div class="friend-info">
                <div class="friend-name">{{ friend.friendName }}</div>
                <div class="friend-motto">{{ friend.friendMotto || '这个人很懒，什么都没写' }}</div>
              </div>
              <el-button type="danger" text @click="handleDeleteFriend(friend)">删除</el-button>
            </div>
          </div>
        </div>

        <!-- 好友申请 -->
        <div v-else class="request-list">
          <!-- 收到的申请 -->
          <div class="section">
            <div class="list-header">
              <span>收到的申请</span>
            </div>
            <el-empty v-if="receivedRequests.length === 0" description="暂无申请" />
            <div v-else class="request-cards">
              <div v-for="request in receivedRequests" :key="request.id" class="request-card">
                <el-avatar :size="50" :src="request.requesterAvatar || defaultAvatar" />
                <div class="request-info">
                  <div class="request-name">{{ request.requesterName }}</div>
                  <div class="request-time">{{ formatTime(request.createdAt) }}</div>
                </div>
                <div class="request-actions">
                  <el-button type="primary" size="small" @click="handleRequest(request.id, true)">同意</el-button>
                  <el-button size="small" @click="handleRequest(request.id, false)">拒绝</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 发出的申请 -->
          <div class="section">
            <div class="list-header">
              <span>发出的申请</span>
            </div>
            <el-empty v-if="sentRequests.length === 0" description="暂无申请" />
            <div v-else class="request-cards">
              <div v-for="request in sentRequests" :key="request.id" class="request-card">
                <div class="request-info">
                  <div class="request-name">申请给: {{ getReceiverName(request) }}</div>
                  <div class="request-time">{{ formatTime(request.createdAt) }}</div>
                </div>
                <el-tag :type="getStatusType(request.status)">{{ getStatusText(request.status) }}</el-tag>
              </div>
            </div>
          </div>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, put, del } from '@/utils/request.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Bell, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 当前选中的标签
const activeTab = ref('friends')

// 好友列表
const friends = ref([])

// 收到的申请
const receivedRequests = ref([])

// 发出的申请
const sentRequests = ref([])

// 待处理的申请数量
const pendingCount = computed(() => receivedRequests.value.filter(r => r.status === 'PENDING').length)

// 添加好友弹窗
const showAddDialog = ref(false)
const addForm = ref({
  contact: ''
})

// 获取好友列表
const fetchFriends = async () => {
  try {
    const res = await get('/friends')
    friends.value = res.data || []
  } catch (err) {
    console.error('获取好友列表失败:', err)
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

// 获取发出的好友申请
const fetchSentRequests = async () => {
  try {
    const res = await get('/friend-requests/sent')
    sentRequests.value = res.data || []
  } catch (err) {
    console.error('获取申请列表失败:', err)
  }
}

// 标签切换
const handleTabSelect = (index) => {
  activeTab.value = index
}

// 添加好友
const handleAddFriend = async () => {
  if (!addForm.value.contact.trim()) {
    ElMessage.warning('请输入手机号或邮箱')
    return
  }
  try {
    await post('/friend-requests', { contact: addForm.value.contact })
    ElMessage.success('好友申请已发送')
    showAddDialog.value = false
    addForm.value.contact = ''
    fetchSentRequests()
  } catch (err) {
    ElMessage.error(err.msg || '发送失败')
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
    ElMessage.error(err.msg || '操作失败')
  }
}

// 删除好友
const handleDeleteFriend = async (friend) => {
  try {
    await ElMessageBox.confirm(`确定要删除好友 ${friend.friendName} 吗？`, '提示', { type: 'warning' })
    await del(`/friends/${friend.friendId}`)
    ElMessage.success('已删除好友')
    fetchFriends()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error(err.msg || '删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 获取接收者名称
const getReceiverName = (request) => {
  return request.receiverName || `用户${request.receiverId}`
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'ACCEPTED': return 'success'
    case 'REJECTED': return 'info'
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待处理'
    case 'ACCEPTED': return '已同意'
    case 'REJECTED': return '已拒绝'
    default: return status
  }
}

// 页面加载
onMounted(() => {
  fetchFriends()
  fetchReceivedRequests()
  fetchSentRequests()
})
</script>

<style scoped>
.friends-container {
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

.main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.sidebar {
  width: 200px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.el-menu {
  border-right: none;
}

.badge {
  margin-left: 8px;
}

.add-friend-btn {
  padding: 20px;
  margin-top: auto;
}

.content {
  flex: 1;
  background: #f5f7fa;
  overflow-y: auto;
  padding: 20px;
}

.list-header {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.section {
  margin-bottom: 30px;
}

.friend-cards, .request-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.friend-card, .request-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.friend-info, .request-info {
  flex: 1;
}

.friend-name, .request-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.friend-motto {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.request-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.request-actions {
  display: flex;
  gap: 8px;
}
</style>
