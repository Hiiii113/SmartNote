<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { Editor, defaultValueCtx, editorViewOptionsCtx, rootCtx } from '@milkdown/core'
import { listener, listenerCtx } from '@milkdown/plugin-listener'
import { commonmark } from '@milkdown/preset-commonmark'
import { replaceAll } from '@milkdown/utils'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入内容...' },
  noteId: { type: [String, Number], default: null },
  readOnly: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let editor = null
let isInternalUpdate = false
let lastValue = ''

const editable = computed(() => !props.readOnly)

const initEditor = async (content = '') => {
  if (!editorRef.value || editor) return

  lastValue = content || ''
  editor = await Editor.make()
    .config((ctx) => {
      ctx.set(rootCtx, editorRef.value)
      ctx.set(defaultValueCtx, lastValue)
      ctx.update(editorViewOptionsCtx, (prev) => ({
        ...prev,
        editable: () => editable.value
      }))
      ctx.get(listenerCtx).markdownUpdated((_, markdown) => {
        if (isInternalUpdate) return
        lastValue = markdown
        emit('update:modelValue', markdown)
      })
    })
    .use(commonmark)
    .use(listener)
    .create()
}

const destroyEditor = async () => {
  if (!editor) return
  await editor.destroy()
  editor = null
}

const syncEditorContent = (content = '') => {
  if (!editor || content === lastValue) return

  isInternalUpdate = true
  lastValue = content

  try {
    editor.action(replaceAll(content))
  } finally {
    setTimeout(() => {
      isInternalUpdate = false
    }, 0)
  }
}

watch(
  () => props.modelValue,
  (newValue) => {
    if (!editor) return
    syncEditorContent(newValue || '')
  }
)

watch(
  () => props.readOnly,
  async () => {
    const content = lastValue
    await destroyEditor()
    await initEditor(content)
  }
)

watch(
  () => props.noteId,
  async () => {
    const content = props.modelValue || ''
    await destroyEditor()
    await initEditor(content)
  }
)

onMounted(() => {
  initEditor(props.modelValue || '')
})

onUnmounted(() => {
  destroyEditor()
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
