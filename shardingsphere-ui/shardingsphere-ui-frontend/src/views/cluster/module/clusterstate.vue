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
    <v-chart :options="getOption()"/>
  </div>
</template>
<script>
import ChartBase from '@/components/ChartBase'
import 'echarts-liquidfill'
import API from '../api'
export default {
  name: 'ClusterState',
  components: {
    'v-chart': ChartBase
  },
  data() {
    return {
      instanceData: {},
      proxy: [],
      datasource: [],
      categories: [],
      linesData: [],
      chartData: [],
      links: [],
      option: {},
      state: {
        ONLINE: '#01acca',
        OFFLINE: '#FF0A14',
        DISABLED: '#FF199D',
        UNKNOWN: '#ffb402'
      }
    }
  },
  created() {
    this.loadAllInstanceStates()
  },
  methods: {
    loadAllInstanceStates() {
      API.loadInstanceStates().then(res => {
        const data = res.model
        const instanceStates = data.instanceStates
        const dataSources = data.dataSourceStates
        this.instanceData = instanceStates
        this.initDatasource(dataSources)
        this.initProxy(instanceStates)
        this.initLines()
        this.setChartData()
      })
    },
    initProxy(data) {
      let x = 20
      for (const key in data) {
        this.proxy.push({
          name: key,
          value: [x, 130],
          category: this.getCategorie(data[key]),
          symbolSize: 100,
          label: {
            position: 'top'
          }
        })
        x += 50
      }
    },
    initDatasource(data) {
      let x = 0
      for (const key in data) {
        this.datasource.push({
          name: key,
          category: 0,
          state: data[key].state,
          speed: '',
          value: [x, 20]
        })
        x += 20
      }
    },
    initLines() {
      this.datasource.forEach((el) => {
        this.proxy.forEach((e2) => {
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
            color: new ChartBase.graphic.LinearGradient(0, 0, 1, 0, [{
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
            color: new ChartBase.graphic.LinearGradient(0, 0, 1, 0, [{
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
            color: new ChartBase.graphic.LinearGradient(0, 0, 1, 0, [{
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
            color: new ChartBase.graphic.LinearGradient(0, 0, 1, 0, [{
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
    getCategorie(nodeState) {
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
      this.initCategories()
      this.option = {
        legend: [{
          formatter: function(name) {
            return ChartBase.format.truncateText(name, 100, '14px Microsoft Yahei', '…');
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
