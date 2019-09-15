import { expect } from 'chai'
import { shallowMount, createLocalVue, mount } from '@vue/test-utils'
import VueI18n from 'vue-i18n'
import Vuex from 'vuex'
import Login from '../../../src/views/login/index.vue'
import Language from '../../../src/lang/index'
import store from '../../../src/store'
import router from '../../../src/router'

const localVue = createLocalVue()

localVue.use(VueI18n)
localVue.use(Vuex)

// language setting init
const navLang = navigator.language
const localLang = navLang === 'zh-CN' || navLang === 'en-US' ? navLang : false
const lang = window.localStorage.getItem('language') || localLang || 'zh-CN'
localVue.config.lang = lang

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

describe('Login/index.vue', () => {
  it('Login Does the pages exist？', () => {
    const wrapper = shallowMount(Login, {
      localVue,
      i18n,
      store,
      router
    })
    expect(wrapper.isVueInstance()).to.be.true
  })
})
