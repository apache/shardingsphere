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
      <span style="margin-left: 20px;">
        server: <samp style="color: #E17425;">{{ serviceForm.serviceName }}</samp>
        <i class="el-icon-edit" @click="showServerDialog"></i>
      </span>
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
              :content="$t('dataScaling.tableList.operateSee')"
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
                :disabled="scope.row.status === 'STOPPED'"
                icon="el-icon-video-pause"
                size="small"
                type="danger"
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
    <!-- add Dialog -->
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
            :disabled="!schemaData.length"
            @change="selectChange"
          >
            <el-option v-for="(item, index) in schemaData" :label="item" :value="item" :key="index"></el-option>
          </el-select>
          <span v-show="form.source" style="margin-left: 10px">
            <el-button type="primary" size="mini" @click="showDatasource">Datasource</el-button>
            <el-button type="primary" size="mini" @click="showRule">Rule</el-button>
          </span>
        </el-form-item>
        <el-form-item
          :label="$t('dataScaling.registDialog.target')"
          prop="target"
        >
          <el-radio-group v-model="form.target">
            <el-radio label="Proxy">Proxy</el-radio>
          </el-radio-group>
        </el-form-item>
        <div v-show="schemaData.length > 0">
          <el-form-item
            :label="$t('dataScaling.registDialog.username')"
            prop="username"
          >
            <el-input v-model="form.username" :placeholder="$t('dataScaling.registDialog.usernamePlaceholder')"></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('dataScaling.registDialog.password')"
            prop="password"
          >
            <el-input v-model="form.password" :placeholder="$t('dataScaling.registDialog.passwordPlaceholder')"></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('dataScaling.registDialog.url')"
            prop="url"
          >
            <el-input v-model="form.url" :placeholder="$t('dataScaling.registDialog.urlPlaceholder')"></el-input>
          </el-form-item>
          <el-form-item
            :label="$t('dataScaling.registDialog.jobCount')"
            prop="jobCount"
          >
            <el-input v-model="form.jobCount" :placeholder="$t('dataScaling.registDialog.jobCountPlaceholder')"></el-input>
          </el-form-item>
        </div>
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
    <!-- showDatasource -->
    <el-dialog
      :visible.sync="DatasourceVisible"
      title="Result Datasource:"
      width="1010px"
    >
      <el-row>
        <el-col :span="24">
          <el-input
            :rows="20"
            v-model="textareaDatasourceCom"
            type="textarea"
            readonly
            class="show-text"
          />
        </el-col>
      </el-row>
      <div slot="footer" class="dialog-footer"></div>
    </el-dialog>
    <!-- showRule -->
    <el-dialog
      :visible.sync="RuleVisible"
      title="Result Rule:"
      width="1010px"
    >
      <el-row>
        <el-col :span="24">
          <el-input
            :rows="20"
            v-model="textareaRuleCom"
            type="textarea"
            readonly
            class="show-text"
          />
        </el-col>
      </el-row>
    </el-dialog>
    <!-- syncTaskProgress -->
    <el-dialog
      :visible.sync="DataScalingDialogProgressVisible"
      width="1010px"
      @close="close()"
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
              <el-form-item label="syncStatus">
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
    </el-dialog>
    <!-- syncTaskProgressDetail -->
    <el-dialog
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
          <el-col :span="6" class="collapse-progress collapse-active">
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
            <el-row class="collapse-row progress-item">
              <el-col :span="2">
                <el-button
                  size="mini"
                  type="success"
                  icon="el-icon-check"
                  circle
                ></el-button>
              </el-col>
              <el-col :span="2"><div>History</div></el-col>
              <el-col :span="6" class="collapse-progress">
                <el-progress :stroke-width="10" :percentage="percentageComputed"></el-progress>
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
                <v-chart :options="getOption(item)" />
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
          <el-row class="progress-item">
            <el-col :span="10">
              <span style="color: #333; font-weight: 500;">delayMillisecond:</span>
              {{
                this.$moment(
                  syncTaskProgress.realTimeSyncTaskProgress &&
                    syncTaskProgress.realTimeSyncTaskProgress.delayMillisecond
                ).format('s')
              }}s
            </el-col>
          </el-row>
        </el-collapse-item>
      </el-collapse>
    </el-dialog>
    <el-dialog
      :visible.sync="serverDialogVisible"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      title="Data Scaling Setting"
      width="480px"
      center>
      <el-form label-width="110px">
        <el-form-item label="Service Name:">
          <el-input v-model="serviceForm.serviceName" :placeholder="$t('dataScaling.serviceDialog.serviceName')"/>
        </el-form-item>
        <el-form-item label="Service Url:">
          <el-input v-model="serviceForm.serviceUrl" :placeholder="$t('dataScaling.serviceDialog.serviceUrl')"/>
        </el-form-item>
        <el-form-item>
          <el-button @click="serverDialogVisible = false">{{
            $t('dataScaling.registDialog.btnCancelTxt')
          }}</el-button>
          <el-button type="primary" @click="setServer">
            {{ $t('dataScaling.registDialog.btnConfirmTxt') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-row>
</template>
<script>
import yaml from 'js-yaml'
import Vue from 'vue'
import ChartBase from '@/components/ChartBase'
import moment from 'moment'
import clone from 'lodash/clone'
import isEmpty from 'lodash/isEmpty'
import 'echarts-liquidfill'
import API from '../api'

Vue.prototype.$moment = moment

/**
 * 保留n位小数
 */
const nDecimal = (num = 0, n = 0) => {
  if (num === null) return '--'
  let f_x = parseFloat(num)
  if (isNaN(f_x)) {
    console.log('function:changeTwoDecimal->parameter error')
    return false
  }

  if (!n) return parseInt(f_x)

  f_x = Math.round(num * 100) / 100
  let s_x = f_x.toString()
  let pos_decimal = s_x.indexOf('.')
  if (pos_decimal < 0) {
    pos_decimal = s_x.length
    s_x += '.'
  }
  while (s_x.length <= pos_decimal + n) {
    s_x += '0'
  }
  return s_x
}

let timer = null

export default {
  name: 'DataScalingIndex',
  components: {
    'v-chart': ChartBase
  },
  data() {
    return {
      DataScalingDialogVisible: false,
      DataScalingDialogProgressVisible: false,
      DataScalingDialogSyncTaskProgressDetailVisible: false,
      DatasourceVisible: false,
      serverDialogVisible: false,
      RuleVisible: false,
      serviceForm: {
        serviceName: '',
        serviceType: 'ShardingScaling',
        serviceUrl: ''
      },
      schemaData: [],
      textareaDatasource: ``,
      textareaRule: ``,
      serverHost: '',
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
        target: 'Proxy',
        username: '',
        password: '',
        url: '',
        jobCount: '3'
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
        ],
        username: [
          {
            required: true,
            message: this.$t('dataScaling').registDialog.usernamePlaceholder,
            trigger: 'change'
          }
        ],
        password: [
          {
            required: true,
            message: this.$t('dataScaling').registDialog.passwordPlaceholder,
            trigger: 'change'
          }
        ],
        url: [
          {
            required: true,
            message: this.$t('dataScaling').registDialog.urlPlaceholder,
            trigger: 'change'
          }
        ],
        jobCount: [
          {
            required: true,
            message: this.$t('dataScaling').registDialog.jobCountPlaceholder,
            trigger: 'change'
          }
        ]
      },
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null
    }
  },
  computed: {
    textareaDatasourceCom() {
      const dsYamlType = new yaml.Type(
        'tag:yaml.org,2002:org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration',
        {
          kind: 'mapping',
          construct(data) {
            return data !== null ? data : {}
          }
        }
      )
      const DS_SCHEMA = yaml.Schema.create(dsYamlType)
      return JSON.stringify(
        yaml.load(this.textareaDatasource, { schema: DS_SCHEMA }),
        null,
        '\t'
      )
    },
    textareaRuleCom() {
      const dsYamlType = new yaml.Type(
        'tag:yaml.org,2002:org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration',
        {
          kind: 'mapping',
          construct(data) {
            return data !== null ? data : {}
          }
        }
      )
      const DS_SCHEMA = yaml.Schema.create(dsYamlType)
      return JSON.stringify(
        yaml.load(this.textareaRule, { schema: DS_SCHEMA }),
        null,
        '\t'
      )
    },
    percentageComputed() {
      const arr = this.syncTaskProgress.historySyncTaskProgress
      if (!arr) return
      let sumEstimatedRows = 0
      let sumSyncedRows = 0
      for (const v of arr) {
        sumEstimatedRows += v.estimatedRows
        sumSyncedRows += v.syncedRows
      }
      let res = 0
      if (sumEstimatedRows) {
        res = sumSyncedRows / sumEstimatedRows
      }
      return nDecimal(res * 100, 0)
    }
  },
  created() {
    this.getJobServer()
  },
  methods: {
    showServerDialog() {
      this.serverDialogVisible = true
    },
    setServer() {
      if (this.serviceForm.serviceUrl) {
        API.postJobServer(this.serviceForm).then(res => {
          this.$notify({
            title: this.$t('dataScaling').notify.title,
            message: 'Set up successfully！',
            type: 'success'
          })
          this.serverDialogVisible = false
        }, () => {
          this.$notify({
            title: this.$t('dataScaling').notify.title,
            message: 'Setup failed！',
            type: 'error'
          })
        })
      } else {
        this.$notify({
          title: this.$t('dataScaling').notify.title,
          message: this.$t('dataScaling').rules.serviceUrl,
          type: 'error'
        })
      }
    },
    getJobServer() {
      API.getJobServer().then(res => {
        const { model } = res
        if (model) {
          const { serviceName, serviceType, serviceUrl } = model
          this.serviceForm = {
            serviceName,
            serviceType,
            serviceUrl
          }
          this.getJobList()
        } else {
          this.serverDialogVisible = true
        }
      }, () => {
        this.serverDialogVisible = true
      })
    },
    getPercentage(arr) {
      if (!arr) return
      let sumEstimatedRows = ''
      let sumSyncedRows = ''
      for (const v of arr) {
        sumEstimatedRows += v.estimatedRows
        sumSyncedRows += v.syncedRows
      }
      let res = 0
      if (sumEstimatedRows) {
        res = sumSyncedRows / sumEstimatedRows
      }
      return nDecimal(res, 2) * 100
    },
    getOption(obj) {
      let data = 0
      if (obj.estimatedRows) {
        data = obj.syncedRows / obj.estimatedRows
      }
      const option = {
        series: [
          {
            type: 'liquidFill',
            radius: '90%',
            data: [data],
            outline: {
              show: false
            },
            label: {
              fontSize: 20
            }
          }
        ]
      }
      return option
    },
    selectChange(item) {
      this.getSchemaDataSource(item)
      this.getSchemaRule(item)
    },
    getSchemaDataSource(schemaName) {
      API.getSchemaDataSource(schemaName).then(res => {
        const { model } = res
        if (Object.prototype.toString.call(model) === '[object String]') {
          this.textareaDatasource = model
        } else {
          this.textareaDatasource = JSON.stringify(model, null, '\t')
        }
      })
    },
    getSchemaRule(schemaName) {
      API.getSchemaRule(schemaName).then(res => {
        const { model } = res
        if (Object.prototype.toString.call(model) === '[object String]') {
          this.textareaRule = model
        } else {
          this.textareaRule = JSON.stringify(model, null, '\t')
        }
      })
    },
    getSchema() {
      API.getSchema().then(res => {
        this.schemaData = res.model
      })
    },
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getJobList() {
      API.getJobList().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
    },
    handlerStop(row) {
      const params = {
        jobId: row.jobId
      }
      API.postJobStop(params).then(res => {
        this.$notify({
          title: this.$t('dataScaling').notify.title,
          message: this.$t('dataScaling').notify.delSucMessage,
          type: 'success'
        })
        this.getJobList()
      })
    },
    getJobProgress(row) {
      const { jobId, status } = row
      API.getJobProgress(jobId).then(res => {
        const { model } = res
        this.progressRow = model
        if (!isEmpty(this.syncTaskProgress)) {
          for (const v of model.syncTaskProgress) {
            if (v.id === this.syncTaskProgress.id) {
              this.syncTaskProgress = v
            }
          }
        }
        clearTimeout(timer)
        if (status !== 'STOPPED') {
          timer = setTimeout(() => {
            this.getJobProgress(row)
            clearTimeout(timer)
          }, 2000)
        }
      })
    },
    handlerView(row) {
      this.DataScalingDialogProgressVisible = true
      this.getJobProgress(row)
    },
    showSyncTaskProgressDetail(item) {
      this.DataScalingDialogSyncTaskProgressDetailVisible = true
      this.syncTaskProgress = item
    },
    onConfirm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const { username, password, url, jobCount } = this.form
          const params = {
            ruleConfiguration: {
              sourceDatasource: this.textareaDatasource,
              sourceRule: this.textareaRule,
              // sourceDatasource: "ds_0: !!YamlDataSourceConfiguration\n  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n  properties:\n    jdbcUrl: jdbc:mysql://sharding-scaling-mysql:3306/test?serverTimezone=UTC&useSSL=false\n    username: root\n    password: '123456'\n    connectionTimeout: 30000\n    idleTimeout: 60000\n    maxLifetime: 1800000\n    maxPoolSize: 50\n    minPoolSize: 1\n    maintenanceIntervalMilliseconds: 30000\n    readOnly: false\n",
              // sourceRule: "defaultDatabaseStrategy:\n  inline:\n    algorithmExpression: ds_${user_id % 2}\n    shardingColumn: user_id\ntables:\n  t1:\n    actualDataNodes: ds_0.t1\n    keyGenerator:\n      column: order_id\n      type: SNOWFLAKE\n    logicTable: t1\n    tableStrategy:\n      inline:\n        algorithmExpression: t1\n        shardingColumn: order_id\n  t2:\n    actualDataNodes: ds_0.t2\n    keyGenerator:\n      column: order_item_id\n      type: SNOWFLAKE\n    logicTable: t2\n    tableStrategy:\n      inline:\n        algorithmExpression: t2\n        shardingColumn: order_id\n",
              destinationDataSources: {
                username,
                password,
                // url: 'jdbc:mysql://sharding-scaling-mysql:3306/test2?serverTimezone=UTC&useSSL=false'
                url
              }
            },
            jobConfiguration: {
              concurrency: jobCount
            }
          }
          API.getJobStart(params).then(res => {
            this.DataScalingDialogVisible = false
            this.$notify({
              title: this.$t('dataScaling').notify.title,
              message: this.$t('dataScaling').notify.conSucMessage,
              type: 'success'
            })
            this.clearForm()
            this.getJobList()
          })
        } else {
          return false
        }
      })
    },
    clearForm() {
      this.form = {
        source: '',
        target: 'Proxy',
        username: '',
        password: '',
        url: '',
        jobCount: '3'
      }
    },
    add() {
      this.DataScalingDialogVisible = true
      this.getSchema()
    },
    showDatasource() {
      this.DatasourceVisible = true
    },
    showRule() {
      this.RuleVisible = true
    },
    close() {
      clearTimeout(timer)
    }
  }
}
</script>
<style lang="scss">
  .el-icon-edit {
    cursor: pointer;
  }
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
