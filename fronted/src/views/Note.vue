<template>
  <div class="note-container" @click="hideContextMenu">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">SmartNote</div>
      <el-dropdown trigger="click">
        <div class="user-info">
          <el-avatar :size="36" :src="getAvatarUrl(userInfo.avatar)"/>
          <span class="username">{{ userInfo.username }}</span>
          <el-icon>
            <ArrowDown/>
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :icon="User" @click="showProfileDialog">个人信息</el-dropdown-item>
            <el-dropdown-item :icon="Lock" @click="showPasswordDialog">修改密码</el-dropdown-item>
            <el-dropdown-item divided :icon="SwitchButton" @click="handleLogout">退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <!-- AI 助手入口 -->
    <div class="ai-bar">
      <div class="ai-input-wrapper" @click="openAiAssistant">
        <el-icon class="ai-icon"><Promotion /></el-icon>
        <span class="ai-placeholder">AI 助手 - 有问题尽管问我</span>
      </div>
    </div>

    <div class="main">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="note">
            <el-icon>
              <Notebook/>
            </el-icon>
            <span>笔记</span>
          </el-menu-item>
          <el-menu-item index="friends">
            <el-icon>
              <User/>
            </el-icon>
            <span>好友</span>
          </el-menu-item>
        </el-menu>

        <!-- 底部图标 -->
        <div class="sidebar-footer">
          <div class="footer-item" :class="{ active: activeMenu === 'history' }" @click="handleFooterMenuSelect('history')">
            <el-icon><Clock/></el-icon>
            <span>历史</span>
          </div>
          <div class="footer-item" :class="{ active: activeMenu === 'trash' }" @click="handleFooterMenuSelect('trash')">
            <el-icon><Delete/></el-icon>
            <span>回收站</span>
          </div>
        </div>

        <!-- 笔记列表 -->
        <div v-if="activeMenu === 'note'" class="note-list">
          <div class="list-header">
            <span>我的笔记</span>
            <el-dropdown trigger="click" @command="handleCreateCommand">
              <el-button type="primary" :icon="Plus" circle size="small"/>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="note">新建笔记</el-dropdown-item>
                  <el-dropdown-item command="folder">新建文件夹</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索笔记、文件夹或标签"
              clearable
              @input="handleSearch"
              @clear="handleClearSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
          <!-- 搜索结果列表（扁平显示） -->
          <div v-if="isSearching && searchResults.length > 0" class="search-results">
            <div
              v-for="item in searchResults"
              :key="item.id"
              class="search-result-item"
              @click="handleSearchResultClick(item)"
            >
              <el-icon v-if="item.type === 'FOLDER'" class="folder-icon">
                <Folder/>
              </el-icon>
              <el-icon v-else class="note-icon">
                <Document/>
              </el-icon>
              <span class="result-name">{{ item.name }}</span>
              <span v-if="item.tags" class="result-tags">{{ item.tags }}</span>
            </div>
          </div>
          <el-empty v-else-if="isSearching && searchResults.length === 0" description="未找到匹配结果" :image-size="60"/>
          <!-- 正常树形列表 -->
          <el-tree
            v-else
            ref="noteTreeRef"
            :data="noteTreeData"
            :props="treeProps"
            node-key="id"
            lazy
            :load="loadNode"
            @node-click="handleNodeClick"
            @node-contextmenu="handleContextMenu"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <el-icon v-if="data.type === 'FOLDER'" class="folder-icon">
                  <Folder/>
                </el-icon>
                <el-icon v-else class="note-icon">
                  <Document/>
                </el-icon>
                <span>{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 浏览历史列表 -->
        <div v-else-if="activeMenu === 'history'" class="note-list">
          <div class="list-header">
            <span>浏览历史</span>
            <el-button type="danger" text size="small" @click="handleClearHistory">清空</el-button>
          </div>
          <el-empty v-if="historyList.length === 0" description="暂无浏览记录"/>
          <div v-else class="history-list">
            <div
              v-for="item in historyList"
              :key="item.id"
              class="history-item"
              @click="handleHistoryClick(item)"
            >
              <div class="history-title">{{ item.noteTitle }}</div>
              <div class="history-info">
                <span>浏览 {{ item.viewCount }} 次</span>
                <span>{{ formatHistoryTime(item.browsedAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 回收站列表 -->
        <div v-else class="note-list">
          <div class="list-header">
            <span>回收站</span>
          </div>
          <el-tree
            ref="trashTreeRef"
            :data="trashTreeData"
            :props="treeProps"
            node-key="id"
            lazy
            :load="loadTrashNode"
            @node-click="handleTrashNodeClick"
            @node-contextmenu="handleTrashContextMenu"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <el-icon v-if="data.type === 'FOLDER'" class="folder-icon">
                  <Folder/>
                </el-icon>
                <el-icon v-else class="note-icon">
                  <Document/>
                </el-icon>
                <span>{{ data.name }}</span>
              </div>
            </template>
          </el-tree>
        </div>
      </aside>

      <!-- 右侧编辑区域 -->
      <main class="content">
        <div v-if="currentNote" class="editor">
          <div class="editor-header">
            <el-input v-model="currentNote.title" class="title-input" placeholder="请输入标题"/>
            <div class="editor-actions">
              <el-button type="primary" text size="small" @click="handleAiAnalyze"
                         :loading="aiLoading">AI分析
              </el-button>
              <el-button type="primary" text size="small" @click="handleCopyLink">复制链接
              </el-button>
              <el-button type="primary" :icon="Check" @click="handleSaveNote">保存</el-button>
              <span v-if="autoSaveStatus === 'saving'" class="auto-save-status">保存中...</span>
              <span v-else-if="autoSaveStatus === 'saved'" class="auto-save-status saved">已保存</span>
            </div>
          </div>
          <div class="editor-tags">
            <el-select
              v-model="currentNote.tags"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="添加标签"
              style="width: 100%"
            />
          </div>
          <div class="editor-visibility">
            <span class="visibility-label">可见性：</span>
            <el-select v-model="currentNote.visibility" size="small" style="width: 130px" @change="handleVisibilityChange">
              <el-option label="私有" value="PRIVATE"/>
              <el-option label="部分好友可见" value="FRIENDS"/>
              <el-option label="公开" value="PUBLIC"/>
            </el-select>
            <el-button v-if="currentNote.visibility === 'FRIENDS'" type="primary" text size="small" @click="openShareDialog">
              <el-icon><User/></el-icon>
              设置好友权限
            </el-button>
          </div>
          <MilkdownEditor
            v-model="currentNote.content"
            placeholder="开始编写你的笔记..."
            class="md-editor"
          />
        </div>
        <div v-else class="empty-content">
          <el-empty description="选择一个笔记开始编辑"/>
        </div>
      </main>
    </div>

    <!-- 右键菜单 - 文件夹菜单 -->
    <div
      v-if="contextMenuVisible && contextMenuData && contextMenuData.type === 'FOLDER' && !isTrashContext"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="handleContextCreateNote">新建笔记</div>
      <div class="menu-item" @click="handleContextCreateFolder">新建文件夹</div>
      <div class="menu-divider"></div>
      <div class="menu-item" @click="handleContextRename">重命名</div>
      <div class="menu-item danger" @click="handleContextDelete">删除</div>
    </div>

    <!-- 右键菜单 - 笔记菜单 -->
    <div
      v-if="contextMenuVisible && contextMenuData && contextMenuData.type === 'NOTE' && !isTrashContext"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="handleContextRename">重命名</div>
      <div class="menu-item danger" @click="handleContextDelete">删除</div>
    </div>

    <!-- 右键菜单 - 回收站菜单 -->
    <div
      v-if="contextMenuVisible && contextMenuData && isTrashContext"
      class="context-menu"
      :style="{ left: contextMenuX + 'px', top: contextMenuY + 'px' }"
      @click.stop
    >
      <div class="menu-item" @click="handleContextRestore">恢复</div>
      <div v-if="contextMenuData.type === 'NOTE'" class="menu-item danger"
           @click="handleContextPermanentDelete">永久删除
      </div>
    </div>

    <!-- 新建笔记弹窗 -->
    <el-dialog v-model="createNoteDialogVisible" title="新建笔记" width="400px">
      <el-form :model="createNoteForm">
        <el-form-item label="标题">
          <el-input v-model="createNoteForm.title" placeholder="请输入笔记标题"/>
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="createNoteForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入标签后回车"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createNoteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateNote">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新建文件夹弹窗 -->
    <el-dialog v-model="createFolderDialogVisible" title="新建文件夹" width="400px">
      <el-form :model="createFolderForm">
        <el-form-item label="文件夹名称">
          <el-input v-model="createFolderForm.name" placeholder="请输入文件夹名称"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createFolderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateFolder">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="renameForm.name" placeholder="请输入新名称"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRename">确定</el-button>
      </template>
    </el-dialog>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="profileDialogVisible" title="个人信息" width="450px">
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="头像">
          <div class="avatar-upload" @click="triggerAvatarUpload">
            <el-avatar :size="80" :src="getAvatarUrl(profileForm.avatar)"/>
            <div class="avatar-overlay">
              <span>点击更换</span>
            </div>
          </div>
          <input ref="avatarInputRef" type="file" accept="image/*" style="display: none" @change="handleAvatarChange"/>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item label="座右铭">
          <el-input v-model="profileForm.motto" type="textarea" :rows="3"
                    placeholder="请输入座右铭"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password/>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password/>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdatePassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分享弹窗 -->
    <el-dialog v-model="showShareDialog" title="设置好友权限" width="500px">
      <div class="share-dialog">
        <el-empty v-if="friends.length === 0" description="暂无好友，快去添加好友吧"/>
        <div v-else class="share-friend-list">
          <div v-for="friend in friends" :key="friend.id" class="share-friend-item">
            <div class="friend-info">
              <el-avatar :size="36" :src="getAvatarUrl(friend.friendAvatar)"/>
              <div class="friend-detail">
                <span class="friend-name">{{ friend.friendName }}</span>
                <span class="friend-motto">{{ friend.friendMotto || '这个人很懒，什么都没写' }}</span>
              </div>
            </div>
            <el-select
              v-model="sharePermissions[friend.friendId]"
              size="small"
              placeholder="未授权"
              clearable
              style="width: 100px"
            >
              <el-option label="只读" :value="false"/>
              <el-option label="可编辑" :value="true"/>
            </el-select>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showShareDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveShare">保存</el-button>
      </template>
    </el-dialog>

    <!-- AI 分析结果弹窗 -->
    <el-dialog v-model="aiDialogVisible" title="AI 分析" width="600px">
      <div v-if="aiLoading" class="ai-loading">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span>AI 正在分析中...</span>
      </div>
      <div v-else-if="aiResult" class="ai-result">
        <MdPreview :model-value="aiResult" />
      </div>
      <div v-else class="ai-empty">
        <el-empty description="暂无 AI 分析记录" :image-size="80" />
        <el-button type="primary" @click="handleGenerateAiSummary" :loading="aiGenerating">
          生成 AI 总结
        </el-button>
      </div>
      <template #footer>
        <el-button @click="aiDialogVisible = false">关闭</el-button>
        <el-button v-if="aiResult && !aiLoading" type="primary" @click="handleGenerateAiSummary" :loading="aiGenerating">
          重新生成
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 助手弹窗 -->
    <el-dialog v-model="aiAssistantVisible" title="AI 助手" width="600px" class="ai-assistant-dialog">
      <div class="ai-assistant-content">
        <div class="ai-chat-header">
          <el-button type="primary" text size="small" @click="startNewConversation" :disabled="aiStreaming">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
        <div class="ai-chat-messages">
          <div v-if="aiMessages.length === 0" class="ai-welcome">
            <el-icon :size="48" color="#409eff"><Promotion /></el-icon>
            <p>你好！我是 AI 助手，有什么可以帮助你的吗？</p>
          </div>
          <div v-for="(msg, index) in aiMessages" :key="index" class="ai-message" :class="msg.role">
            <!-- 流式输出中用普通文本，完成后用 MdPreview -->
            <div v-if="msg.streaming" class="message-content streaming">{{ msg.content }}</div>
            <MdPreview v-else :model-value="msg.content" class="message-content" />
          </div>
        </div>
        <div class="ai-input-area">
          <el-input
            v-model="aiInput"
            placeholder="输入你的问题..."
            @keyup.enter="sendAiMessage"
            :disabled="aiStreaming"
          >
            <template #append>
              <el-button :icon="Promotion" @click="sendAiMessage" :loading="aiStreaming" />
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {del, get, post, put} from '@/utils/request.js'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  ArrowDown,
  Check,
  Clock,
  Delete,
  Document,
  Folder,
  Loading,
  Lock,
  Notebook,
  Plus,
  Promotion,
  Search,
  SwitchButton,
  User
} from '@element-plus/icons-vue'
import MilkdownEditor from '@/components/MilkdownEditor.vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const router = useRouter()
const route = useRoute()

// 用户信息
const userInfo = ref({
  username: '',
  motto: '',
  avatar: ''
})

// 树引用
const noteTreeRef = ref(null)
const trashTreeRef = ref(null)

// 当前选中的菜单
const activeMenu = ref('note')

// 当前选中的笔记
const currentNote = ref(null)

// 自动保存状态
const autoSaveStatus = ref('') // '' | 'saving' | 'saved'

// 树形结构配置
const treeProps = {
  label: 'name',
  isLeaf: (data) => data.type === 'NOTE'
}

// 主页面笔记树数据
const noteTreeData = ref([])

// 回收站树数据
const trashTreeData = ref([])

// 浏览历史列表
const historyList = ref([])

// 搜索相关
const searchKeyword = ref('')
const searchResults = ref([])
const isSearching = ref(false)
let searchTimer = null

// 新建笔记弹窗
const createNoteDialogVisible = ref(false)
const createNoteForm = ref({
  title: '',
  folderId: null,
  tags: []
})

// 新建文件夹弹窗
const createFolderDialogVisible = ref(false)
const createFolderForm = ref({
  name: '',
  parentId: null
})

// 重命名弹窗
const renameDialogVisible = ref(false)
const renameForm = ref({
  name: ''
})

// 个人信息弹窗
const profileDialogVisible = ref(false)
const profileForm = ref({
  username: '',
  motto: '',
  avatar: ''
})
const avatarInputRef = ref(null)

// 修改密码弹窗
const passwordDialogVisible = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 分享弹窗
const showShareDialog = ref(false)
const friends = ref([])
const sharePermissions = ref({})
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 获取头像完整URL
const getAvatarUrl = (avatar) => {
  if (!avatar) return defaultAvatar
  // 如果已经是完整URL，直接返回
  if (avatar.startsWith('http')) return avatar
  // 否则拼接后端地址
  return 'http://localhost:8081/' + avatar
}

// AI 分析
const aiLoading = ref(false)
const aiGenerating = ref(false)
const aiResult = ref('')
const aiDialogVisible = ref(false)

// AI 助手
const aiAssistantVisible = ref(false)
const aiInput = ref('')
const aiMessages = ref([])
const aiStreaming = ref(false)  // 是否正在流式输出
const aiConversationId = ref('')  // 会话ID，用于上下文记忆

// 打开 AI 助手
const openAiAssistant = () => {
  aiAssistantVisible.value = true
}

// 新建对话
const startNewConversation = () => {
  aiConversationId.value = crypto.randomUUID()
  aiMessages.value = []
}

// 发送消息给 AI（非流式）
const sendAiMessage = async () => {
  if (!aiInput.value.trim() || aiStreaming.value) return

  // 如果没有会话ID，生成一个
  if (!aiConversationId.value) {
    aiConversationId.value = crypto.randomUUID()
  }

  const userInput = aiInput.value
  aiInput.value = ''

  // 添加用户消息
  aiMessages.value.push({
    role: 'user',
    content: userInput
  })

  // 添加一个加载中的助手消息
  aiMessages.value.push({
    role: 'assistant',
    content: '',
    streaming: true
  })
  const assistantIndex = aiMessages.value.length - 1

  aiStreaming.value = true

  try {
    const response = await fetch('http://localhost:8081/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'satoken': localStorage.getItem('token')
      },
      body: JSON.stringify({
        message: userInput,
        conversationId: aiConversationId.value
      })
    })

    if (!response.ok) {
      throw new Error('请求失败')
    }

    // 非流式响应，直接读取完整内容
    const data = await response.json()

    // 更新助手消息
    aiMessages.value[assistantIndex].content = data.data || '抱歉，AI 响应异常。'
    aiMessages.value[assistantIndex].streaming = false
  } catch (err) {
    aiMessages.value[assistantIndex].content = '抱歉，发生了错误，请稍后重试。'
    aiMessages.value[assistantIndex].streaming = false
    console.error('AI 聊天错误:', err)
  } finally {
    aiStreaming.value = false
  }
}

// 右键菜单
const contextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuData = ref(null)
const isTrashContext = ref(false)

// 获取根目录节点
const fetchRootNodes = async () => {
  try {
    const res = await get('/nodes/root')
    if (res.data) {
      noteTreeData.value = res.data
    }
  } catch (err) {
    console.error('获取笔记失败:', err)
  }
}

// 获取回收站根目录节点
const fetchTrashRootNodes = async () => {
  try {
    const res = await get('/nodes/trash/root')
    if (res.data) {
      trashTreeData.value = res.data
    }
  } catch (err) {
    console.error('获取回收站失败:', err)
  }
}

// 获取浏览历史
const fetchHistoryList = async () => {
  try {
    const res = await get('/browse-history', {limit: 20})
    historyList.value = res.data || []
  } catch (err) {
    console.error('获取浏览历史失败:', err)
  }
}

// 搜索处理（防抖）
const handleSearch = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(async () => {
    if (!searchKeyword.value.trim()) {
      isSearching.value = false
      searchResults.value = []
      return
    }
    try {
      const res = await get('/nodes/search', { keyword: searchKeyword.value.trim() })
      searchResults.value = res.data || []
      isSearching.value = true
    } catch (err) {
      console.error('搜索失败:', err)
      searchResults.value = []
    }
  }, 300)
}

