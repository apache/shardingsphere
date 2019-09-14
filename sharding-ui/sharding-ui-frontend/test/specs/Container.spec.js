import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Container from '../../src/components/Container/index.vue'

describe('Container/index.vue', () => {
  it('Container Does the component exist？', () => {
    const wrapper = shallowMount(Container)
    expect(wrapper.isVueInstance()).to.be.true
  })

  it('setData()', () => {
    const wrapper = shallowMount(Container)
    wrapper.setData({ isCollapse: true })
    expect(wrapper.vm.isCollapse).to.equal(true)
  })

  it('onTogger()', () => {
    const wrapper = shallowMount(Container)
    const sHead = wrapper.find('.s-head')
    sHead.trigger('click')
    expect(wrapper.vm.isCollapse).to.equal(false)
  })
})
