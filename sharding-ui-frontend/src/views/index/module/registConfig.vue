<template>
  <el-card class="box-card">
    <div class="btn-group">
      <el-button type="primary" icon="el-icon-plus" @click="add">{{ $t("index.btnTxt") }}</el-button>
    </div>
    <div class="table-wrap">
      <el-table :data="tableData" border="" style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"/>
        <el-table-column :label="$t('index.table.operate')" fixed="right" width="140">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleConnect(scope.row)">{{ $t("common.connect") }}</el-button>
            <el-button type="text" size="small" @click="handlerDel(scope.row)">{{ $t("common.del") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog :title="$t('index.registDialog.title')" :visible.sync="regustDialogVisible">
      <el-form :model="form">
        <el-form-item :label="$t('index.registDialog.name')">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.address')">
          <el-input v-model="form.address" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('index.registDialog.namespaces')">
          <el-input v-model="form.namespaces" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="regustDialogVisible = false">{{ $t("index.registDialog.btnCancelTxt") }}</el-button>
        <el-button type="primary" @click="onConfirm">{{ $t("index.registDialog.btnConfirmTxt") }}</el-button>
      </div>
    </el-dialog>
  </el-card>
</template>
<script>
export default {
  name: 'RegistConfig',
  data() {
    return {
      regustDialogVisible: false,
      column: [
        {
          label: this.$t('index').registDialog.name,
          prop: 'date'
        },
        {
          label: this.$t('index').registDialog.address,
          prop: 'name'
        },
        {
          label: this.$t('index').registDialog.namespaces,
          prop: 'ss'
        }
      ],
      form: {
        name: '',
        address: '',
        namespaces: ''
      },
      tableData: []
    }
  },
  methods: {
    handleConnect(row) {
      this.$notify({
        title: this.$t('common').notify.title,
        message: this.$t('common').notify.conSucMessage,
        type: 'success'
      })
    },
    handlerDel(row) {
      this.$notify({
        title: this.$t('common').notify.title,
        message: this.$t('common').notify.delSucMessage,
        type: 'success'
      })
    },
    onConfirm() {
      this.regustDialogVisible = false
      this.$notify({
        title: this.$t('common').notify.title,
        message: this.$t('common').notify.conSucMessage,
        type: 'success'
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
</style>
