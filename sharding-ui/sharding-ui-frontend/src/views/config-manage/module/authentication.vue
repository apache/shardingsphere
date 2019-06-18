<template>
  <div class="auth">
    <el-form ref="ruleForm" :model="form" :rules="rules" label-width="80px">
      <el-form-item :label="$t('login.labelUserName')" prop="username">
        <el-input v-model="form.username" :placeholder="$t('input.pUserName')"/>
      </el-form-item>
      <el-form-item :label="$t('login.labelPassword')" prop="password">
        <el-input v-model="form.password" :placeholder="$t('input.pPaasword')"/>
      </el-form-item>
      <el-form-item>
        <el-button @click="resetForm('ruleForm')">{{ $t('btn.reset') }}</el-button>
        <el-button type="primary" @click="submitForm('ruleForm')">{{ $t('btn.submit') }}</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import API from '../api'
export default {
  name: 'Authentication',
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: this.$t('input').pUserName, trigger: 'blur' }
        ],
        password: [
          { required: true, message: this.$t('input').pPaasword, trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getAuth()
  },
  methods: {
    getAuth() {
      API.getAuth().then((res) => {
        const data = res.model
        this.form.username = data.username
        this.form.password = data.password
      })
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          const params = {
            password: this.form.password,
            username: this.form.username
          }
          API.putAuth(params).then((res) => {
            if (res.success) {
              this.$notify({
                title: this.$t('common').notify.title,
                message: this.$t('common').notify.updateCompletedMessage,
                type: 'success'
              })
            } else {
              this.$notify({
                title: this.$t('common').notify.title,
                message: this.$t('common').notify.updateFaildMessage,
                type: 'error'
              })
            }
          })
        } else {
          return false
        }
      })
    },
    resetForm(formName) {
      this.$refs[formName].resetFields()
    }
  }
}
</script>
<style lang="scss" scoped>
  .auth {
    margin-top: 20px;
  }
</style>
