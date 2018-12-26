/**
 *  系统 配置项
 */

let HOST
if (process.env.NODE_ENV === 'mock') {
  HOST = 'https://easy-mock.com/mock/5c1c861921d37d1c3c4dc5ae/ss-ui'
} else {
  HOST = 'http://47.100.190.183:8088'
}

export default {
  HOST,
  JSONP_HOST: ''
}