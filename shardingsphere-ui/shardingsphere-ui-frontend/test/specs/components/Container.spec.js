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
import { expect } from 'chai'
import { shallowMount } from '@vue/test-utils'
import Container from '../../../src/components/Container/index.vue'

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
