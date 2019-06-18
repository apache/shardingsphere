import * as types from './mutation-types'

const makeAction = type => {
  return ({ commit }, ...args) => commit(type, ...args)
}
// global actions
export const setRegCenterActivated = makeAction(types.REG_CENTER_ACTIVATED)
