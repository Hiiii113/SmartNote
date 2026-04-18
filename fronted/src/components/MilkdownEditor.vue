<script setup>
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { Editor, rootCtx, defaultValueCtx } from '@milkdown/core'
import { commonmark } from '@milkdown/preset-commonmark'
import { listener, listenerCtx } from '@milkdown/plugin-listener'
import * as Y from 'yjs'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '开始编写...' },
  noteId: { type: [String, Number], default: null },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'sync-status'])

const editorRef = ref(null)
let editor = null
let isInternalUpdate = false
let lastValue = ''

// Yjs 相关
let ydoc = null
let ytext = null
let socket = null
let isSynced = false
let heartbeatTimer = null

// 计算 WebSocket URL
const wsUrl = computed(() => {
  if (!props.noteId) return null
  const token = localStorage.getItem('token')
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//localhost:8081/ws/yjs/${props.noteId}?token=${token}`
})

// 初始化 Yjs 协同编辑
const initYjs = () => {
  if (!wsUrl.value) {
    console.log('[Yjs] 没有 noteId，跳过连接')
    return
  }

  console.log('[Yjs] 准备连接:', wsUrl.value)
  emit('sync-status', 'connecting')

  // 创建 Yjs 文档
  ydoc = new Y.Doc()
  ytext = ydoc.getText('content')

  // 创建原生 WebSocket 连接
  socket = new WebSocket(wsUrl.value)
  socket.binaryType = 'arraybuffer' // 设置类型是二进制

  socket.onopen = () => {
    console.log('[Yjs] WebSocket 已连接')
    emit('sync-status', 'connected')

    // 开始心跳
    startHeartbeat()

    // 如果有初始内容，先设置到 ytext
    if (props.modelValue) {
      ydoc.transact(() => {
        ytext.insert(0, props.modelValue)
      })
    }

    // 发送完整文档状态，让其他用户同步
    const state = Y.encodeStateAsUpdate(ydoc)
    socket.send(state)
  }

  socket.onmessage = (event) => {
    const arrayBuffer = event.data
    if (!arrayBuffer || arrayBuffer.byteLength === 0) return

    try {
      // 应用 Yjs 更新
      Y.applyUpdate(ydoc, new Uint8Array(arrayBuffer), 'remote')
      isSynced = true

      // 更新编辑器内容
      const newContent = ytext.toString()
      if (newContent !== lastValue) {
        isInternalUpdate = true
        lastValue = newContent
        emit('update:modelValue', newContent)
        if (editor) {
          updateEditorContent(newContent)
        }
        setTimeout(() => { isInternalUpdate = false }, 0)
      }
    } catch (e) {
      console.error('[Yjs] 处理消息失败:', e)
    }
  }

  socket.onerror = (e) => {
    console.error('[Yjs] WebSocket 错误:', e)
    emit('sync-status', 'disconnected')
  }

  socket.onclose = (e) => {
    console.log('[Yjs] WebSocket 关闭:', e.code, e.reason)
    emit('sync-status', 'disconnected')
    stopHeartbeat()
  }

  // 监听本地文档变化，发送给后端
  ydoc.on('update', (update, origin) => {
    // 只发送本地产生的更新，不发送远程更新
    if (origin !== 'remote' && socket && socket.readyState === WebSocket.OPEN) {
      socket.send(update)
    }
  })
}

// 断开 Yjs 连接
const disconnectYjs = () => {
  stopHeartbeat()
  if (socket) {
    socket.close()
    socket = null
  }
  if (ydoc) {
    ydoc.destroy()
    ydoc = null
    ytext = null
  }
  isSynced = false
}

// 开始心跳
const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      // 发送心跳消息（文本格式，后端会忽略）
      socket.send(JSON.stringify({ type: 'heartbeat' }))
    }
  }, 30000) // 30秒一次
}

// 停止心跳
const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

// 更新编辑器内容（通过重建编辑器）
const updateEditorContent = (content) => {
  if (editor) {
    editor.destroy()
    editor = null
    setTimeout(() => {
      initEditorWithContent(content)
    }, 0)
  }
}

// 使用指定内容初始化编辑器
const initEditorWithContent = async (content) => {
  if (editorRef.value && !editor) {
    editor = await Editor.make()
      .config((ctx) => {
        ctx.set(rootCtx, editorRef.value)
        ctx.set(defaultValueCtx, content || '')
        ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
          if (isInternalUpdate) return

          isInternalUpdate = true
          lastValue = markdown
          emit('update:modelValue', markdown)

          // 同步到 Yjs
          if (ytext && !props.readOnly) {
            ydoc.transact(() => {
              ytext.delete(0, ytext.length)
              ytext.insert(0, markdown)
            })
          }

          setTimeout(() => {
            isInternalUpdate = false
          }, 0)
        })
      })
      .use(commonmark)
      .use(listener)
      .create()
  }
}

