<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License"); you may not use this file except in compliance with
  - the License.  You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div id="app">
    <s-container v-if="localStorage.getItem('Access-Token')">
      <el-breadcrumb separator="/" class="bread-wrap">
        <el-breadcrumb-item :to="{ path: '/' }">{{
          $t('common.home')
        }}</el-breadcrumb-item>
        <el-breadcrumb-item v-for="each in menus" :key="each">
          {{ each }}
        </el-breadcrumb-item>
      </el-breadcrumb>
      <router-view />
    </s-container>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script>
import SContainer from '@/components/Container/index.vue'
export default {
  name: 'App',
  components: {
    SContainer
  },
  data() {
    return {
      menus: [],
      localStorage: window.localStorage
    }
  },
  watch: {
    $route(to, from) {
      for (const parentMenuItem of this.$t('common').menuData) {
        if (!parentMenuItem.child && parentMenuItem.href === to.path) {
          this.menus = [parentMenuItem.title]
          break
        }
        for (const childMenuItem of parentMenuItem.child) {
          if (childMenuItem.href === to.path) {
            this.menus = [parentMenuItem.title, childMenuItem.title]
            break
          }
        }
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.bread-wrap {
  margin-bottom: 15px;
}
</style>
