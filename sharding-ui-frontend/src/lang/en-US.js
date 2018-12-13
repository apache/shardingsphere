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
    connect: 'Connect',
    del: 'Delete',
    notify: {
      title: 'Prompt',
      conSucMessage: 'Connection succeeded',
      conFailMessage: 'Connection failed',
      delSucMessage: 'Delete succeeded',
      delFailMessage: 'Delete failed'
    }
  },
  login: {
    btnTxt: 'Login'
  },
  index: {
    btnTxt: 'ADD',
    registDialog: {
      title: 'Add a registry',
      name: 'Registration name',
      address: 'Registration address',
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
      namespaces: 'Please enter a namespace'
    }
  }
}