// 初始化编辑器
const initEditor = async () => {
  // 如果 Yjs 已有内容，使用 Yjs 的内容
  let initialContent = props.modelValue || ''

  if (ytext && ytext.length > 0) {
    initialContent = ytext.toString()
    lastValue = initialContent
    emit('update:modelValue', initialContent)
  } else {
    lastValue = initialContent
    // 将初始内容同步到 Yjs
    if (ytext && initialContent) {
      ydoc.transact(() => {
        ytext.insert(0, initialContent)
      })
    }
  }

  await initEditorWithContent(initialContent)
}

// 监听 noteId 变化，重新建立连接
watch(() => props.noteId, (newNoteId, oldNoteId) => {
  if (newNoteId !== oldNoteId) {
    // 断开旧连接
    disconnectYjs()

    // 销毁编辑器
    if (editor) {
      editor.destroy()
      editor = null
    }

    // 重新初始化
    if (newNoteId) {
      initYjs()
      setTimeout(() => {
        initEditor()
      }, 100)
    } else {
      initEditor()
    }
  }
}, { immediate: false })

// 监听外部值变化（非 Yjs 更新）
watch(() => props.modelValue, (newVal) => {
  if (!isInternalUpdate && editor && newVal !== lastValue) {
    lastValue = newVal

    // 同步到 Yjs
    if (ytext && !props.readOnly) {
      ydoc.transact(() => {
        ytext.delete(0, ytext.length)
        ytext.insert(0, newVal || '')
      })
    }

    // 重建编辑器
    editor.destroy()
    editor = null
    setTimeout(() => {
      initEditorWithContent(newVal || '')
    }, 0)
  }
})

onMounted(() => {
  if (props.noteId) {
    initYjs()
    // 等待 Yjs 连接建立后再初始化编辑器
    setTimeout(() => {
      initEditor()
    }, 100)
  } else {
    initEditor()
  }
})

onUnmounted(() => {
  if (editor) {
    editor.destroy()
    editor = null
  }
  disconnectYjs()
})

// 暴露方法
defineExpose({
  getContent: () => props.modelValue,
  isConnected: () => socket?.readyState === WebSocket.OPEN
})
</script>

<template>
  <div class="milkdown-editor">
    <div ref="editorRef" class="editor-container"></div>
  </div>
</template>

<style scoped>
.milkdown-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 8px;
}

.editor-container {
  flex: 1;
  overflow: auto;
}

/* Milkdown 基础样式 */
.editor-container :deep(.editor) {
  height: 100%;
  padding: 16px;
  outline: none;
  min-height: 300px;
}

.editor-container :deep(.editor h1) {
  font-size: 2em;
  font-weight: bold;
  margin: 0.5em 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.3em;
}

.editor-container :deep(.editor h2) {
  font-size: 1.5em;
  font-weight: bold;
  margin: 0.5em 0;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.3em;
}

.editor-container :deep(.editor h3) {
  font-size: 1.25em;
  font-weight: bold;
  margin: 0.5em 0;
}

.editor-container :deep(.editor h4) {
  font-size: 1em;
  font-weight: bold;
  margin: 0.5em 0;
}

.editor-container :deep(.editor p) {
  margin: 0.5em 0;
  line-height: 1.8;
}

.editor-container :deep(.editor code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 0.9em;
}

.editor-container :deep(.editor pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1em 0;
}

.editor-container :deep(.editor pre code) {
  background: none;
  padding: 0;
  color: inherit;
}

.editor-container :deep(.editor blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 1em 0;
  color: #666;
  background: #f9f9f9;
  padding: 8px 16px;
  border-radius: 0 8px 8px 0;
}

.editor-container :deep(.editor ul),
.editor-container :deep(.editor ol) {
  padding-left: 24px;
  margin: 0.5em 0;
  line-height: 1.8;
}

.editor-container :deep(.editor li) {
  margin: 0.25em 0;
}

.editor-container :deep(.editor a) {
  color: #409eff;
  text-decoration: none;
}

.editor-container :deep(.editor a:hover) {
  text-decoration: underline;
}

.editor-container :deep(.editor hr) {
  border: none;
  border-top: 1px solid #eee;
  margin: 1.5em 0;
}

.editor-container :deep(.editor img) {
  max-width: 100%;
  border-radius: 8px;
}

.editor-container :deep(.editor table) {
  border-collapse: collapse;
  width: 100%;
  margin: 1em 0;
}

.editor-container :deep(.editor th),
.editor-container :deep(.editor td) {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: left;
}

.editor-container :deep(.editor th) {
  background: #f5f5f5;
  font-weight: bold;
}

.editor-container :deep(.editor tr:nth-child(even)) {
  background: #fafafa;
}
</style>
