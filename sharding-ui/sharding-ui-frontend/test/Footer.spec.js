import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Footer from '../src/components/Footer/index.vue'

describe('Footer/index.vue', () => {
  const wrapper = shallowMount(Footer)
  expect(wrapper.find('div').text()).contains('Copyright')
})
