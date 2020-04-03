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
    home: 'Home',
    menuData: [
      {
        title: 'Config Center',
        child: [
          {
            title: 'Config Server',
            href: '/config-center'
          },
          {
            title: 'Rule Config',
            href: '/rule-config'
          }
        ]
      },
      {
        title: 'Registry Center',
        child: [
          {
            title: 'Registry Server',
            href: '/registry-center'
          },
          {
            title: 'Runtime Status',
            href: '/runtime-status'
          }
        ]
      },
      {
        title: 'Data scaling',
        href: '/data-scaling'
      }
    ],
    connected: 'Connected',
    connection: 'Connection',
    del: 'Delete',
    notify: {
      title: 'Prompt',
      addSucMessage: 'Add Succeeded',
      editSucMessage: 'Edit Succeeded',
      conSucMessage: 'Connection Succeeded',
      conFailMessage: 'Connection Failed',
      delSucMessage: 'Delete Succeeded',
      delFailMessage: 'Delete Failed',
      updateCompletedMessage: 'Update Completed',
      updateFaildMessage: 'Update Faild'
    },
    loginOut: 'Sign Out',
    dropdownList: [
      {
        title: '中文',
        command: 'zh-CN'
      },
      {
        title: 'English',
        command: 'en-US'
      }
    ]
  },
  login: {
    btnTxt: 'Login',
    labelUserName: 'Username',
    labelPassword: 'Password'
  },
  btn: {
    submit: 'Submit',
    reset: 'Reset',
    cancel: 'Cancel'
  },
  input: {
    pUserName: 'Please enter user name',
    pPaasword: 'Please enter your password'
  },
  registryCenter: {
    btnTxt: 'ADD',
    registDialog: {
      title: 'Add a registry center',
      editTitle: 'Edit registry center',
      name: 'Name',
      centerType: 'Instance Type',
      address: 'Address',
      orchestrationName: 'Orchestration Name',
      namespaces: 'Namespace',
      digest: 'Digest',
      btnConfirmTxt: 'Confirm',
      btnCancelTxt: 'Cancel'
    },
    table: {
      operate: 'Operate',
      operateConnect: 'Connect',
      operateConnected: 'Connected',
      operateDel: 'Del',
      operateEdit: 'Edit'
    },
    rules: {
      name: 'Please enter the name of the registration center',
      address: 'Please enter the registration center Address',
      namespaces: 'Please enter a Namespace',
      centerType: 'Please select a Center Type',
      orchestrationName: 'Please enter a Orchestration Name',
      digest: 'Please enter a digest'
    }
  },
  configCenter: {
    btnTxt: 'ADD',
    configDialog: {
      title: 'Add a config center',
      editTitle: 'Edit config center',
      name: 'Name',
      centerType: 'Instance Type',
      address: 'Address',
      orchestrationName: 'Orchestration Name',
      namespaces: 'Namespace',
      digest: 'Digest',
      btnConfirmTxt: 'Confirm',
      btnCancelTxt: 'Cancel'
    },
    table: {
      operate: 'Operate',
      operateConnect: 'Connect',
      operateConnected: 'Connected',
      operateDel: 'Del',
      operateEdit: 'Edit'
    },
    rules: {
      name: 'Please enter the name of the config center',
      address: 'Please enter the config center Address',
      namespaces: 'Please enter a Namespace',
      centerType: 'Please select a Center Type',
      orchestrationName: 'Please enter a Orchestration Name',
      digest: 'Please enter a digest'
    }
  },
  runtimeStatus: {
    serviceNode: 'Service Node',
    slaveDataSourceName: 'Slave DataSource Info',
    dataSource: {
      schema: 'Schema',
      masterDataSourceName: 'Master DataSource Name',
      slaveDataSourceName: 'Slave DataSource Name'
    },
    instance: {
      instanceId: 'Instance Id',
      serverIp: 'Server Ip'
    }
  },
  ruleConfig: {
    form: {
      inputPlaceholder: 'Please enter content'
    },
    schema: {
      name: 'Name',
      ruleConfig: 'Rule Config',
      dataSourceConfig: 'Data Source Config'
    },
    schemaRules: {
      name: 'Please enter the name of the schema',
      ruleConfig: 'Please enter the rule config of the schema',
      dataSourceConfig: 'Please enter the data source config of the schema'
    },
    radioBtn: {
      schema: 'Schema',
      authentication: 'Authentication',
      props: 'Props'
    }
  },
  dataScaling: {
    btnTxt: 'ADD',
    tableList: {
      jobId: 'jobId',
      jobName: 'jobName',
      status: 'status',
      operate: 'operate',
      operateStop: 'stop',
      operateSee: 'see'
    },
    registDialog: {
      title: 'Add a job',
      source: 'Source',
      target: 'Target',
      jobCount: 'JobCount',
      jobCountPlaceholder: 'Please enter jobCount',
      username: 'Username',
      usernamePlaceholder: 'Please enter username',
      password: 'Password',
      passwordPlaceholder: 'Please enter password',
      url: 'Url',
      urlPlaceholder: 'Please enter url',
      btnConfirmTxt: 'Confirm',
      btnCancelTxt: 'Cancel'
    },
    rules: {
      source: 'Please select the source of the registration center',
      target: 'Please select a target',
      serviceUrl: 'ServiceUrl must fill'
    },
    notify: {
      title: 'Prompt',
      conSucMessage: 'Add Succeeded',
      conFailMessage: 'Add Failed',
      delSucMessage: 'Delete Succeeded',
      delFailMessage: 'Delete Failed'
    },
    serviceDialog: {
      serviceName: 'Please enter serviceName',
      serviceType: 'Please enter serviceType',
      serviceUrl: 'Please enter serviceUrl'
    }
  }
}
