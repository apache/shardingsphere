<template>
  <s-container>
    <el-card class="box-card">
      <el-radio-group v-model="radioValue">
        <el-radio-button label="schema"/>
        <el-radio-button label="authentication"/>
        <el-radio-button label="configmap"/>
        <el-radio-button label="props"/>
      </el-radio-group>
      <el-tree :data="data" :props="defaultProps" class="tree-wrap" @node-click="handleNodeClick"/>
    </el-card>
  </s-container>
</template>
<script>
import SContainer from '@/components/Container/index.vue'

export default {
  name: 'ConfigManage',
  components: {
    SContainer
  },
  data() {
    return {
      radioValue: 'schema',
      data: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      }
    }
  },
  methods: {
    handleNodeClick(data) {
      console.log(data)
    }
  }
}
</script>
<style lang='scss' scoped>
  .tree-wrap {
    margin-top: 20px;
  }
</style>
