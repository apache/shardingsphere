<template>
  <div class="s-menu">
    <el-menu
      :collapse="isCollapse"
      :default-active="defActive"
      background-color="#001529"
      text-color="#fff"
      active-text-color="#fff"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose">
      <s-logo/>
      <el-submenu v-for="(item, index) in menuData" :key="String(index)" :index="String(index)">
        <template slot="title">
          <i class="el-icon-tickets"/>
          <span slot="title">{{ item.title }}</span>
        </template>
        <el-menu-item-group>
          <a v-for="(itm, idx) in item.child" :href="'#' + itm.href" :key="idx">
            <el-menu-item
              :index="itm.href">{{ itm.title }}</el-menu-item>
          </a>
        </el-menu-item-group>
      </el-submenu>
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
          for (const vv of v.child) {
            if (route.path === vv.href) {
              this.defActive = vv.href
              break
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
  }
  .el-menu-vertical-demo:not(.el-menu--collapse) {
    width: 256px;
    height: 100%;
  }
  .el-menu-item {
    background: #001529;
  }
  .el-menu {
    border-right: none;
  }
  .el-submenu {
    .el-menu {
      background: #000c17;
    }
  }
  .is-active {
    background-color: #1890ff !important;
  }
}
</style>
