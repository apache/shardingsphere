<template>
  <div class="schema">
    <el-row :gutter="10">
      <el-col
        v-for="(item, index) in schemaData"
        :key="index"
        :span="Math.ceil(24 / schemaData.length)"
      >
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>{{ item.title }}</span>
          </div>
          <div v-for="(itm, idex) in item.children" :key="idex" class="coll-item">
            <div :class="'itm icon-' + idex"/>
            <div class="txt">{{ itm }}</div>
            <i class="icon-edit" @click="handlerClick(item.title, itm)"/>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row>
      <el-button type="primary" icon="el-icon-plus" @click="add" />
    </el-row>
    <el-dialog :visible.sync="centerDialogVisible" :title="type" width="80%" top="3vh">
      <el-row :gutter="20">
        <el-col :span="12">
          <span style="font-size: 18px; font-weight: bold;">Edit source here:</span>
          <el-input
            :rows="20"
            v-model="textarea"
            type="textarea"
            placeholder="请输入内容"
            class="edit-text"
          />
        </el-col>
        <el-col :span="12">
          <span style="font-size: 18px; font-weight: bold;">Result (JS object dump):</span>
          <el-input
            :rows="20"
            v-model="textarea2"
            type="textarea"
            readonly
            placeholder="请输入内容"
            class="show-text"
          />
        </el-col>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="centerDialogVisible = false">{{ $t('btn.cancel') }}</el-button>
        <el-button type="primary" @click="onConfirm">{{ $t('btn.submit') }}</el-button>
      </span>
    </el-dialog>
    <el-dialog :visible.sync="addSchemaDialog" title="Add Schema" width="80%" top="3vh">
      <el-row>
        <el-col>
          <span style="font-size: 14px; color: #4a4a4a; font-weight: bold;">schema name:</span>
          <el-input class="width: 30%"/>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <span style="font-size: 14px; color: #4a4a4a; font-weight: bold;">ruleConfig:</span>
          <el-input
            :rows="18"
            v-model="rueleConfigTextArea"
            type="textarea"
            placeholder="请输入内容"
            class="edit-text"
          />
        </el-col>
        <el-col :span="12">
          <span style="font-size: 14px; color: #4a4a4a; font-weight: bold;">dataSourceConfig:</span>
          <el-input
            :rows="18"
            v-model="dataSourceConfigTextArea"
            type="textarea"
            placeholder="请输入内容"
            class="edit-text"
          />
        </el-col>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addSchemaDialog = false">{{ $t('btn.cancel') }}</el-button>
        <el-button type="primary" @click="addSchema">{{ $t('btn.submit') }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import yaml from 'js-yaml'
import API from '../api'

export default {
  name: 'Schema',
  data() {
    return {
      treeData: [],
      textarea: ``,
      rueleConfigTextArea: ``,
      dataSourceConfigTextArea: ``,
      schemaData: [],
      centerDialogVisible: false,
      addSchemaDialog: false,
      type: null,
      sname: '',
      scname: ''
    }
  },
  computed: {
    textarea2() {
      const dsYamlType = new yaml.Type(
        'tag:yaml.org,2002:io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration',
        {
          kind: 'mapping',
          construct(data) {
            return data !== null ? data : {}
          }
        }
      )
      const DS_SCHEMA = yaml.Schema.create(dsYamlType)
      return JSON.stringify(
        yaml.load(this.textarea, { schema: DS_SCHEMA }),
        null,
        '\t'
      )
    }
  },
  created() {
    this.getSchema()
  },
  methods: {
    add() {
      this.addSchemaDialog = true
    },
    handlerClick(parent, child) {
      if (child === 'rule') {
        API.getSchemaRule(parent).then(res => {
          this.renderYaml(parent, child, res)
        })
      } else {
        API.getSchemaDataSource(parent).then(res => {
          this.renderYaml(parent, child, res)
        })
      }
    },
    renderYaml(parent, child, res) {
      if (!res.success) return
      const model = res.model
      if (Object.prototype.toString.call(model) === '[object String]') {
        this.textarea = model
      } else {
        this.textarea = JSON.stringify(model, null, '\t')
      }
      this.sname = parent
      this.scname = child
      this.type = `${parent}-${child}`
      this.centerDialogVisible = true
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
    onConfirm() {
      if (this.scname === 'rule') {
        API.putSchemaRule(this.sname, { ruleConfig: this.textarea }).then(
          res => {
            this._onConfirm(res)
          }
        )
      } else {
        API.putSchemaDataSource(this.sname, {
          dataSourceConfig: this.textarea
        }).then(res => {
          this._onConfirm(res)
        })
      }
    },
    _onConfirm(res) {
      if (res.success) {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.updateCompletedMessage,
          type: 'success'
        })
        this.centerDialogVisible = false
      } else {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.updateFaildMessage,
          type: 'error'
        })
      }
    },
    addSchema() {
      console.log('add schema')
    }
  }
}
</script>
<style lang='scss'>
.schema {
  margin-top: 20px;
  .coll-item {
    height: 16px;
    line-height: 16px;
    width: 100%;
    float: left;
    margin-bottom: 22px;
    .txt {
      color: rgb(51, 51, 51);
      font-size: 14px;
      padding-left: 10px;
      float: left;
      margin-right: 10px;
    }
    .itm {
      float: left;
      width: 16px;
      height: 16px;
    }
    .icon-0 {
      background: url("../../../assets/img/rules.png") no-repeat left center;
    }
    .icon-1 {
      background: url("../../../assets/img/data-source.png") no-repeat left
        center;
    }
    .edit-btn {
      float: right;
    }
  }
  .el-row {
    margin-bottom: 20px;
  }
  .el-collapse-item__header {
    font-size: 16px;
  }
  .edit-text {
    margin-top: 5px;
    textarea {
      background: #fffffb;
    }
  }
  .show-text {
    margin-top: 5px;
    textarea {
      background: rgb(246, 246, 246);
    }
  }
  .icon-edit {
    background: url("../../../assets/img/edit.png") no-repeat left center;
    width: 16px;
    height: 16px;
    display: inline-block;
    float: right;
    cursor: pointer;
  }
  .el-dialog__body {
    padding: 10px 20px
  }
  .el-input {
    width: 30%;
  }
  .el-input__inner {
    height: 35px;
    line-height: 35px;
  }
}
</style>
