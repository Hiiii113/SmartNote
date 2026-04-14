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
      component: Login,
    },
    {
      path: '/note',
      name: 'Note',
      component: Note,
    },
    {
      path: '/note/:id',
      name: 'NoteDetail',
      component: Note,
    },
    {
      path: '/friends',
      name: 'Friends',
      component: Friends,
    },
    {
      path: '/chat',
      name: 'Chat',
      component: Chat,
    },
  ],
})

export default router
