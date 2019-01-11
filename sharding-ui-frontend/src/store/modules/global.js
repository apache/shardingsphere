import * as types from '../mutation-types'

const state = {
  regCenterActivated: ''
}

const mutations = {
  [types.REG_CENTER_ACTIVATED](state, params) {
    state.regCenterActivated = params
  }
}

export default {
  state,
  mutations
}
