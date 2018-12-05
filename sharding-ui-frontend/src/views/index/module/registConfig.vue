<template>
  <el-card class="box-card">
    <div class="btn-group">
      <el-button type="primary" icon="el-icon-plus">{{ $t("index.btnTxt") }}</el-button>
    </div>
    <div class="table-wrap">
      <el-table :data="tableData" border="" style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"/>
        <el-table-column fixed="right" label="操作" width="140">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleConnect(scope.row)">{{ $t("common.connect") }}</el-button>
            <el-button type="text" size="small" @click="handlerDel(scope.row)">{{ $t("common.del") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>
</template>
<script>
export default {
  name: 'RegistConfig',
  data() {
    return {
      column: [
        {
          label: '注册中心名称',
          prop: 'date'
        },
        {
          label: '注册中心地址',
          prop: 'name'
        },
        {
          label: '命名空间',
          prop: 'ss'
        }
      ],
      tableData: [
        {
          date: '2016-05-03',
          name: '王小虎',
          ss: '上海市普陀区金沙江路 1518 弄',
          zip: 200333
        },
        {
          date: '2016-05-02',
          name: '王小虎',
          province: '上海',
          city: '普陀区',
          ss: '上海市普陀区金沙江路 1518 弄',
          zip: 200333
        },
        {
          date: '2016-05-04',
          name: '王小虎',
          province: '上海',
          city: '普陀区',
          ss: '上海市普陀区金沙江路 1518 弄',
          zip: 200333
        },
        {
          date: '2016-05-01',
          name: '王小虎',
          province: '上海',
          city: '普陀区',
          ss: '上海市普陀区金沙江路 1518 弄',
          zip: 200333
        }
      ]
    }
  },
  methods: {
    handleConnect(row) {
      console.log(row)
    },
    handlerDel(row) {

    }
  }
}
</script>
<style lang='scss' scoped>
  .btn-group {
    margin-bottom: 20px;
  }
</style>
