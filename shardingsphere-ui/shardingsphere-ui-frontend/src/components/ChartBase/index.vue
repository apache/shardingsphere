<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License") you may not use this file except in compliance with
  - the License.  You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="echarts"></div>
</template>

<script>
import echarts from 'echarts'
import debounce from 'lodash/debounce'
import { addListener, removeListener } from 'resize-detector'
import Vue from 'vue'

// enumerating ECharts events for now
const EVENTS = [
  'legendselectchanged',
  'legendselected',
  'legendunselected',
  'datazoom',
  'datarangeselected',
  'timelinechanged',
  'timelineplaychanged',
  'restore',
  'dataviewchanged',
  'magictypechanged',
  'geoselectchanged',
  'geoselected',
  'geounselected',
  'pieselectchanged',
  'pieselected',
  'pieunselected',
  'mapselectchanged',
  'mapselected',
  'mapunselected',
  'axisareaselected',
  'focusnodeadjacency',
  'unfocusnodeadjacency',
  'brush',
  'brushselected',
  'click',
  'dblclick',
  'mouseover',
  'mouseout',
  'mousedown',
  'mouseup',
  'globalout'
]

export default {
  props: {
    options: {
      type: Object,
      default() {
        return {}
      }
    },
    theme: {
      type: [String, Object],
      default: ''
    },
    initOptions: {
      type: Object,
      default() {
        return {}
      }
    },
    group: {
      type: String,
      default: ''
    },
    autoResize: {
      type: Boolean,
      default: true
    },
    watchShallow: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      chart: null
    }
  },
  computed: {
    // Only recalculated when accessed from JavaScript.
    // Won't update DOM on value change because getters
    // don't depend on reactive values
    width: {
      cache: false,
      get() {
        return this.delegateGet('width', 'getWidth')
      }
    },
    height: {
      cache: false,
      get() {
        return this.delegateGet('height', 'getHeight')
      }
    },
    isDisposed: {
      cache: false,
      get() {
        return !!this.delegateGet('isDisposed', 'isDisposed')
      }
    },
    computedOptions: {
      cache: false,
      get() {
        return this.delegateGet('computedOptions', 'getOption')
      }
    }
  },
  watch: {
    group(group) {
      this.chart.group = group
    }
  },
  created() {
    this.$watch(
      'options',
      options => {
        if (!this.chart && options) {
          this.init()
        } else {
          this.chart.setOption(this.options, true)
        }
      },
      { deep: !this.watchShallow }
    )
    const watched = ['theme', 'initOptions', 'autoResize', 'watchShallow']
    watched.forEach(prop => {
      this.$watch(
        prop,
        () => {
          this.refresh()
        },
        { deep: true }
      )
    })
  },
  mounted() {
    // auto init if `options` is already provided
    if (this.options) {
      this.init()
    }
  },
  activated() {
    if (this.autoResize) {
      this.chart && this.chart.resize()
    }
  },
  beforeDestroy() {
    if (!this.chart) {
      return
    }
    this.destroy()
  },
  methods: {
    // provide a explicit merge option method
    mergeOptions(options, notMerge, lazyUpdate) {
      this.delegateMethod('setOption', options, notMerge, lazyUpdate)
    },
    // just delegates ECharts methods to Vue component
    // use explicit params to reduce transpiled size for now
    appendData(params) {
      this.delegateMethod('appendData', params)
    },
    resize(options) {
      this.delegateMethod('resize', options)
    },
    dispatchAction(payload) {
      this.delegateMethod('dispatchAction', payload)
    },
    convertToPixel(finder, value) {
      return this.delegateMethod('convertToPixel', finder, value)
    },
    convertFromPixel(finder, value) {
      return this.delegateMethod('convertFromPixel', finder, value)
    },
    containPixel(finder, value) {
      return this.delegateMethod('containPixel', finder, value)
    },
    showLoading(type, options) {
      this.delegateMethod('showLoading', type, options)
    },
    hideLoading() {
      this.delegateMethod('hideLoading')
    },
    getDataURL(options) {
      return this.delegateMethod('getDataURL', options)
    },
    getConnectedDataURL(options) {
      return this.delegateMethod('getConnectedDataURL', options)
    },
    clear() {
      this.delegateMethod('clear')
    },
    dispose() {
      this.delegateMethod('dispose')
    },
    delegateMethod(name, ...args) {
      if (!this.chart) {
        Vue.util.warn(
          `Cannot call [${name}] before the chart is initialized. Set prop [options] first.`,
          this
        )
        return
      }
      return this.chart[name](...args)
    },
    delegateGet(name, method) {
      if (!this.chart) {
        Vue.util.warn(
          `Cannot get [${name}] before the chart is initialized. Set prop [options] first.`,
          this
        )
      }
      return this.chart[method]()
    },
    init() {
      if (this.chart) {
        return
      }

      const chart = echarts.init(this.$el, this.theme, this.initOptions)

      if (this.group) {
        chart.group = this.group
      }

      chart.setOption(this.options, true)

      // expose ECharts events as custom events
      EVENTS.forEach(event => {
        chart.on(event, params => {
          this.$emit(event, params)
        })
      })

      if (this.autoResize) {
        this.__resizeHanlder = debounce(
          () => {
            chart.resize()
          },
          100,
          { leading: true }
        )
        addListener(this.$el, this.__resizeHanlder)
      }

      this.chart = chart
    },
    destroy() {
      if (this.autoResize) {
        removeListener(this.$el, this.__resizeHanlder)
      }
      this.dispose()
      this.chart = null
    },
    refresh() {
      this.destroy()
      this.init()
    }
  },
  connect(group) {
    if (typeof group !== 'string') {
      group = group.map(chart => chart.chart)
    }
    echarts.connect(group)
  },
  disconnect(group) {
    echarts.disConnect(group)
  },
  registerMap(mapName, geoJSON, specialAreas) {
    echarts.registerMap(mapName, geoJSON, specialAreas)
  },
  registerTheme(name, theme) {
    echarts.registerTheme(name, theme)
  },
  graphic: echarts.graphic
}
</script>

<style>
.echarts {
  width: auto;
  height: 400px;
}
</style>
