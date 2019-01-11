<template>
  <s-container>
    <el-card class="box-card">
      <el-radio-group v-model="radioValue" @change="onChange">
        <el-radio-button label="1">{{ $t('orchestration.serviceNode') }}</el-radio-button>
        <el-radio-button label="2">{{ $t('orchestration.slaveDataSourceName') }}</el-radio-button>
      </el-radio-group>
      <component :is="currentView"/>
    </el-card>
  </s-container>
</template>
<script>
import SContainer from '@/components/Container/index.vue'
import MDataSource from './module/dataSource'
import MInstance from './module/instance'
export default {
  name: 'Orchestration',
  components: {
    SContainer,
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