// 清除搜索
const handleClearSearch = () => {
  searchKeyword.value = ''
  searchResults.value = []
  isSearching.value = false
}

// 点击搜索结果
const handleSearchResultClick = (item) => {
  if (item.type === 'NOTE') {
    router.push(`/note/${item.id}`)
  } else {
    // 文件夹：展开到该文件夹
    ElMessage.info('请从左侧目录树进入文件夹')
  }
}

// 懒加载子节点
const loadNode = async (node, resolve) => {
  if (node.level === 0) {
    resolve(noteTreeData.value)
    return
  }
  if (node.data.type === 'FOLDER') {
    try {
      const res = await get(`/nodes/${node.data.id}`)
      resolve(res.data || [])
    } catch (err) {
      resolve([])
    }
  } else {
    resolve([])
  }
}

// 懒加载回收站子节点
const loadTrashNode = async (node, resolve) => {
  if (node.level === 0) {
    resolve(trashTreeData.value)
    return
  }
  if (node.data.type === 'FOLDER') {
    try {
      const res = await get(`/nodes/trash/${node.data.id}`)
      resolve(res.data || [])
    } catch (err) {
      resolve([])
    }
  } else {
    resolve([])
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchRootNodes()
  fetchUserInfo()
})

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await get('/users/info')
    if (res.data) {
      userInfo.value = {
        username: res.data.username,
        motto: res.data.motto || '',
        avatar: res.data.avatar || ''
      }
    }
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}

