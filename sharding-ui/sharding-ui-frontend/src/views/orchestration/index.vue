<template>
  <el-row class="box-card">
    <el-radio-group v-model="radioValue" @change="onChange">
      <el-radio-button label="1">{{ $t('orchestration.serviceNode') }}</el-radio-button>
      <el-radio-button label="2">{{ $t('orchestration.slaveDataSourceName') }}</el-radio-button>
    </el-radio-group>
    <component :is="currentView"/>
  </el-row>
</template>
<script>
import MDataSource from './module/dataSource'
import MInstance from './module/instance'
export default {
  name: 'Orchestration',
  components: {
    MDataSource,
    MInstance
  },
  data() {
    return {
      radioValue: 1,
      currentView: MInstance
    }
  },
  methods: {
    onChange(val) {
      if (val === '1') {
        this.currentView = MInstance
      } else {
        this.currentView = MDataSource
      }
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
