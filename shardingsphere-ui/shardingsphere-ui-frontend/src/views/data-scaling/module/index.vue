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
  <el-row class="box-card">
    <div class="btn-group">
      <el-button
        class="btn-plus"
        type="primary"
        icon="el-icon-plus"
        @click="add">{{ $t('dataScaling.btnTxt') }}</el-button>
    </div>
    <div class="table-wrap">
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"
        />
        <el-table-column
          :label="$t('dataScaling.tableList.operate')"
          fixed="right"
          width="140"
        >
          <template slot-scope="scope">
            <el-tooltip
              :content="$t('dataScaling.tableList.operateStop')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                size="small"
                type="primary"
                icon="el-icon-view"
                @click="handlerView(scope.row)"
              />
            </el-tooltip>
            <el-tooltip
              :content="$t('dataScaling.tableList.operateStop')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                size="small"
                type="danger"
                icon="el-icon-video-pause"
                @click="handlerStop(scope.row)"
              />
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          :total="total"
          :current-page="currentPage"
          background
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    <el-dialog
      :title="$t('dataScaling.registDialog.title')"
      :visible.sync="DataScalingDialogVisible"
      width="1010px"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="170px">
        <el-form-item
          :label="$t('dataScaling.registDialog.source')"
          prop="source"
        >
          <el-select
            v-model="form.source"
            :placeholder="$t('dataScaling.rules.source')"
            @change="selectChange"
          >
            <el-option label="Schema" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item
          :label="$t('dataScaling.registDialog.target')"
          prop="target"
        >
          <el-radio-group v-model="form.target">
            <el-radio label="Proxy">Proxy</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="DataScalingDialogVisible = false">{{
          $t('dataScaling.registDialog.btnCancelTxt')
        }}</el-button>
        <el-button type="primary" @click="onConfirm('form')">{{
          $t('dataScaling.registDialog.btnConfirmTxt')
        }}</el-button>
      </div>
    </el-dialog>
    <!-- syncTaskProgress -->
    <el-dialog
      :title="$t('dataScaling.registDialog.title')"
      :visible.sync="DataScalingDialogProgressVisible"
      width="1010px"
    >
      <el-form :inline="true">
        <el-form-item label="JobID">
          {{ progressRow.id }}
        </el-form-item>
        <el-form-item label="JobName">
          {{ progressRow.jobName }}
        </el-form-item>
        <el-form-item label="Status">
          {{ progressRow.status }}
        </el-form-item>
      </el-form>
      <el-row :gutter="12">
        <el-col
          v-for="(item, index) in progressRow.syncTaskProgress"
          :span="6"
          :key="index"
        >
          <el-card shadow="hover">
            <el-form :inline="true">
              <el-form-item label="syncTask">
                {{ item.id }}
              </el-form-item>
              <el-form-item label="syncStatic">
                {{ item.status }}
              </el-form-item>
              <el-form-item>
                <el-button
                  size="mini"
                  type="primary"
                  icon="el-icon-thumb"
                  @click="showSyncTaskProgressDetail(item)"
                ></el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer"></div>
    </el-dialog>
    <!-- syncTaskProgressDetail -->
    <el-dialog
      :title="$t('dataScaling.registDialog.title')"
      :visible.sync="DataScalingDialogSyncTaskProgressDetailVisible"
      width="1010px"
    >
      <el-form :inline="true">
        <el-form-item label="JobID">
          {{ syncTaskProgress.id }}
        </el-form-item>
        <el-form-item label="Status">
          {{ syncTaskProgress.status }}
        </el-form-item>
      </el-form>
      <div class="progress-list">
        <el-row class="progress-item">
          <el-col :span="2">
            <el-button
              size="mini"
              type="success"
              icon="el-icon-check"
              circle
            ></el-button>
          </el-col>
          <el-col :span="2"><div style="color: #333; font-weight: 500;">Preparing</div></el-col>
          <el-col :span="10" class="collapse-progress collapse-active">
            <el-progress
              :stroke-width="10"
              :percentage="100"
              :show-text="false"
            ></el-progress>
          </el-col>
        </el-row>
      </div>
      <el-collapse>
        <el-collapse-item>
          <template slot="title">
            <el-row class="collapse-row">
              <el-col :span="2">
                <el-button
                  size="mini"
                  type="success"
                  icon="el-icon-check"
                  circle
                ></el-button>
              </el-col>
              <el-col :span="2"><div>History</div></el-col>
              <el-col :span="20" class="collapse-progress">
                <el-progress :stroke-width="10" :percentage="70"></el-progress>
              </el-col>
            </el-row>
          </template>
          <el-row :gutter="12">
            <el-col
              v-for="(item, index) in syncTaskProgress.historySyncTaskProgress"
              :span="8"
              :key="index"
            >
              <el-card shadow="hover" style="margin-bottom: 10px">
                {{ item.id }}
                <v-chart :options="liquidFillOptions" />
              </el-card>
            </el-col>
          </el-row>
        </el-collapse-item>
        <el-collapse-item>
          <template slot="title">
            <el-row class="collapse-row">
              <el-col :span="2">
                <el-button
                  size="mini"
                  type="success"
                  icon="el-icon-check"
                  circle
                ></el-button>
              </el-col>
              <el-col :span="2"><div>Realtime</div></el-col>
            </el-row>
          </template>
          <div>
            delayMillisecond:
            {{
              this.$moment(
                syncTaskProgress.realTimeSyncTaskProgress &&
                  syncTaskProgress.realTimeSyncTaskProgress.delayMillisecond
              ).format('DD')
            }}s
          </div>
        </el-collapse-item>
      </el-collapse>
      <div slot="footer" class="dialog-footer"></div>
    </el-dialog>
  </el-row>
