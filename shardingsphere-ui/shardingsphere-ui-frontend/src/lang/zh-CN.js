/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default {
  common: {
    home: '主页',
    menuData: [
      {
        title: '配置中心',
        child: [
          {
            title: '服务列表',
            href: '/config-center'
          },
          {
            title: '配置管理',
            href: '/rule-config'
          }
        ]
      },
      {
        title: '注册中心',
        child: [
          {
            title: '服务列表',
            href: '/registry-center'
          },
          {
            title: '运行状态',
            href: '/runtime-status'
          }
        ]
      },
      {
        title: '数据扩容',
        href: '/data-scaling'
      }
    ],
    connect: '已连接',
    connection: '连接',
    del: '删除',
    notify: {
      title: '提示',
      addSucMessage: '添加成功',
      editSucMessage: '修改成功',
      conSucMessage: '连接成功',
      conFailMessage: '连接失败',
      delSucMessage: '删除成功',
      delFailMessage: '删除失败',
      updateCompletedMessage: '更新成功',
      updateFaildMessage: '更新失败'
    },
    loginOut: '退出登录',
    dropdownList: [
      {
        title: '中文',
        command: 'Chinese'
      },
      {
        title: 'English',
        command: 'English'
      }
    ]
  },
  login: {
    btnTxt: '登录',
    labelUserName: '用户名',
    labelPassword: '密码'
  },
  btn: {
    submit: '提交',
    reset: '重置',
    cancel: '取消'
  },
  input: {
    pUserName: '请输入用户名',
    pPaasword: '请输入密码'
  },
  registryCenter: {
    btnTxt: '添加',
    registDialog: {
      title: '添加注册中心',
      editTitle: '编辑注册中心',
      name: '注册中心名称',
      centerType: '注册中心类型',
      address: '注册中心地址',
      orchestrationName: '数据治理实例',
      namespaces: '命名空间',
      digest: '登录凭证',
      btnConfirmTxt: '确定',
      btnCancelTxt: '取消'
    },
    table: {
      operate: '操作',
      operateConnect: '连接',
      operateConnected: '已激活',
      operateDel: '删除',
      operateEdit: '编辑'
    },
    rules: {
      name: '请输入注册中心名称',
      centerType: '请选择注册中心类型',
      namespaces: '请输入命名空间',
      address: '请选输入注册中心地址',
      orchestrationName: '请输入数据治理实例名称',
      digest: '请输入登录凭证'
    }
  },
  configCenter: {
    btnTxt: '添加',
    configDialog: {
      title: '添加配置中心',
      editTitle: '编辑配置中心',
      name: '配置中心名称',
      centerType: '配置中心类型',
      address: '配置中心地址',
      orchestrationName: '数据治理实例',
      namespaces: '命名空间',
      digest: '登录凭证',
      btnConfirmTxt: '确定',
      btnCancelTxt: '取消'
    },
    table: {
      operate: '操作',
      operateConnect: '连接',
      operateConnected: '已激活',
      operateDel: '删除',
      operateEdit: '编辑'
    },
    rules: {
      name: '请输入配置中心名称',
      centerType: '请选择配置中心类型',
      namespaces: '请输入命名空间',
      address: '请选输入配置中心地址',
      orchestrationName: '请输入数据治理实例名称',
      digest: '请输入登录凭证'
    }
  },
  runtimeStatus: {
    serviceNode: '服务节点',
    slaveDataSourceName: '从库信息',
    dataSource: {
      schema: '逻辑库名',
      masterDataSourceName: '主库名',
      slaveDataSourceName: '从库名'
    },
    instance: {
      instanceId: '节点标识',
      serverIp: '服务ip'
    }
  },
  ruleConfig: {
    form: {
      inputPlaceholder: '请输入内容'
    },
    schema: {
      name: '名称',
      ruleConfig: '分片配置规则',
      dataSourceConfig: '数据源配置规则'
    },
    schemaRules: {
      name: '请输入名称',
      ruleConfig: '请输入数据分片配置规则',
      dataSourceConfig: '请输入数据源配置规则'
    },
    radioBtn: {
      schema: '数据源',
      authentication: '认证信息',
      props: '属性配置'
    }
  }
}
