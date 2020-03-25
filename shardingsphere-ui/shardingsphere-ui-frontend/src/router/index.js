/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export const constantRouterMap = [
  {
    path: '*',
    redirect: '/registry-center'
  },
  {
    path: '/config-center',
    component: () => import('@/views/config-center'),
    hidden: true,
    name: 'Config center'
  },
  {
    path: '/registry-center',
    component: () => import('@/views/registry-center'),
    hidden: true,
    name: 'Registry center'
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/rule-config',
    component: () => import('@/views/rule-config'),
    hidden: true,
    name: 'Rule config'
  },
  {
    path: '/runtime-status',
    component: () => import('@/views/runtime-status'),
    hidden: true,
    name: 'Runtime status'
  },
  {
    path: '/data-scaling',
    component: () => import('@/views/data-scaling'),
    hidden: true,
    name: 'Data scaling'
  }
]

export default new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})
