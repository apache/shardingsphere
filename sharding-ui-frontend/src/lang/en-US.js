export default {
  common: {
    menuData: [{
      title: 'Data governance',
      child: [{
        title: 'Registration configuration',
        href: '/'
      }, {
        title: 'Configuration management',
        href: '/config-manage'
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
    }
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
      orchestrationName: 'Please enter a orchestrationName'
    }
  }
}
