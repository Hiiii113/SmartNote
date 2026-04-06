<template>
  <div class="note-container">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="logo">SmartNote</div>
      <el-dropdown trigger="click">
        <div class="user-info">
          <el-avatar :size="36" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          <span class="username">SmartNote_12345678</span>
          <el-icon><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :icon="User">个人信息</el-dropdown-item>
            <el-dropdown-item :icon="Setting">设置</el-dropdown-item>
            <el-dropdown-item divided :icon="SwitchButton" @click="handleLogout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </header>

    <div class="main">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="note">
            <el-icon><Notebook /></el-icon>
            <span>笔记</span>
          </el-menu-item>
          <el-menu-item index="trash">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </el-menu-item>
        </el-menu>

        <!-- 笔记列表 -->
        <div v-if="activeMenu === 'note'" class="note-list">
          <div class="list-header">
            <span>我的笔记</span>
            <el-button type="primary" :icon="Plus" circle size="small" />
          </div>
          <el-tree
            :data="noteTree"
            :props="treeProps"
            node-key="id"
            :expand-on-click-node="false"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <div class="tree-node">
                <el-icon v-if="data.type === 'folder'" class="folder-icon"><Folder /></el-icon>
                <el-icon v-else class="note-icon"><Document /></el-icon>
                <span>{{ node.label }}</span>
              </div>
            </template>
          </el-tree>
        </div>

        <!-- 回收站列表 -->
        <div v-else class="note-list">
          <div class="list-header">
            <span>回收站</span>
          </div>
          <div class="empty-tip">暂无删除的笔记</div>
        </div>
      </aside>

      <!-- 右侧编辑区域 -->
      <main class="content">
        <div v-if="currentNote" class="editor">
          <div class="editor-header">
            <el-input v-model="currentNote.title" class="title-input" placeholder="请输入标题" />
            <div class="editor-actions">
              <el-button type="primary" :icon="Check">保存</el-button>
            </div>
          </div>
          <el-input
            v-model="currentNote.content"
            type="textarea"
            :rows="20"
            placeholder="开始编写你的笔记..."
            resize="none"
          />
        </div>
        <div v-else class="empty-content">
          <el-empty description="选择一个笔记开始编辑" />
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get } from '@/utils/request.js'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  User,
  Setting,
  SwitchButton,
  Notebook,
  Delete,
  Plus,
  Folder,
  Document,
  Check
} from '@element-plus/icons-vue'

const router = useRouter()

// 当前选中的菜单
const activeMenu = ref('note')

// 当前选中的笔记
const currentNote = ref(null)

// 树形结构配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 笔记数据
const noteTree = ref([])

// 获取根目录笔记
const fetchRootNotes = async () => {
  try {
    const res = await get('/notes/root')
    if (res.data) {
      noteTree.value = res.data
    }
  } catch (err) {
    console.error('获取笔记失败:', err)
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchRootNotes()
})

// 菜单选择
const handleMenuSelect = (index) => {
  activeMenu.value = index
  currentNote.value = null
}

// 点击树节点
const handleNodeClick = (data) => {
  if (data.type === 'note') {
    currentNote.value = {
      id: data.id,
      title: data.name.replace('.md', ''),
      content: data.content || ''
    }
  }
}

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('token')
  ElMessage.success('退出成功')
  router.push('/')
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
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
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

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.folder-icon {
  color: #e6a23c;
}

.note-icon {
  color: #409eff;
}

.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px;
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

.editor :deep(.el-textarea__inner) {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  font-size: 15px;
  line-height: 1.8;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.empty-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
