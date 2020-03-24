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
  <div class="s-menu">
    <el-menu
      :collapse="isCollapse"
      :default-active="defActive"
      background-color="#090a01"
      text-color="#fff"
      active-text-color="#fff"
      class="el-menu-vertical-menu"
      @open="handleOpen"
      @close="handleClose"
    >
      <s-logo/>
      <template v-for="(item, index) in menuData">
        <el-submenu v-if="item.child && item.child.length" :index="String(index)" :key="String(index)">
          <template slot="title">
            <i class="icon-sidebar"/>
            <span slot="title">{{ item.title }}</span>
          </template>
          <a v-for="(itm, idx) in item.child" :href="'#' + itm.href" :key="String(idx)">
            <el-menu-item :index="itm.href">{{ itm.title }}</el-menu-item>
          </a>
        </el-submenu>
        <a v-else :href="'#' + item.href" :key="String(index)">
          <el-menu-item :index="item.href">{{ item.title }}</el-menu-item>
        </a>
      </template>
    </el-menu>
  </div>
</template>
<script>
import SLogo from '../Logo/index.vue'
export default {
  name: 'Menu',
  components: {
    SLogo
  },
  props: {
    isCollapse: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      menuData: this.$t('common').menuData,
      defActive: ''
    }
  },
  watch: {
    $route: {
      handler(route) {
        for (const v of this.menuData) {
          if (!v.child) {
            if (v.href === route.path) {
              this.defActive = v.href
              break
            }
          } else {
            for (const vv of v.child) {
              if (route.path === vv.href) {
                this.defActive = vv.href
                break
              }
            }
          }
        }
      },
      immediate: true
    }
  },
  methods: {
    handleOpen(key, keyPath) {
      // console.log(key, keyPath)
    },
    handleClose(key, keyPath) {
      // console.log(key, keyPath)
    }
  }
}
</script>
<style lang="scss">
.s-menu {
  height: 100%;
  .el-menu--collapse {
    height: 100%;
    width: 80px;
    .s-pro-components-sider-menu-index-logo {
      padding-left: 22px;
    }
  }
  .el-menu-vertical-menu:not(.el-menu--collapse) {
    width: 256px;
    height: 100%;
  }
  .el-menu-item {
    background: #090a01;
  }
  .el-menu {
    border-right: none;
  }
  .el-submenu {
    .el-menu {
      background: #090a01;
    }
  }
  .is-active {
    background-color: #e17425 !important;
  }
  .icon-sidebar {
    background: url("../../assets/img/sidebar-icon.png") no-repeat left center;
    display: inline-block;
    width: 16px;
    height: 16px;
  }
  .el-menu--collapse {
    img {
      display: none;
    }
    .collapse-logo {
      display: block;
      margin-top: 13px;
    }
  }
}
.el-menu--vertical {
  .is-active {
    background-color: #e17425 !important;
  }
}
</style>
