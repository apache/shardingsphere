<template>
  <el-row class="box-card">
    <el-radio-group v-model="radioValue" @change="onChange">
      <el-radio-button label="schema"/>
      <el-radio-button label="authentication"/>
      <el-radio-button label="configMap"/>
      <el-radio-button label="props"/>
    </el-radio-group>
    <component :is="currentView"/>
  </el-row>
</template>
<script>
import MSchema from './module/schema'
import MAuthentication from './module/authentication'
import MProps from './module/props'
import MConfigMap from './module/configMap'

export default {
  name: 'ConfigManage',
  components: {
    MSchema,
    MAuthentication,
    MProps,
    MConfigMap
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
      } else if (val === 'props') {
        this.currentView = MProps
      } else {
        this.currentView = MConfigMap
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
