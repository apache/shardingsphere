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
      >{{ $t("configCenter.btnTxt") }}</el-button>
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
        <el-table-column :label="$t('configCenter.table.operate')" fixed="right" width="200">
          <template slot-scope="scope">
            <el-tooltip
              :content="$t('configCenter.table.operateEdit')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                :disabled="scope.row.activated"
                size="small"
                type="primary"
                icon="el-icon-edit"
                @click="handleEdit(scope.row)"
              />
            </el-tooltip>
            <el-tooltip
              :content="!scope.row.activated ? $t('configCenter.table.operateConnect'): $t('configCenter.table.operateConnected')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                type="primary"
                icon="el-icon-link"
                size="small"
                @click="handleConnect(scope.row)"
              />
            </el-tooltip>
            <el-tooltip
              :content="$t('configCenter.table.operateDel')"
              class="item"
              effect="dark"
              placement="top"
            >
              <el-button
                size="small"
                type="danger"
                icon="el-icon-delete"
                @click="handlerDel(scope.row)"
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
      :title="$t('configCenter.configDialog.title')"
      :visible.sync="regustDialogVisible"
      width="1010px"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="170px">
        <el-form-item :label="$t('configCenter.configDialog.name')" prop="name">
          <el-input :placeholder="$t('configCenter.rules.name')" v-model="form.name" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.centerType')" prop="instanceType">
          <el-radio-group v-model="form.instanceType">
            <el-radio label="Zookeeper">Zookeeper</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.address')" prop="serverLists">
          <el-input
            :placeholder="$t('configCenter.rules.address')"
            v-model="form.serverLists"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.orchestrationName')" prop="orchestrationName">
          <el-input
            :placeholder="$t('configCenter.rules.orchestrationName')"
            v-model="form.orchestrationName"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.namespaces')" prop="namespace">
          <el-input
            :placeholder="$t('configCenter.rules.namespaces')"
            v-model="form.namespace"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.digest')">
          <el-input
            :placeholder="$t('configCenter.rules.digest')"
            v-model="form.digest"
            autocomplete="off"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="regustDialogVisible = false">{{ $t("configCenter.configDialog.btnCancelTxt") }}</el-button>
        <el-button
          type="primary"
          @click="onConfirm('form')"
        >{{ $t("configCenter.configDialog.btnConfirmTxt") }}</el-button>
      </div>
    </el-dialog>
    <el-dialog
      :title="$t('configCenter.configDialog.editTitle')"
      :visible.sync="editDialogVisible"
      width="1010px"
    >
      <el-form ref="editForm" :model="editForm" :rules="rules" label-width="170px">
        <el-form-item :label="$t('configCenter.configDialog.name')" prop="name">
          <el-input :placeholder="$t('configCenter.rules.name')" v-model="editForm.name" autocomplete="off" />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.centerType')" prop="instanceType">
          <el-radio-group v-model="editForm.instanceType">
            <el-radio label="Zookeeper">Zookeeper</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.address')" prop="serverLists">
          <el-input
            :placeholder="$t('configCenter.rules.address')"
            v-model="editForm.serverLists"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.orchestrationName')" prop="orchestrationName">
          <el-input
            :placeholder="$t('configCenter.rules.orchestrationName')"
            v-model="editForm.orchestrationName"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.namespaces')" prop="namespace">
          <el-input
            :placeholder="$t('configCenter.rules.namespaces')"
            v-model="editForm.namespace"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('configCenter.configDialog.digest')">
          <el-input
            :placeholder="$t('configCenter.rules.digest')"
            v-model="editForm.digest"
            autocomplete="off"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEdit">{{ $t("configCenter.configDialog.btnCancelTxt") }}</el-button>
        <el-button
          type="primary"
          @click="confirmEdit('editForm')"
        >{{ $t("configCenter.configDialog.btnConfirmTxt") }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>
<script>
import { mapActions } from 'vuex'
import clone from 'lodash/clone'
import API from '../api'
export default {
  name: 'ConfigCenter',
  data() {
    return {
      regustDialogVisible: false,
      editDialogVisible: false,
      column: [
        {
          label: this.$t('configCenter').configDialog.name,
          prop: 'name'
        },
        {
          label: this.$t('configCenter').configDialog.centerType,
          prop: 'instanceType'
        },
        {
          label: this.$t('configCenter').configDialog.address,
          prop: 'serverLists'
        },
        {
          label: this.$t('configCenter').configDialog.namespaces,
          prop: 'namespace'
        },
        {
          label: this.$t('configCenter').configDialog.orchestrationName,
          prop: 'orchestrationName'
        }
      ],
      form: {
        name: '',
        serverLists: '',
        namespace: '',
        instanceType: 'Zookeeper',
        orchestrationName: '',
        orchestrationType: 'config_center',
        digest: ''
      },
      editForm: {
        primaryName: '',
        name: '',
        serverLists: '',
        namespace: '',
        instanceType: 'Zookeeper',
        orchestrationName: '',
        orchestrationType: 'config_center',
        digest: ''
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t('configCenter').rules.name,
            trigger: 'change'
          }
        ],
        serverLists: [
          {
            required: true,
            message: this.$t('configCenter').rules.address,
            trigger: 'change'
          }
        ],
        namespace: [
          {
            required: true,
            message: this.$t('configCenter').rules.namespace,
            trigger: 'change'
          }
        ],
        instanceType: [
          {
            required: true,
            message: this.$t('configCenter').rules.centerType,
            trigger: 'change'
          }
        ],
        orchestrationName: [
          {
            required: true,
            message: this.$t('configCenter').rules.orchestrationName,
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
    this.getRegCenter()
  },
  methods: {
    ...mapActions(['setRegCenterActivated']),
    handleCurrentChange(val) {
      const data = clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getRegCenter() {
      API.getConfigCenter().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
      this.getRegCenterActivated()
    },
    getRegCenterActivated() {
      API.getConfigCenterActivated().then(res => {
        this.setRegCenterActivated(res.model.name)
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
        API.postConfigCenterConnect(params).then(res => {
          this.$notify({
            title: this.$t('common').notify.title,
            message: this.$t('common').notify.conSucMessage,
            type: 'success'
          })
          this.getRegCenter()
        })
      }
    },
    handlerDel(row) {
      const params = {
        name: row.name
      }
      API.deleteConfigCenter(params).then(res => {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.delSucMessage,
          type: 'success'
        })
        this.getRegCenter()
      })
    },
    onConfirm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          API.postConfigCenter(this.form).then(res => {
            this.regustDialogVisible = false
            this.$notify({
              title: this.$t('common').notify.title,
              message: this.$t('common').notify.addSucMessage,
              type: 'success'
            })
            this.getRegCenter()
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    add() {
      this.regustDialogVisible = true
    },
    handleEdit(row) {
      this.editDialogVisible = true
      this.editForm = Object.assign({}, row)
      this.editForm.primaryName = row.name
    },
    confirmEdit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          API.updateConfigCenter(this.editForm).then(res => {
            this.editDialogVisible = false
            this.$notify({
              title: this.$t('common').notify.title,
              message: this.$t('common').notify.editSucMessage,
              type: 'success'
            })
            this.getRegCenter()
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    cancelEdit() {
      this.editDialogVisible = false
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
