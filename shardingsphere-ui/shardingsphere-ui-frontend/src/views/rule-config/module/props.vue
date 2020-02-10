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
  <el-card class="box-card props">
    <el-row :gutter="20">
      <el-col :span="12">
        <span
          style="font-size: 14px; color: #4a4a4a; margin-bottom: 10px;display: inline-block;"
        >Edit source here:</span>
        <el-input :rows="20" v-model="textarea" type="textarea" class="edit-text" />
      </el-col>
      <el-col :span="12">
        <span
          style="font-size: 14px; color: #4a4a4a; margin-bottom: 10px;display: inline-block;"
        >Result (JS object dump):</span>
        <el-input :rows="20" v-model="textarea2" type="textarea" readonly class="show-text" />
      </el-col>
    </el-row>
    <el-row>
      <el-button class="props-btn" type="primary" @click="onConfirm">{{ $t('btn.submit') }}</el-button>
    </el-row>
  </el-card>
</template>
<script>
import yaml from 'js-yaml'
import API from '../api'

export default {
  name: 'Props',
  data() {
    return {
      textarea: ``
    }
  },
  computed: {
    textarea2() {
      return JSON.stringify(yaml.safeLoad(this.textarea), null, '\t')
    }
  },
  created() {
    this.getProps()
  },
  methods: {
    getProps() {
      API.getProps().then(res => {
        if (!res.success) return
        const model = res.model
        if (Object.prototype.toString.call(model) === '[object String]') {
          this.textarea = model
        } else {
          this.textarea = JSON.stringify(model, null, '\t')
        }
      })
    },
    onConfirm() {
      API.putProps({ props: this.textarea }).then(res => {
        if (res.success) {
          this.$notify({
            title: this.$t('common').notify.title,
            message: this.$t('common').notify.updateCompletedMessage,
            type: 'success'
          })
          this.centerDialogVisible = false
        } else {
          this.$notify({
            title: this.$t('common').notify.title,
            message: this.$t('common').notify.updateFaildMessage,
            type: 'error'
          })
        }
      })
    }
  }
}
</script>
<style lang="scss">
.props {
  margin-top: 20px;
  .edit-text {
    margin-top: 5px;
    textarea {
      background: #fffffb;
    }
  }
  .show-text {
    margin-top: 5px;
    textarea {
      background: rgb(246, 246, 246);
    }
  }
  .props-btn {
    margin-top: 10px;
    float: right;
  }
}
</style>

