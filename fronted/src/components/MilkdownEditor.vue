<script setup>
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Editor, rootCtx, defaultValueCtx } from '@milkdown/core'
import { commonmark } from '@milkdown/preset-commonmark'
import { listener, listenerCtx } from '@milkdown/plugin-listener'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '开始编写...' }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let editor = null
let isInternalUpdate = false

// 初始化编辑器
const initEditor = async () => {
  if (editorRef.value && !editor) {
    editor = await Editor.make()
      .config((ctx) => {
        ctx.set(rootCtx, editorRef.value)
        ctx.set(defaultValueCtx, props.modelValue || '')
        ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
          isInternalUpdate = true
          emit('update:modelValue', markdown)
          nextTick(() => {
            isInternalUpdate = false
          })
        })
      })
      .use(commonmark)
      .use(listener)
      .create()
  }
}

// 监听外部值变化，重新创建编辑器
watch(() => props.modelValue, (newVal, oldVal) => {
  if (!isInternalUpdate && editor && newVal !== oldVal) {
    // 销毁并重新创建编辑器
    editor.destroy()
    editor = null
    nextTick(() => {
      initEditor()
    })
  }
})

onMounted(() => {
  initEditor()
})

onUnmounted(() => {
  editor?.destroy()
})

// 暴露获取内容的方法
defineExpose({
  getContent: () => props.modelValue
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

/* 空状态提示 */
.editor-container:empty::before {
  content: attr(data-placeholder);
  color: #c0c4cc;
  font-size: 14px;
}
</style>
