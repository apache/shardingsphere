<template>
  <s-container>
    <s-regist-config/>
  </s-container>
</template>

<script>
import SContainer from '@/components/Container/index.vue'
import SRegistConfig from './module/registConfig'
export default {
  name: 'Index',
  components: {
    SContainer,
    SRegistConfig
  }
}
</script>

<style lang="scss" scoped>
</style>
