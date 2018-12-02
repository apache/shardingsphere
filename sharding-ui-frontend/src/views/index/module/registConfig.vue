<template>
  <el-card class="box-card">
    <el-button-group class="btn-group">
      <el-button type="primary" icon="el-icon-edit"/>
      <el-button type="primary" icon="el-icon-share"/>
      <el-button type="primary" icon="el-icon-delete"/>
    </el-button-group>
    <div class="table-wrap">
      <el-table :data="tableData" border="" style="width: 100%">
        <el-table-column
          v-for="(item, index) in column"
          :key="index"
          :prop="item.prop"
          :label="item.label"
          :width="item.width"/>
        <el-table-column fixed="right" label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleClick(scope.row)">查看</el-button>
            <el-button type="text" size="small">编辑</el-button>
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
    handleClick(row) {
      console.log(row)
    }
  }
}
</script>
<style lang='scss' scoped>
  .btn-group {
    margin-bottom: 20px;
  }
</style>