</template>
<script>
import Vue from 'vue'
import ECharts from 'vue-echarts'
import moment from 'moment'
import { mapActions } from 'vuex'
import clone from 'lodash/clone'
import 'echarts-liquidfill'
import API from '../api'

Vue.prototype.$moment = moment

export default {
  name: 'DataScalingIndex',
  components: {
    'v-chart': ECharts
  },
  data() {
    return {
      DataScalingDialogVisible: false,
      DataScalingDialogProgressVisible: false,
      DataScalingDialogSyncTaskProgressDetailVisible: false,
      schemaData: [],
      column: [
        {
          label: this.$t('dataScaling').tableList.jobId,
          prop: 'jobId'
        },
        {
          label: this.$t('dataScaling').tableList.jobName,
          prop: 'jobName'
        },
        {
          label: this.$t('dataScaling').tableList.status,
          prop: 'status'
        }
      ],
      form: {
        source: '',
        target: 'Proxy'
      },
      progressRow: {},
      syncTaskProgress: {},
      rules: {
        source: [
          {
            required: true,
            message: this.$t('dataScaling').rules.source,
            trigger: 'change'
          }
        ],
        target: [
          {
            required: true,
            message: this.$t('dataScaling').rules.target,
            trigger: 'change'
          }
        ]
      },
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null,
      liquidFillOptions: {
        series: [
          {
            type: 'liquidFill',
            radius: '90%',
            data: [0.6],
            outline: {
              show: false
            },
            label: {
              fontSize: 20
            }
          }
        ]
      }
    }
  },
  created() {
    this.getJobList()
  },
  methods: {
    ...mapActions(['setRegCenterActivated']),
    selectChange(item) {
      this.getSchema()
    },
    getSchema() {
      API.getSchema().then(res => {
        const data = res.model
        const base = ['rule', 'datasource']
        const newData = []
        for (const v of data) {
          newData.push({
            title: v,
            children: base
          })
        }
        this.schemaData = newData
      })
    },
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getJobList() {
      const data = [
        {
          jobId: 1,
          jobName: 'Local Sharding Scaling Job',
          status: 'RUNNING'
        }
      ]
      this.total = data.length
      this.cloneTableData = clone(data)
      this.tableData = data.splice(0, this.pageSize)

      // API.getJobList().then(res => {
      //   const data = res.model
      //   this.total = data.length
      //   this.cloneTableData = clone(res.model)
      //   this.tableData = data.splice(0, this.pageSize)
      // })
    },
    handleConnect(row) {
      if (row.activated) {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').connected,
          type: 'success'
        })
      } else {
        const params = {
          name: row.name
        }
        API.postRegCenterConnect(params).then(res => {
          this.$notify({
            title: this.$t('common').notify.title,
            message: this.$t('common').notify.conSucMessage,
            type: 'success'
          })
          this.getJobList()
        })
      }
    },
    handlerStop(row) {
      const params = {
        jobId: row.jobId
      }
      API.postJobStop(params).then(res => {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.delSucMessage,
          type: 'success'
        })
        this.getJobList()
      })
    },
    handlerView(row) {
      this.DataScalingDialogProgressVisible = true

      this.progressRow = {
        id: 1,
        jobName: 'Local Sharding Scaling Job',
        status: 'RUNNING/STOPPED',
        syncTaskProgress: [
          {
            id: '127.0.0.1-3306-test',
            status:
              'PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED',
            historySyncTaskProgress: [
              {
                id: 'history-test-t1#0',
                estimatedRows: 41147,
                syncedRows: 41147
              },
              {
                id: 'history-test-t1#1',
                estimatedRows: 42917,
                syncedRows: 42917
              },
              {
                id: 'history-test-t1#2',
                estimatedRows: 43543,
                syncedRows: 43543
              },
              {
                id: 'history-test-t2#0',
                estimatedRows: 39679,
                syncedRows: 39679
              },
              {
                id: 'history-test-t2#1',
                estimatedRows: 41483,
                syncedRows: 41483
              },
              {
                id: 'history-test-t2#2',
                estimatedRows: 42107,
                syncedRows: 42107
              }
            ],
            realTimeSyncTaskProgress: {
              id: 'realtime-test',
              delayMillisecond: 1576563771372,
              logPosition: {
                filename: 'ON.000007',
                position: 177532875,
                serverId: 0
              }
            }
          },
          {
            id: '127.0.0.1-3306-test',
            status:
              'PREPARING/MIGRATE_HISTORY_DATA/SYNCHRONIZE_REALTIME_DATA/STOPPING/STOPPED',
            historySyncTaskProgress: [
              {
                id: 'history-test-t1#0',
                estimatedRows: 41147,
                syncedRows: 41147
              },
              {
                id: 'history-test-t1#1',
                estimatedRows: 42917,
                syncedRows: 42917
              },
              {
                id: 'history-test-t1#2',
                estimatedRows: 43543,
                syncedRows: 43543
              },
              {
                id: 'history-test-t2#0',
                estimatedRows: 39679,
                syncedRows: 39679
              },
              {
                id: 'history-test-t2#1',
                estimatedRows: 41483,
                syncedRows: 41483
              },
              {
                id: 'history-test-t2#2',
                estimatedRows: 42107,
                syncedRows: 42107
              }
            ],
            realTimeSyncTaskProgress: {
              id: 'realtime-test',
              delayMillisecond: 1576563771372,
              logPosition: {
                filename: 'ON.000007',
                position: 177532875,
                serverId: 0
              }
            }
          }
        ]
      }
      // API.getJobProgress(row.jobId).then(res => {

      // })
    },
    showSyncTaskProgressDetail(item) {
      this.DataScalingDialogSyncTaskProgressDetailVisible = true
      this.syncTaskProgress = item
    },
    onConfirm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const params = {
            source: this.form.source,
            target: this.form.target
          }
          API.getJobStart(params).then(res => {
            this.DataScalingDialogVisible = false
            this.$notify({
              title: this.$t('common').notify.title,
              message: this.$t('common').notify.conSucMessage,
              type: 'success'
            })
            this.getJobList()
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    add() {
      this.DataScalingDialogVisible = true
    }
  }
}
</script>
<style lang="scss">
.btn-group {
  margin-bottom: 20px;
}
.pagination {
  float: right;
  margin: 10px -10px 10px 0;
}
.collapse-row {
  width: 100%;
  .collapse-progress {
    margin-top: 15px;
  }
}
.progress-item {
  height: 48px;
  line-height: 48px;
  .collapse-progress {
    margin-top: 15px;
    float: right;
  }
  .collapse-active {
    .el-progress-bar__inner:before {
      content: '';
      opacity: 0;
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: #fff;
      border-radius: 10px;
      animation: progress-active 2s ease-in-out infinite;
    }
  }
}
@keyframes progress-active {
  0% {
    opacity: 0.3;
    width: 0;
  }
  to {
    opacity: 0;
    width: 100%;
  }
}
.echarts {
  width: 300px;
  height: 200px;
}
</style>
