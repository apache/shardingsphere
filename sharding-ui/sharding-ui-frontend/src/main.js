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

import Vue from 'vue'
import App from './App'
import router from './router'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'
import VueI18n from 'vue-i18n'
import Language from './lang/index'
import store from './store'
import Vuex from 'vuex'
import 'normalize.css/normalize.css'
import '@/assets/styles/theme.scss'
import '@/assets/styles/index.scss'

Vue.config.productionTip = false
Vue.use(ElementUI, { locale })
Vue.use(VueI18n)
Vue.use(Vuex)

// language setting init
const lang = localStorage.getItem('language') || 'en-US'
Vue.config.lang = lang

// language setting
const locales = Language
const mergeZH = locales['zh-CN']
const mergeEN = locales['en-US']

const i18n = new VueI18n({
  locale: lang,
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
