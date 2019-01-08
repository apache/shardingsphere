<template>
  <div class="s-layout-header">
    <div class="s-pro-components-header">
      <i :class="classes" @click="togger"/>
      <div class="s-pro-components-header-right">
        <div class="avatar">
          <el-dropdown @command="handlerClick">
            <span class="el-dropdown-link">{{ username || '未登陆' }}</span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>{{ $t("common.loginOut") }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="lang-more">
          <el-dropdown>
            <i class="icon-duoyuyan iconfont"/>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>English</el-dropdown-item>
              <el-dropdown-item disabled>Chinese</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
      <el-breadcrumb separator="/" class="bread-nav">
        <el-breadcrumb-item><a style="font-weight: bold;">{{ $store.state.global.regCenterActivated || '' }}</a></el-breadcrumb-item>
      </el-breadcrumb>
    </div>
  </div>
</template>
<script>
export default {
  name: 'Head',
  data() {
    return {
      isCollapse: false,
      username: '',
      breadcrumbTxt: ''
    }
  },
  computed: {
    classes() {
      return [
        `s-pro-components-header-trigger`,
        {
          [`el-icon-d-arrow-left`]: !this.isCollapse,
          [`el-icon-d-arrow-right`]: this.isCollapse
        }
      ]
    }
  },
  created() {
    const store = window.localStorage
    this.username = store.getItem('username')
    // this.showBreadcrumbTxt()
  },
  methods: {
    // showBreadcrumbTxt() {
    //   const menuData = this.$t('common').menuData
    //   const hash = location.hash.split('#')[1]
    //   for (const v of menuData) {
    //     for (const vv of v.child) {
    //       if (vv.href.includes(hash)) {
    //         this.breadcrumbTxt = vv.title
    //         break
    //       }
    //     }
    //   }
    // },
    togger() {
      this.isCollapse = !this.isCollapse
      this.$emit('on-togger', this.isCollapse)
    },
    handlerClick() {
      const store = window.localStorage
      store.removeItem('username')
      store.removeItem('Access-Token')
      location.href = '#/login'
    }
  }
}
</script>
<style lang="scss" scoped>
.s-layout-header {
  background: #001529;
  padding: 0;
  height: 64px;
  line-height: 64px;
  width: 100%;
  .bread-nav {
    float: right;
    height: 64px;
    line-height: 64px;
    padding-right: 20px;
  }
  .s-pro-components-header {
    height: 64px;
    padding: 0;
    background: #fff;
    box-shadow: 0 1px 4px rgba(0,21,41,.08);
    position: relative;
    i.s-pro-components-header-trigger {
      font-size: 20px;
      height: 64px;
      cursor: pointer;
      transition: all .3s,padding 0s;
      padding: 22px 24px;
      float: left;
    }
    .s-pro-components-header-right {
      float: right;
      height: 100%;
      overflow: hidden;
    }
  }
  .avatar {
    cursor: pointer;
    padding: 0 12px;
    display: inline-block;
    transition: all .3s;
    height: 100%;
    float: left;
  }
  .lang-more {
    cursor: pointer;
    padding: 0 20px;
    display: inline-block;
    transition: all .3s;
    height: 100%;
    // .lang-icon {
    //   background: url('../../assets/img/lang.png') no-repeat center center;
    //   width: 32px;
    //   height: 60px;
    // }
  }
}
</style>
