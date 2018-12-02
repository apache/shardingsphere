<template>
  <div class="s-menu">
    <el-menu
      :collapse="isCollapse"
      background-color="#001529"
      text-color="#fff"
      active-text-color="#fff"
      default-active="1-4-1"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose">
      <s-logo/>
      <el-submenu v-for="(item, index) in menuData" :key="index" :index="index">
        <template slot="title">
          <i class="el-icon-location"/>
          <span slot="title">{{ item.title }}</span>
        </template>
        <el-menu-item-group>
          <el-menu-item
            v-for="(itm, idx) in item.child"
            :key="idx"
            :index="index + '-' + idx">{{ itm.title }}</el-menu-item>
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
      menuData: [
        {
          title: '数据治理',
          child: [
            {
              title: '注册配置中心'
            }, {
              title: '配置管理'
            }
          ]
        }
      ]
    }
  },
  methods: {
    handleOpen(key, keyPath) {
      console.log(key, keyPath)
    },
    handleClose(key, keyPath) {
      console.log(key, keyPath)
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
