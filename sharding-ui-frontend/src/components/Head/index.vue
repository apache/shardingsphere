<template>
  <div class="s-layout-header">
    <div class="s-pro-components-header">
      <i :class="classes" @click="togger"/>
      <div class="s-pro-components-header-right">
        <div class="avatar">
          <el-dropdown @command="handlerClick">
            <el-tag type="success">
              <span class="el-dropdown-link">
                {{ username || '未登陆' }}
                <i class="el-icon-arrow-down el-icon--right"/>
              </span>
            </el-tag>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item>{{ $t("common.loginOut") }}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="lang-more">
          <el-tag>English</el-tag>
        </div>
      </div>
      <el-breadcrumb separator="/" class="bread-nav">
        <el-breadcrumb-item>
          <a
            style="font-weight: bold; color: #E17425;"
          >{{ $store.state.global.regCenterActivated || '' }}</a>
        </el-breadcrumb-item>
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
        `icon-item`,
        {
          [`icon-shrink`]: !this.isCollapse,
          [`icon-expand`]: this.isCollapse
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
    box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
    position: relative;
    i.icon-item {
      width: 16px;
      height: 16px;
      float: left;
      cursor: pointer;
      margin: 24px;
    }
    i.icon-shrink {
      background: url("../../assets/img/shrink.png") no-repeat left center;
    }
    .icon-expand {
      background: url("../../assets/img/expand.png") no-repeat left center;
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
    transition: all 0.3s;
    height: 100%;
  }
  .lang-more {
    cursor: pointer;
    padding: 0 20px;
    display: inline-block;
    transition: all 0.3s;
    height: 100%;
    // .lang-icon {
    //   background: url('../../assets/img/lang.png') no-repeat center center;
    //   width: 32px;
    //   height: 60px;
    // }
  }
}
</style>
