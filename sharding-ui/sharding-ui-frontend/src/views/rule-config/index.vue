<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License"); you may not use this file except in compliance with
  - the License.  You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <el-row class="box-card">
    <el-radio-group v-model="radioValue" @change="onChange">
      <el-radio-button :label="$t('ruleConfig.radioBtn.schema')" />
      <el-radio-button :label="$t('ruleConfig.radioBtn.authentication')" />
      <el-radio-button :label="$t('ruleConfig.radioBtn.props')" />
    </el-radio-group>
    <component :is="currentView" />
  </el-row>
</template>
<script>
import MSchema from './module/schema'
import MAuthentication from './module/authentication'
import MProps from './module/props'

export default {
  name: 'RuleConfig',
  components: {
    MSchema,
    MAuthentication,
    MProps
  },
  data() {
    return {
      radioValue: this.$t('ruleConfig').radioBtn.schema,
      currentView: MSchema
    }
  },
  methods: {
    onChange(val) {
      const radioBtn = this.$t('ruleConfig').radioBtn
      if (val === radioBtn.schema) {
        this.currentView = MSchema
      } else if (val === radioBtn.authentication) {
        this.currentView = MAuthentication
      } else if (val === radioBtn.props) {
        this.currentView = MProps
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
