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
  <div class="login-container">
    <el-form
      ref="loginForm"
      :model="loginForm"
      class="login-form"
      auto-complete="off"
      label-position="left"
    >
      <h3 class="title" />
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          name="username"
          type="text"
          auto-complete="off"
          placeholder="username"
        >
          <i slot="prefix" class="icon-user icon-iem" />
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          :type="pwdType"
          v-model="loginForm.password"
          name="password"
          auto-complete="on"
          placeholder="password"
          @keyup.enter.native="handleLogin"
        >
          <i slot="prefix" class="icon-password icon-iem" />
        </el-input>
      </el-form-item>
      <el-form-item class="btn-login">
        <el-button
          :loading="loading"
          type="primary"
          style="width:100%;"
          @click.native.prevent="handleLogin"
        >{{ $t("login.btnTxt") }}</el-button>
      </el-form-item>
    </el-form>
    <s-footer style="position: fixed;bottom: 0;" />
  </div>
</template>

<script>
import SFooter from '../../components/Footer/index'
import API from './api'
export default {
  name: 'Login',
  components: {
    SFooter
  },
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'admin'
      },
      loading: false,
      pwdType: 'password',
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    if (window.localStorage.getItem('Access-Token')) {
      location.href = '#/registry-center'
    }
  },
  methods: {
    handleLogin() {
      const params = {
        username: this.loginForm.username,
        password: this.loginForm.password
      }
      API.getLogin(params).then(res => {
        const data = res.model
        const store = window.localStorage
        store.setItem('Access-Token', data.accessToken)
        store.setItem('username', data.username)
        location.href = '#/registry-center'
      })
    }
  }
}
</script>

<style rel="stylesheet/scss"  lang="scss">
$bg: #2d3a4b;
$light_gray: #f2f2f2;

/* reset element-ui css */
.login-container {
  .el-input {
    display: inline-block;
    // height: 47px;
    width: 85%;
    input {
      background: transparent;
      border: 0px;
      -webkit-appearance: none;
      border-radius: 0px;
      padding: 12px 5px 12px 60px;
      color: $light_gray;
      // height: 47px;
      &:-webkit-autofill {
        -webkit-box-shadow: 0 0 0px 1000px $bg inset !important;
        -webkit-text-fill-color: #fff !important;
      }
    }
  }
  .el-form-item {
    border: 1px solid rgba(255, 255, 255, 0.1);
    background: rgba(0, 0, 0, 0.1);
    border-radius: 5px;
    color: #454545;
  }
  .el-form-item__content {
    background: #070601;
    border-radius: 6px;
  }
  .icon-iem {
    margin: 8px 7px;
    width: 24px;
    height: 24px;
    display: inline-block;
  }
  .icon-user {
    background: url('../../assets/img/user.png') no-repeat left center;
  }
  .icon-password {
    background: url('../../assets/img/password.png') no-repeat left center;
  }
  .btn-login {
    margin-top: 50px;
  }
}
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
$bg: #2d3a4b;
$dark_gray: #889aa4;
$light_gray: #eee;
.login-container {
  position: fixed;
  height: 100%;
  width: 100%;
  // background-color: $bg;
  background: url('../../assets/img/bg.png') no-repeat center center;
  .login-form {
    position: absolute;
    left: 0;
    right: 0;
    width: 520px;
    max-width: 100%;
    padding: 35px 35px 15px 35px;
    margin: 120px auto;
  }
  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
  }
  .title {
    margin: 0px auto 40px auto;
    height: 86px;
    background: url('../../assets/img/login-logo.png') no-repeat center center;
  }
}
.footer-copy-right {
  width: 100%;
  line-height: 30px;
  position: absolute;
  bottom: 0;
  text-align: center;
}
</style>
