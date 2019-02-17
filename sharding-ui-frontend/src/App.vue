<template>
  <div id="app">
    <s-container v-if="localStorage.getItem('Access-Token')">
      <el-breadcrumb separator="/" class="bread-wrap">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Data governance</el-breadcrumb-item>
        <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <router-view/>
    </s-container>
    <template v-else>
      <router-view/>
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
      name: '',
      localStorage: window.localStorage
    }
  },
  watch: {
    $route(to, from) {
      this.name = to.name
    }
  }
}
</script>
<style lang="scss" scoped>
.bread-wrap {
  margin-bottom: 15px;
}
</style>

