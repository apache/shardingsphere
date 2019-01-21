<template>
  <div id="app">
    <s-container>
      <el-breadcrumb separator="/" class="bread-wrap">
        <el-breadcrumb-item :to="{ path: '/' }">Home</el-breadcrumb-item>
        <el-breadcrumb-item>Data governance</el-breadcrumb-item>
        <el-breadcrumb-item>{{ name }}</el-breadcrumb-item>
      </el-breadcrumb>
      <router-view/>
    </s-container>
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
      name: ''
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

