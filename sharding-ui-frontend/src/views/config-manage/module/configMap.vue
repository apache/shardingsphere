<template>
  <div class="config-map">
    <el-row :gutter="20">
      <el-col :span="12">
        <span style="font-size: 18px; font-weight: bold;">Edit source here:</span>
        <el-input
          :rows="20"
          v-model="textarea"
          type="textarea"
          class="edit-text"/>
      </el-col>
      <el-col :span="12">
        <span style="font-size: 18px; font-weight: bold;">Result (JS object dump):</span>
        <el-input
          :rows="20"
          v-model="textarea2"
          type="textarea"
          readonly
          class="show-text"/>
      </el-col>
    </el-row>
    <el-row>
      <el-button class="config-map-btn" type="primary" @click="onConfirm">{{ $t('btn.submit') }}</el-button>
    </el-row>
  </div>
</template>
<script>
import yaml from 'js-yaml'
import API from '../api'

export default {
  name: 'ConfigMap',
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
    this.getConfigMap()
  },
  methods: {
    getConfigMap() {
      API.getConfigMap().then((res) => {
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
      API.putConfigMap({ configMap: this.textarea }).then((res) => {
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
  .config-map {
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
    .config-map-btn {
      margin-top: 10px;
      float: right;
    }
  }
</style>

