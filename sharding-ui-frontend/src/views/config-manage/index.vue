<template>
  <s-container>
    <el-card class="box-card">
      <el-radio-group v-model="radioValue">
        <el-radio-button label="schema"/>
        <el-radio-button label="authentication"/>
        <el-radio-button label="configmap"/>
        <el-radio-button label="props"/>
      </el-radio-group>
      <m-schema/>
    </el-card>
  </s-container>
</template>
<script>
import yaml from 'js-yaml'
import SContainer from '@/components/Container/index.vue'
import MSchema from './module/schema'

export default {
  name: 'ConfigManage',
  components: {
    SContainer,
    MSchema
  },
  data() {
    return {
      radioValue: 'schema'
    }
  }
}
</script>
<style lang='scss' scoped>
  .tree-wrap {
    margin-top: 20px;
  }
</style>
