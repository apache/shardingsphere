export default {
  common: {
    menuData: [{
      title: 'Data governance',
      child: [{
        title: 'Registration configuration',
        href: '/'
      }, {
        title: 'Configuration management',
        href: '#/configuration-management'
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
    }
  }
}