// 隐藏右键菜单
const hideContextMenu = () => {
  contextMenuVisible.value = false
}

// 菜单切换
const handleMenuSelect = (index) => {
  if (index === 'friends') {
    router.push('/chat')
    return
  }
  activeMenu.value = index
  currentNote.value = null
  if (index === 'trash') {
    fetchTrashRootNodes()
  } else if (index === 'history') {
    fetchHistoryList()
  } else {
    fetchRootNodes()
  }
}

// 底部菜单切换
const handleFooterMenuSelect = (index) => {
  activeMenu.value = index
  currentNote.value = null
  if (index === 'trash') {
    fetchTrashRootNodes()
  } else if (index === 'history') {
    fetchHistoryList()
  }
}

// 点击树节点
const handleNodeClick = async (data, node) => {
  if (data.type === 'NOTE') {
    router.push(`/note/${data.id}`)
  }
}

// 加载笔记详情
const loadNoteDetail = async (noteId) => {
  try {
    const res = await get(`/notes/${noteId}`)
    currentNote.value = {
      id: noteId,
      title: res.data.title,
      content: res.data.content || '',
      tags: res.data.tags ? res.data.tags.split(',').filter(t => t) : [],
      visibility: res.data.visibility || 'PRIVATE',
      canEdit: res.data.canEdit !== false
    }
  } catch (err) {
    ElMessage.error(err.msg || '获取笔记详情失败')
    router.push('/note')
  }
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId && route.name === 'NoteDetail') {
    loadNoteDetail(newId)
  }
}, {immediate: true})

