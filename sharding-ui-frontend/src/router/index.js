import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export const constantRouterMap = [
  {
    path: '*',
    redirect: '/config-regist'
  },
  {
    path: '/config-regist',
    component: () => import('@/views/index/index'), hidden: true },
  { path: '/login',
    component: () => import('@/views/login/index'), hidden: true },
  { path: '/config-manage',
    component: () => import('@/views/config-manage/index'), hidden: true },
  { path: '/orchestration',
    component: () => import('@/views/orchestration/index'), hidden: true }
]

export default new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})
