import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Container from '../../src/components/Container/index.vue'

describe('Container/index.vue', () => {
  it('container组件是否存在', () => {
    const wrapper = shallowMount(Container)
    expect(wrapper.isVueInstance()).to.be.true
  })

  it('setData()方法', () => {
    const wrapper = shallowMount(Container)
    wrapper.setData({ isCollapse: true })
    expect(wrapper.vm.isCollapse).to.equal(true)
  })

  it('onTogger()方法', () => {
    const wrapper = shallowMount(Container)
    const sHead = wrapper.find('.s-head')
    sHead.trigger('click')
    expect(wrapper.vm.isCollapse).to.equal(false)
  })
})
