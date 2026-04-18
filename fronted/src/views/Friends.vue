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
            <div v-for="friend in friends" :key="friend.id" class="friend-card" @click="openFriendDetail(friend)">
              <el-avatar :size="50" :src="getAvatarUrl(friend.friendAvatar)" />
              <div class="friend-info">
                <div class="friend-name">{{ friend.friendName }}</div>
                <div class="friend-motto">{{ friend.friendMotto || '这个人很懒，什么都没写' }}</div>
              </div>
              <el-tag size="small" type="info">{{ friend.groupName || '默认' }}</el-tag>
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
                <el-avatar :size="50" :src="getAvatarUrl(request.requesterAvatar)" />
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

    <!-- 好友详情弹窗 -->
    <el-dialog v-model="showFriendDetail" title="好友详情" width="400px">
      <div v-if="currentFriend" class="friend-detail">
        <div class="detail-avatar">
          <el-avatar :size="80" :src="getAvatarUrl(currentFriend.friendAvatar)" />
        </div>
        <div class="detail-info">
          <div class="detail-item">
            <span class="label">用户名：</span>
            <span class="value">{{ currentFriend.friendName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">座右铭：</span>
            <span class="value">{{ currentFriend.friendMotto || '这个人很懒，什么都没写' }}</span>
          </div>
          <div class="detail-item">
            <span class="label">分组：</span>
            <span v-if="!editingGroup" class="value">{{ currentFriend.groupName || '默认' }}</span>
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
        <el-button type="danger" @click="handleDeleteFriend(currentFriend)">删除好友</el-button>
        <el-button @click="showFriendDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, put, del } from '@/utils/request.js'
import { getAvatarUrl } from '@/utils/common.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Bell, Plus } from '@element-plus/icons-vue'

const router = useRouter()

// 当前选中的标签
const activeTab = ref('friends')

// 好友列表
const friends = ref([])

// 收到的申请
const receivedRequests = ref([])

// 待处理的申请数量
const pendingCount = computed(() => receivedRequests.value.filter(r => r.status === 'PENDING').length)

// 添加好友弹窗
const showAddDialog = ref(false)
const addForm = ref({
  contact: ''
})

// 好友详情弹窗
const showFriendDetail = ref(false)
const currentFriend = ref(null)
const editingGroup = ref(false)
const newGroupName = ref('')

// 打开好友详情
const openFriendDetail = (friend) => {
  currentFriend.value = friend
  editingGroup.value = false
  newGroupName.value = ''
  showFriendDetail.value = true
}

// 开始编辑分组
const startEditGroup = () => {
  newGroupName.value = currentFriend.value.groupName || '默认'
  editingGroup.value = true
}

// 保存分组
const saveGroup = async () => {
  if (!newGroupName.value.trim()) {
    ElMessage.warning('分组名不能为空')
    return
  }
  try {
    await put(`/friends/group/${currentFriend.value.friendId}`, { group: newGroupName.value.trim() })
    ElMessage.success('分组修改成功')
    currentFriend.value.groupName = newGroupName.value.trim()
    editingGroup.value = false
    fetchFriends()
  } catch (err) {
    if (err.msg) ElMessage.error(err.msg)
  }
}

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

// 删除好友
const handleDeleteFriend = async (friend) => {
  try {
    await ElMessageBox.confirm(`确定要删除好友 ${friend.friendName} 吗？`, '提示', { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' })
    await del(`/friends/${friend.friendId}`)
    ElMessage.success('已删除好友')
    fetchFriends()
  } catch (err) {
    if (err !== 'cancel' && err.msg) {
      ElMessage.error(err.msg)
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 页面加载
onMounted(() => {
  fetchFriends()
  fetchReceivedRequests()
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

/* 好友卡片可点击 */
.friend-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.friend-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
