export default {
  common: {
    menuData: [{
      title: '数据治理',
      child: [{
        title: '注册配置中心',
        href: '/'
      }, {
        title: '配置管理',
        href: '/config-manage'
      }]
    }],
    connect: '连接',
    del: '删除',
    notify: {
      title: '提示',
      conSucMessage: '连接成功',
      conFailMessage: '连接失败',
      delSucMessage: '删除成功',
      delFailMessage: '删除失败'
    }
  },
  login: {
    btnTxt: '登陆'
  },
  index: {
    btnTxt: '添加',
    registDialog: {
      title: '添加注册中心',
      name: '注册中心名称',
      address: '注册中心地址',
      namespaces: '命名空间',
      btnConfirmTxt: '确定',
      btnCancelTxt: '取消'
    },
    table: {
      operate: '操作'
    }
  }
}
