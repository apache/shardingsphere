<template>
  <s-container>
    <el-card class="box-card">
      <el-radio-group v-model="radioValue" @change="onChange">
        <el-radio-button label="schema"/>
        <el-radio-button label="authentication"/>
        <el-radio-button label="configmap"/>
        <el-radio-button label="props"/>
      </el-radio-group>
      <component :is="currentView"/>
    </el-card>
  </s-container>
</template>
<script>
import SContainer from '@/components/Container/index.vue'
import MSchema from './module/schema'
import MAuthentication from './module/authentication'

export default {
  name: 'ConfigManage',
  components: {
    SContainer,
    MSchema,
    MAuthentication
  },
  data() {
    return {
      radioValue: 'schema',
      currentView: MSchema
    }
  },
  methods: {
    onChange(val) {
      if (val === 'schema') {
        this.currentView = MSchema
      } else if (val === 'authentication') {
        this.currentView = MAuthentication
      }
    }
  }
}
</script>
<style lang='scss' scoped>
  .tree-wrap {
    margin-top: 20px;
  }
</style>
