import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import locale from 'element-ui/lib/locale/lang/en'
import VueI18n from 'vue-i18n'
import Language from './lang/index'
import store from './store'
import Vuex from 'vuex'
import 'normalize.css/normalize.css'
import '@/assets/styles/index.scss'

Vue.config.productionTip = false
Vue.use(ElementUI, { locale })
Vue.use(VueI18n)
Vue.use(Vuex)

// language setting init
const navLang = navigator.language
const localLang = (navLang === 'zh-CN' || navLang === 'en-US') ? navLang : false
const lang = window.localStorage.getItem('language') || localLang || 'zh-CN'
Vue.config.lang = lang

// language setting
const locales = Language
const mergeZH = locales['zh-CN']
const mergeEN = locales['en-US']

const i18n = new VueI18n({
  locale: 'en-US',
  messages: {
    'zh-CN': mergeZH,
    'en-US': mergeEN
  }
})

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  components: { App },
  template: '<App/>'
})
