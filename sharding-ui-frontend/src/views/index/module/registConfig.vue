<template>
  <el-row class="box-card">
    <div class="btn-group">
      <el-button type="primary" icon="el-icon-plus" @click="add">{{ $t("index.btnTxt") }}</el-button>
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
        <el-table-column :label="$t('index.table.operate')" fixed="right" width="140">
          <template slot-scope="scope">
            <el-button
              :disabled="scope.row.activated"
              type="primary"
              icon="icon-link iconfont"
              size="small"
              @click="handleConnect(scope.row)"
            />
            <el-button
              size="small"
              type="danger"
              icon="el-icon-delete"
              @click="handlerDel(scope.row)"
            />
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
      :title="$t('index.registDialog.title')"
      :visible.sync="regustDialogVisible"
      width="1010px"
    >
      <el-form ref="form" :model="form" :rules="rules" label-width="150px">
        <el-form-item :label="$t('index.registDialog.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.centerType')" prop="centerType">
          <el-radio-group v-model="form.centerType">
            <el-radio label="Zookeeper">Zookeeper</el-radio>
            <el-radio label="Etcd">Etcd</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.address')" prop="address">
          <el-input v-model="form.address" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.orchestrationName')" prop="orchestrationName">
          <el-input v-model="form.orchestrationName" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.namespaces')" prop="namespaces">
          <el-input v-model="form.namespaces" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.digest')">
          <el-input v-model="form.digest" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="regustDialogVisible = false">{{ $t("index.registDialog.btnCancelTxt") }}</el-button>
        <el-button
          type="primary"
          @click="onConfirm('form')"
        >{{ $t("index.registDialog.btnConfirmTxt") }}</el-button>
      </div>
    </el-dialog>
  </el-row>
</template>
<script>
import { mapActions } from 'vuex'
import _ from 'lodash'
import API from '../api'
export default {
  name: 'RegistConfig',
  data() {
    return {
      regustDialogVisible: false,
      column: [
        {
          label: this.$t('index').registDialog.name,
          prop: 'name'
        },
        {
          label: this.$t('index').registDialog.centerType,
          prop: 'registryCenterType'
        },
        {
          label: this.$t('index').registDialog.address,
          prop: 'serverLists'
        },
        {
          label: this.$t('index').registDialog.orchestrationName,
          prop: 'orchestrationName'
        },
        {
          label: this.$t('index').registDialog.namespaces,
          prop: 'namespace'
        }
      ],
      form: {
        name: '',
        address: '',
        namespaces: '',
        centerType: 'Zookeeper',
        orchestrationName: '',
        digest: ''
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t('index').rules.name,
            trigger: 'change'
          }
        ],
        address: [
          {
            required: true,
            message: this.$t('index').rules.address,
            trigger: 'change'
          }
        ],
        namespaces: [
          {
            required: true,
            message: this.$t('index').rules.namespaces,
            trigger: 'change'
          }
        ],
        centerType: [
          {
            required: true,
            message: this.$t('index').rules.centerType,
            trigger: 'change'
          }
        ],
        orchestrationName: [
          {
            required: true,
            message: this.$t('index').rules.orchestrationName,
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
      const data = _.clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getRegCenter() {
      API.getRegCenter().then(res => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = _.clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
      this.getRegCenterActivated()
    },
    getRegCenterActivated() {
      API.getRegCenterActivated().then(res => {
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
          // activated: row.activated,
          // digest: row.digest,
          name: row.name
          // namespace: row.namespace,
          // orchestrationName: row.orchestrationName,
          // registryCenterType: row.registryCenterType,
          // serverLists: row.serverLists
        }
        API.postRegCenterConnect(params).then(res => {
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
        // activated: row.activated,
        // digest: row.digest,
        name: row.name
        // namespace: row.namespace,
        // orchestrationName: row.orchestrationName,
        // registryCenterType: row.registryCenterType,
        // serverLists: row.serverLists
      }
      API.deleteRegCenter(params).then(res => {
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
          const params = {
            digest: this.form.digest,
            name: this.form.name,
            namespace: this.form.namespaces,
            orchestrationName: this.form.orchestrationName,
            registryCenterType: this.form.centerType,
            serverLists: this.form.address
          }
          API.postRegCenter(params).then(res => {
            this.regustDialogVisible = false
            this.$notify({
              title: this.$t('common').notify.title,
              message: this.$t('common').notify.conSucMessage,
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
