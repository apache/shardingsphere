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
        @click="add"
      >{{ $t("dataScaling.btnTxt") }}</el-button>
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
        <el-table-column :label="$t('dataScaling.tableList.operate')" fixed="right" width="140">
          <template slot-scope="scope">
            <el-tooltip
              :content="$t('dataScaling.tableList.operateStop')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                size="small"
                type="danger"
                icon="el-icon-delete"
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
        <el-form-item :label="$t('dataScaling.registDialog.source')" prop="source">
          <el-select v-model="form.source" :placeholder="$t('dataScaling.rules.source')" @change="selectChange">
            <el-option label="Schema" value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('dataScaling.registDialog.target')" prop="target">
          <el-radio-group v-model="form.target">
            <el-radio label="Proxy">Proxy</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="DataScalingDialogVisible = false">{{ $t("dataScaling.registDialog.btnCancelTxt") }}</el-button>
        <el-button
          type="primary"
          @click="onConfirm('form')"
        >{{ $t("dataScaling.registDialog.btnConfirmTxt") }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>
<script>
import { mapActions } from 'vuex'
import clone from 'lodash/clone'
import API from '../api'
export default {
  name: 'DataScalingIndex',
  data() {
    return {
      DataScalingDialogVisible: false,
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
      total: null
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
      API.getJobList().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
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
        name: row.name
      }
      API.deleteRegCenter(params).then(res => {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.delSucMessage,
          type: 'success'
        })
        this.getJobList()
      })
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
<style lang='scss' scoped>
.btn-group {
  margin-bottom: 20px;
}
.pagination {
  float: right;
  margin: 10px -10px 10px 0;
}
</style>
