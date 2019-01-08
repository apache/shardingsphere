<template>
  <div class="table-wrap">
    <el-table :data="tableData" border="" style="width: 100%">
      <el-table-column
        v-for="(item, index) in column"
        :key="index"
        :prop="item.prop"
        :label="item.label"
        :width="item.width"/>
      <el-table-column :label="$t('index.table.operate')" fixed="right" width="80" align="center">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.enabled"
            active-color="#13ce66"
            inactive-color="#ff4949"
            @change="handleChange(scope.row)"/>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        :total="total"
        :current-page="currentPage"
        background
        layout="prev, pager, next"
        @current-change="handleCurrentChange"/>
    </div>
  </div>
</template>

<script>
import _ from 'lodash'
import API from '../api'
export default {
  name: 'Instance',
  data() {
    return {
      column: [
        {
          label: this.$t('orchestration').instance.serverIp,
          prop: 'serverIp'
        },
        {
          label: this.$t('orchestration').instance.instanceId,
          prop: 'instanceId'
        }
      ],
      tableData: [],
      cloneTableData: [],
      currentPage: 1,
      pageSize: 10,
      total: null
    }
  },
  created() {
    this.getOrcheInstance()
  },
  methods: {
    handleCurrentChange(val) {
      const data = _.clone(this.cloneTableData)
      this.tableData = data.splice(val - 1, this.pageSize)
    },
    getOrcheInstance() {
      API.getOrcheInstance().then((res) => {
        const data = res.model
        this.total = data.length
        this.cloneTableData = _.clone(res.model)
        this.tableData = data.splice(0, this.pageSize)
      })
    },
    putOrcheInstance(row) {
      API.putOrcheInstance(row).then((res) => {
        this.getOrcheInstance()
      })
    },
    handleChange(row) {
      this.putOrcheInstance(row)
    }
  }
}
</script>

<style lang="scss" scoped>
  .table-wrap {
    margin-top: 20px;
  }
  .pagination {
    float: right;
    margin: 10px -10px 10px 0;
  }
</style>
