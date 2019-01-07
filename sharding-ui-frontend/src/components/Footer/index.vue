<template>
  <div class="footer-wrapper">
    Copyright
    <i class="icon-copyright iconfont"/> 2016 - 2018 The Apache Software Foundation, Licensed under the Apache License, Version 2.0.
  </div>
</template>
<script>
export default {
  name: 'Footer',
  data() {
    return {}
  },
  methods: {}
}

</script>
<style rel="stylesheet/scss" lang="scss" scoped>
$bg:#2d3a4b;
$dark_gray:#889aa4;
$light_gray:#eee;

.footer-wrapper {
  color: $dark_gray;
  width: 100%;
  font-size: 14px;
  line-height: 60px;
  text-align: center;
}

</style>
