import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Note from '../views/Note.vue'
import Friends from '../views/Friends.vue'
import Chat from '../views/Chat.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: Login, // 登录（默认界面）
    },
    {
      path: '/note',
      name: 'Note',
      component: Note, // 笔记界面
    },
    {
      path: '/note/:id',
      name: 'NoteDetail',
      component: Note, // 根据笔记 id 进入对应笔记界面
    },
    {
      path: '/friends',
      redirect: '/chat' // 聊天界面
    },
    {
      path: '/chat',
      name: 'Chat',
      component: Chat, // 聊天界面
    },
  ],
})

export default router
