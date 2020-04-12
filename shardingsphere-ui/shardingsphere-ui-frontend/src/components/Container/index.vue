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
  <el-container style="height: 100%;">
    <el-aside width="auto">
      <s-menu :is-collapse="isCollapse" />
    </el-aside>
    <el-container>
      <el-header style="padding: 0">
        <s-head class="s-head" @on-togger="onTogger" />
      </el-header>
      <el-main>
        <slot />
      </el-main>
      <el-footer>
        <s-footer />
      </el-footer>
    </el-container>
  </el-container>
</template>

<script>
import SMenu from '../Menu/index.vue'
import SHead from '../Head/index.vue'
import SFooter from '../Footer/index.vue'
export default {
  name: 'Container',
  components: {
    SMenu,
    SHead,
    SFooter
  },
  data() {
    return {
      isCollapse: false
    }
  },
  methods: {
    onTogger(val) {
      this.isCollapse = val
    }
  }
}
</script>

<style lang="scss" scoped>
.el-main {
  overflow: inherit;
}
</style>