// 点击回收站节点
const handleTrashNodeClick = (data) => {
  if (data.type === 'NOTE') {
    ElMessage.info('回收站中的笔记不可编辑，请先恢复')
  }
}

// 右键菜单 - 主页面
const handleContextMenu = (event, data, node) => {
  event.preventDefault()
  event.stopPropagation()
  contextMenuData.value = data
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  isTrashContext.value = false
  contextMenuVisible.value = true
}

// 右键菜单 - 回收站
const handleTrashContextMenu = (event, data, node) => {
  event.preventDefault()
  event.stopPropagation()
  contextMenuData.value = data
  contextMenuX.value = event.clientX
  contextMenuY.value = event.clientY
  isTrashContext.value = true
  contextMenuVisible.value = true
}

// 新建命令处理
const handleCreateCommand = (command) => {
  if (command === 'note') {
    createNoteForm.value = {title: '', folderId: null, tags: []}
    createNoteDialogVisible.value = true
  } else if (command === 'folder') {
    createFolderForm.value = {name: '', parentId: null}
    createFolderDialogVisible.value = true
  }
}

// 右键 - 重命名
const handleContextRename = () => {
  hideContextMenu()
  renameForm.value = {name: contextMenuData.value.name}
  renameDialogVisible.value = true
}

// 右键 - 删除
const handleContextDelete = async () => {
  hideContextMenu()
  const data = contextMenuData.value
  try {
    await ElMessageBox.confirm(`确定要删除该${data.type === 'NOTE' ? '笔记' : '文件夹'}吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    if (data.type === 'NOTE') {
      await del(`/notes/${data.id}`)
    } else {
      await del(`/folders/${data.id}`)
    }
    ElMessage.success('已移入回收站')
    fetchRootNodes()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 右键 - 在文件夹中新建笔记
const handleContextCreateNote = () => {
  hideContextMenu()
  createNoteForm.value = {title: '', folderId: contextMenuData.value.id, tags: []}
  createNoteDialogVisible.value = true
}

// 右键 - 在文件夹中新建文件夹
const handleContextCreateFolder = () => {
  hideContextMenu()
  createFolderForm.value = {name: '', parentId: contextMenuData.value.id}
  createFolderDialogVisible.value = true
}

// 右键 - 恢复
const handleContextRestore = async () => {
  hideContextMenu()
  const data = contextMenuData.value
  try {
    if (data.type === 'NOTE') {
      await post(`/notes/${data.id}/restore`)
    } else {
      await post(`/folders/${data.id}/restore`)
    }
    ElMessage.success('恢复成功')
    fetchTrashRootNodes()
  } catch (err) {
    ElMessage.error('恢复失败')
  }
}

// 右键 - 永久删除
const handleContextPermanentDelete = async () => {
  hideContextMenu()
  try {
    await ElMessageBox.confirm('永久删除后无法恢复，确定要删除吗？', '警告', {
      type: 'error',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await del(`/notes/${contextMenuData.value.id}/permanent`)
    ElMessage.success('已永久删除')
    fetchTrashRootNodes()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 执行重命名
const handleRename = async () => {
  if (!renameForm.value.name.trim()) {
    ElMessage.warning('名称不能为空')
    return
  }
  const data = contextMenuData.value
  try {
    if (data.type === 'NOTE') {
      await put(`/notes/${data.id}`, {title: renameForm.value.name})
    } else {
      await put(`/folders/${data.id}`, {name: renameForm.value.name})
    }
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    fetchRootNodes()
  } catch (err) {
    ElMessage.error('重命名失败')
  }
}

// 新建笔记
const handleCreateNote = async () => {
  if (!createNoteForm.value.title) {
    ElMessage.warning('请输入笔记标题')
    return
  }
  try {
    await post('/notes', {
      title: createNoteForm.value.title,
      folderId: createNoteForm.value.folderId || 0,
      tags: createNoteForm.value.tags.join(',')
    })
    ElMessage.success('创建成功')
    createNoteDialogVisible.value = false
    fetchRootNodes()
  } catch (err) {
    ElMessage.error('创建失败')
  }
}

// 新建文件夹
const handleCreateFolder = async () => {
  if (!createFolderForm.value.name) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  try {
    await post('/folders', {
      name: createFolderForm.value.name,
      parentId: createFolderForm.value.parentId || 0
    })
    ElMessage.success('创建成功')
    createFolderDialogVisible.value = false
    fetchRootNodes()
  } catch (err) {
    ElMessage.error('创建失败')
  }
}

// 保存笔记
const handleSaveNote = async () => {
  if (!currentNote.value.title) {
    ElMessage.warning('请输入笔记标题')
    return
  }
  try {
    await put(`/notes/${currentNote.value.id}`, {
      title: currentNote.value.title,
      content: currentNote.value.content,
      tags: currentNote.value.tags.join(',')
    })
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('保存失败')
  }
}

// 自动保存（防抖）
let autoSaveTimer = null
const autoSaveNote = () => {
  if (autoSaveTimer) clearTimeout(autoSaveTimer)
  autoSaveTimer = setTimeout(async () => {
    if (!currentNote.value?.id || !currentNote.value?.canEdit) return
    if (!currentNote.value.title) return

    autoSaveStatus.value = 'saving'
    try {
      await put(`/notes/${currentNote.value.id}`, {
        title: currentNote.value.title,
        content: currentNote.value.content,
        tags: currentNote.value.tags.join(',')
      })
      autoSaveStatus.value = 'saved'
      setTimeout(() => { autoSaveStatus.value = '' }, 2000)
    } catch (err) {
      autoSaveStatus.value = ''
    }
  }, 2000)
}

// 监听内容变化自动保存
watch(() => currentNote.value?.content, () => {
  if (currentNote.value?.canEdit) {
    autoSaveNote()
  }
})

// 修改可见性
const handleVisibilityChange = async (value) => {
  if (!currentNote.value) return
  try {
    await put(`/notes/${currentNote.value.id}/visibility?visibility=${value}`)
    ElMessage.success('修改成功！')
  } catch (err) {
    ElMessage.error(err.msg || '修改失败')
  }
}

// 复制笔记链接
const handleCopyLink = async () => {
  if (!currentNote.value) return
  const link = `${window.location.origin}/note/${currentNote.value.id}`
  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success('链接已复制')
  } catch (err) {
    ElMessage.error('复制失败')
  }
}

// AI 分析笔记 - 查询已有记录
const handleAiAnalyze = async () => {
  if (!currentNote.value) return
  aiLoading.value = true
  aiResult.value = ''
  aiDialogVisible.value = true
  try {
    const res = await get(`/ai/analyze/${currentNote.value.id}`)
    aiResult.value = res.data || ''
  } catch (err) {
    // 没有记录时保持 aiResult 为空
    aiResult.value = ''
  } finally {
    aiLoading.value = false
  }
}

// 生成 AI 总结
const handleGenerateAiSummary = async () => {
  if (!currentNote.value) return
  aiGenerating.value = true
  try {
    const res = await post(`/ai/analyze/${currentNote.value.id}`, {}, { timeout: 120000 })
    aiResult.value = res.data
    ElMessage.success('AI 分析完成')
  } catch (err) {
    ElMessage.error('AI 分析失败')
    console.log(err)
  } finally {
    aiGenerating.value = false
  }
}

// 显示个人信息弹窗
const showProfileDialog = () => {
  profileForm.value = {
    username: userInfo.value.username,
    motto: userInfo.value.motto,
    avatar: userInfo.value.avatar
  }
  profileDialogVisible.value = true
}

// 触发头像上传
const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

// 处理头像选择
const handleAvatarChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  // 校验文件类型
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'image/bmp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.error('只支持 jpg/jpeg/png/gif/webp/bmp 格式的图片')
    return
  }

  // 上传头像
  const formData = new FormData()
  formData.append('file', file)

  try {
    // 使用原生 fetch 上传文件
    const response = await fetch('http://localhost:8081/users/avatar', {
      method: 'POST',
      headers: {
        'satoken': localStorage.getItem('token')
      },
      body: formData
    })
    const res = await response.json()
    if (res.code === 200) {
      ElMessage.success('头像修改成功')
      // 重新获取用户信息以更新头像
      await fetchUserInfo()
      // 更新弹窗中的头像
      profileForm.value.avatar = userInfo.value.avatar
    } else {
      ElMessage.error(res.msg || '头像上传失败')
    }
  } catch (err) {
    ElMessage.error('头像上传失败')
  }

  // 清空 input，允许重复选择同一文件
  event.target.value = ''
}

// 显示修改密码弹窗
const showPasswordDialog = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

// 更新个人信息
const handleUpdateProfile = async () => {
  try {
    if (profileForm.value.username && profileForm.value.username !== userInfo.value.username) {
      await put('/users/username', {username: profileForm.value.username})
    }
    if (profileForm.value.motto !== userInfo.value.motto) {
      await put('/users/motto', {motto: profileForm.value.motto})
    }
    ElMessage.success('修改成功')
    profileDialogVisible.value = false
    fetchUserInfo()
  } catch (err) {
    ElMessage.error('修改失败')
  }
}

// 修改密码
const handleUpdatePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  try {
    await put('/users/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (err) {
    ElMessage.error(err.msg || '修改失败')
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

// 获取笔记权限
const fetchPermissions = async (noteId) => {
  try {
    const res = await get(`/notes/${noteId}/permissions`)
    const permissions = {}
    res.data.forEach(p => {
      permissions[p.userId] = p.canEdit === 1
    })
    sharePermissions.value = permissions
  } catch (err) {
    console.error('获取权限失败:', err)
  }
}

// 保存分享
const handleSaveShare = async () => {
  if (!currentNote.value) return

  try {
    // 删除所有旧权限
    for (const friend of friends.value) {
      await del(`/notes/${currentNote.value.id}/permissions/${friend.friendId}`)
    }

    // 添加新权限
    for (const [userId, canEdit] of Object.entries(sharePermissions.value)) {
      if (canEdit !== undefined && canEdit !== null) {
        await post(`/notes/${currentNote.value.id}/permissions`, {
          userId: Number(userId),
          canEdit: canEdit
        })
      }
    }

    ElMessage.success('分享设置已保存')
    showShareDialog.value = false
  } catch (err) {
    ElMessage.error('保存失败')
  }
}

// 打开分享弹窗
const openShareDialog = async () => {
  await fetchFriends()
  await fetchPermissions(currentNote.value.id)
  showShareDialog.value = true
}

// 点击浏览历史项
const handleHistoryClick = async (item) => {
  activeMenu.value = 'note'
  router.push(`/note/${item.noteId}`)
}

// 清空浏览历史
const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有浏览历史吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await del('/browse-history')
    ElMessage.success('清空成功')
    historyList.value = []
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('清空失败')
    }
  }
}

// 格式化浏览历史时间
const formatHistoryTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

// 退出登录
const handleLogout = async () => {
  try {
    await get('/users/logout')
  } catch (err) {
    // 接口失败不影响退出
  } finally {
    localStorage.removeItem('token')
    ElMessage.success('退出成功')
    router.push('/')
  }
}
</script>

<style scoped>
.note-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
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
  letter-spacing: 1px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 6px;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
}

/* AI 助手入口栏 */
.ai-bar {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 10px 20px;
}

.ai-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #f0f7ff 0%, #e8f4ff 100%);
  border: 1px solid #d9ecff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.ai-input-wrapper:hover {
  background: linear-gradient(135deg, #e8f4ff 0%, #d9ecff 100%);
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.ai-icon {
  font-size: 20px;
  color: #409eff;
}

.ai-placeholder {
  font-size: 14px;
  color: #606266;
}

/* 主体区域 */
.main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧边栏 */
.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  position: relative;
}

.sidebar-footer {
  position: absolute;
  bottom: 10px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 10px;
  padding: 10px;
  border-top: 1px solid #e4e7ed;
}

.footer-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  color: #909399;
  font-size: 12px;
  transition: all 0.3s;
}

.footer-item .el-icon {
  font-size: 20px;
}

.footer-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.footer-item.active {
  color: #409eff;
  background: #ecf5ff;
}

.el-menu {
  border-right: none;
}

.note-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border-top: 1px solid #e4e7ed;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 5px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.search-box {
  margin-bottom: 10px;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.search-result-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.search-result-item:hover {
  background: #f5f7fa;
}

.result-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-tags {
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  flex: 1;
}

.folder-icon {
  color: #e6a23c;
}

.note-icon {
  color: #409eff;
}

/* 浏览历史样式 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.history-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover {
  background: #e6e8eb;
}

.history-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* 头像上传样式 */
.avatar-upload {
  position: relative;
  cursor: pointer;
  display: inline-block;
}

.avatar-upload:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-overlay span {
  color: #fff;
  font-size: 12px;
}

/* 右侧内容区 */
.content {
  flex: 1;
  background: #f5f7fa;
  overflow-y: auto;
}

.editor {
  height: 100%;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.editor-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
}

.title-input {
  flex: 1;
  margin-right: 20px;
}

.title-input :deep(.el-input__inner) {
  font-size: 20px;
  font-weight: 600;
  border: none;
  padding: 0;
}

.title-input :deep(.el-input__wrapper) {
  box-shadow: none;
}

.auto-save-status {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.auto-save-status.saved {
  color: #67c23a;
}

.editor-tags {
  margin-bottom: 12px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
}

.editor-visibility {
  margin-bottom: 12px;
  background: #fff;
  padding: 12px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.visibility-label {
  font-size: 14px;
  color: #606266;
}

/* 分享弹窗样式 */
.share-dialog {
  max-height: 400px;
  overflow-y: auto;
}

.share-friend-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.share-friend-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: background 0.3s;
}

.share-friend-item:hover {
  background: #eef1f6;
}

.share-friend-item .friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.share-friend-item .friend-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.share-friend-item .friend-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.share-friend-item .friend-motto {
  font-size: 12px;
  color: #909399;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.md-editor {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
}

.empty-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 右键菜单样式 */
.context-menu {
  position: fixed;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 5px 0;
  min-width: 120px;
  z-index: 9999;
}

.menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
}

.menu-item:hover {
  background: #f5f7fa;
}

.menu-item.danger {
  color: #f56c6c;
}

.menu-item.danger:hover {
  background: #fef0f0;
}

.menu-divider {
  height: 1px;
  background: #e4e7ed;
  margin: 5px 0;
}

.ai-result {
  max-height: 400px;
  overflow-y: auto;
}

.ai-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 16px;
  color: #409eff;
}

.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

/* AI 助手弹窗 */
.ai-assistant-content {
  display: flex;
  flex-direction: column;
  height: 450px;
}

.ai-chat-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.ai-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.ai-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.ai-welcome p {
  margin-top: 16px;
  font-size: 14px;
}

.ai-message {
  margin-bottom: 12px;
}

.ai-message.user {
  text-align: right;
}

.ai-message.user .message-content {
  background: #409eff;
  color: #fff;
}

.ai-message.assistant .message-content {
  background: #fff;
  color: #303133;
}

.message-content {
  display: inline-block;
  padding: 10px 14px;
  border-radius: 8px;
  max-width: 80%;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.5;
  text-align: left;
}

/* 流式输出样式 */
.message-content.streaming {
  white-space: pre-wrap;
}

/* MdPreview 样式覆盖 */
.ai-message.assistant .message-content :deep(.md-preview) {
  background: transparent;
  padding: 0;
}

.ai-input-area {
  flex-shrink: 0;
}
</style>
