import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Container from '../src/components/Container/index.vue'

describe('Container/index.vue', () => {
  const wrapper = shallowMount(Container)
  expect(wrapper.isVueInstance()).to.be.true
})
