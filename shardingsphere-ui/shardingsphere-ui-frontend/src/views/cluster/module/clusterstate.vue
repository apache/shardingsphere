<!--
  - Licensed to the Apache Software Foundation (ASF) under one or more
  - contributor license agreements.  See the NOTICE file distributed with
  - this work for additional information regarding copyright ownership.
  - The ASF licenses this file to You under the Apache License, Version 2.0
  - (the "License"); you may not use this file except in compliance with
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
  <div style="width: 100%; height: 100%">
    <div id="myChart" class="echarts"></div>
  </div>
</template>
<script>

import echarts from 'echarts'
import Vue from 'vue'
Vue.prototype.$echarts = echarts
import 'echarts-liquidfill'
import API from '../api'
export default {
  name: 'ClusterState',
  data() {
    return {
      allData: {},
      instanceData: {},
      proxy: [],
      datasource: [],
      categories: [],
      linesData: [],
      links: [],
      option: {},
      state: {
        ONLINE: '#01acca',
        OFFLINE: '#FF0A14',
        DISABLED: '#FF199D',
        UNKNOWN: '#ffb402'
      },
      myChart: {},
      timer: {},
      refreshInterval: 60000
    }
  },
  mounted() {
    this.initChart()
    this.loadAllInstanceStates()
  },
  activated() {
    this.myChart && this.myChart.resize()
  },
  destroyed() {
    this.close()
  },
  methods: {
    refresh() {
      this.loadAllInstanceStates()
    },
    loadAllInstanceStates() {
      API.loadInstanceStates().then(res => {
        const data = res.model
        this.allData = data
        this.instanceData = data.instanceStates
        this.createChart()
      })
    },
    initChart() {
      this.initCategories()
      this.myChart = this.$echarts.init(document.getElementById('myChart'))
      this.myChart.setOption(this.getOption(), true)
      window.addEventListener("resize", () => { this.myChart.resize() })
      this.startTimer()
    },
    createChart() {
      this.initProxy()
      this.initDatasource()
      this.initLines()
      this.setChartData()
      this.myChart.setOption(this.option)
    },
    startTimer() {
      this.timer = setInterval(this.refresh, this.refreshInterval, this.refreshInterval)
    },
    initProxy() {
      this.proxy = []
      let x = 20
      for (const key in this.allData.instanceStates) {
        this.proxy.push({
          name: key,
          value: [x, 130],
          category: this.getCategory(this.allData.instanceStates[key]),
          symbolSize: 100,
          label: {
            position: 'top'
          }
        })
        x += 50
      }
    },
    initDatasource() {
      this.datasource = []
      let x = 0
      for (const key in this.allData.dataSourceStates) {
        this.datasource.push({
          name: key,
          category: 0,
          state: this.allData.dataSourceStates[key].state,
          speed: '',
          value: [x, 20]
        })
        x += 20
      }
    },
    initLines() {
      this.links = []
      this.linesData = []
      this.datasource.slice().forEach((el) => {
        this.proxy.slice().forEach((e2) => {
          const e3 = this.instanceData[e2.name].dataSources[el.name]
          if (e3) {
            if (e3.state === 'ONLINE') {
              this.linesData.push([{
                coord: e2.value
              }, {
                coord: el.value
              }])
            }
            this.links.push({
              source: e2.name,
              target: el.name,
              speed: el.speed,
              lineStyle: {
                normal: {
                  color: this.state[e3.state],
                  curveness: 0
                }
              }
            })
          }
        })
      })
    },
    initCategories() {
      this.categories = [{
        name: this.$t('clusterState').legendLabel.onLine,
        itemStyle: {
          normal: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
              offset: 0,
              color: '#01acca'
            }, {
              offset: 1,
              color: '#5adbe7'
            }])
          }
        },
        label: {
          normal: {
            fontSize: '14'
          }
        }
      }, {
        name: this.$t('clusterState').legendLabel.offLine,
        itemStyle: {
          normal: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
              offset: 0,
              color: '#FF0A14'
            }, {
              offset: 1,
              color: '#FF9387'
            }])
          }
        },
        label: {
          normal: {
            fontSize: '14'
          }
        }
      }, {
        name: this.$t('clusterState').legendLabel.disabled,
        itemStyle: {
          normal: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
              offset: 0,
              color: '#FF199D'
            }, {
              offset: 1,
              color: '#FF8EE6'
            }])
          }
        },
        label: {
          normal: {
            fontSize: '14'
          }
        }
      }, {
        name: this.$t('clusterState').legendLabel.unknown,
        itemStyle: {
          normal: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{
              offset: 0,
              color: '#ffb402'
            }, {
              offset: 1,
              color: '#ffdc84'
            }])
          }
        },
        label: {
          normal: {
            fontSize: '14'
          }
        }
      }]
    },
    getCategory(nodeState) {
      if (nodeState.state === 'ONLINE') {
        return 0
      } else if (nodeState.state === 'OFFLINE') {
        return 1
      } else if (nodeState.state === 'DISABLED') {
        return 2
      }
      return 3
    },
    setChartData() {
      this.option.series[0].data = this.proxy.concat(this.datasource)
      this.option.series[1].data = this.linesData
      this.option.series[0].links = this.links
    },
    getOption() {
      this.option = {
        legend: [{
          formatter: function(name) {
            return echarts.format.truncateText(name, 100, '14px Microsoft Yahei', '…');
          },
          tooltip: {
            show: true
          },
          textStyle: {
            color: '#999'
          },
          selectedMode: false,
          right: 0,
          data: this.categories.map(function(el) {
            return el.name
          })
        }],
        xAxis: {
          show: false,
          type: 'value'
        },
        yAxis: {
          show: false,
          type: 'value'
        },
        series: [{
          type: 'graph',
          layout: 'none',
          roam: 'scale',
          coordinateSystem: 'cartesian2d',
          symbolSize: 60,
          z: 3,
          edgeLabel: {
            normal: {
              show: true,
              textStyle: {
                fontSize: 14
              },
              formatter: function(params) {
                let txt = ''
                if (params.data.speed !== undefined) {
                  txt = params.data.speed
                }
                return txt
              },
            }
          },
          label: {
            normal: {
              show: true,
              position: 'bottom',
              color: '#5e5e5e'
            }
          },
          itemStyle: {
            normal: {
              shadowColor: 'none'
            },
            emphasis: {

            }
          },
          lineStyle: {
            normal: {
              width: 2,
              shadowColor: 'none'
            },
          },
          edgeSymbol: ['none', 'arrow'],
          edgeSymbolSize: 8,
          data: [],
          links: [],
          categories: this.categories
        }, {
          name: 'A',
          type: 'lines',
          coordinateSystem: 'cartesian2d',
          z: 1,
          effect: {
            show: true,
            smooth: false,
            trailLength: 0,
            symbol: "arrow",
            color: 'rgba(55,155,255,0.5)',
            symbolSize: 12
          },
          lineStyle: {
            normal: {
              curveness: 0
            }
          },
          data: []
        }]
      }
      return this.option
    },
    close() {
      clearTimeout(this.timer)
    }
  }
}
</script>
<style lang='scss' scoped>
.btn-group {
  margin-bottom: 20px;
}
.pagination {
  float: right;
  margin: 10px -10px 10px 0;
}
.echarts {
  height: 100%;
  width: 100%;
  padding-top: 10px;
}
</style>
