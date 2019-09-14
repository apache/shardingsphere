import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Logo from '../../src/components/Logo/index.vue'

describe('Logo/index.vue', () => {
  it('Logo Does the component exist？', () => {
    const wrapper = shallowMount(Logo)
    expect(wrapper.find('.collapse-logo').html()).to.equal(
      '<img src="/_karma_webpack_/static/img/logo.80b4bf4.png" alt="logo" class="collapse-logo">'
    )
  })
})
