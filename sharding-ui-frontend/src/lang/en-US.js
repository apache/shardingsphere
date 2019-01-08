export default {
  common: {
    menuData: [{
      title: 'Data governance',
      child: [{
        title: 'Config regist',
        href: '/config-regist'
      }, {
        title: 'Config manage',
        href: '/config-manage'
      }, {
        title: 'Orchestration',
        href: '/orchestration'
      }]
    }],
    connected: 'Connected',
    connection: 'Connection',
    del: 'Delete',
    notify: {
      title: 'Prompt',
      conSucMessage: 'Connection succeeded',
      conFailMessage: 'Connection failed',
      delSucMessage: 'Delete succeeded',
      delFailMessage: 'Delete failed',
      updateCompletedMessage: 'Update completed',
      updateFaildMessage: 'Update faild'
    },
    loginOut: 'Sign out'
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
  index: {
    btnTxt: 'ADD',
    registDialog: {
      title: 'Add a registry',
      name: 'name',
      centerType: 'centerType',
      address: 'address',
      orchestrationName: 'orchestrationName',
      namespaces: 'Namespaces',
      digest: 'Digest',
      btnConfirmTxt: 'Confirm',
      btnCancelTxt: 'Cancel'
    },
    table: {
      operate: 'operate'
    },
    rules: {
      name: 'Please enter the name of the registration center',
      address: 'Please enter the registration center address',
      namespaces: 'Please enter a namespace',
      centerType: 'Please select a centerType',
      orchestrationName: 'Please enter a orchestrationName',
      digest: 'Please enter a digest'
    }
  },
  orchestration: {
    serviceNode: 'Service node',
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
  }
}
