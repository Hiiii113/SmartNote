import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 整体导入 ElementPlus 组件库
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 导入 md-editor-v3 样式
import 'md-editor-v3/lib/style.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
// 注册 ElementPlus
app.use(ElementPlus)

app.mount('#app')
