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
  <div class="schema">
    <el-row :gutter="10">
      <el-col
        v-for="(item, index) in schemaData"
        :key="index"
        :xs="24"
        :sm="12"
        :md="6"
        :lg="4"
        :xl="3"
      >
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>{{ item.title }}</span>
          </div>
          <div v-for="(itm, idex) in item.children" :key="idex" class="coll-item">
            <div :class="'itm icon-' + idex" />
            <div class="txt">{{ itm }}</div>
            <i class="icon-edit" @click="handlerClick(item.title, itm)" />
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
            :placeholder="$t('ruleConfig.form.inputPlaceholder')"
            v-model="textarea"
            type="textarea"
            class="edit-text"
          />
        </el-col>
        <el-col :span="12">
          <span style="font-size: 18px; font-weight: bold;">Result (JS object dump):</span>
          <el-input
            :rows="20"
            :placeholder="$t('ruleConfig.form.inputPlaceholder')"
            v-model="textarea2"
            type="textarea"
            readonly
            class="show-text"
          />
        </el-col>
      </el-row>
      <span slot="footer" class="dialog-footer">
        <el-button @click="centerDialogVisible = false">{{ $t('btn.cancel') }}</el-button>
        <el-button type="primary" @click="onConfirm">{{ $t('btn.submit') }}</el-button>
      </span>
    </el-dialog>
    <el-dialog :visible.sync="addSchemaDialogVisible" title="Add Schema" width="80%" top="3vh">
      <el-form ref="form" :model="form" :rules="rules" label-width="170px">
        <el-form-item :label="$t('ruleConfig.schema.name')" prop="name">
          <el-input
            :placeholder="$t('ruleConfig.schemaRules.name')"
            v-model="form.name"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="$t('ruleConfig.schema.ruleConfig')" prop="ruleConfig">
          <el-input
            :placeholder="$t('ruleConfig.schemaRules.ruleConfig')"
            :rows="8"
            v-model="form.ruleConfig"
            autocomplete="off"
            type="textarea"
            class="edit-text"
          />
        </el-form-item>
        <el-form-item :label="$t('ruleConfig.schema.dataSourceConfig')" prop="dataSourceConfig">
          <el-input
            :placeholder="$t('ruleConfig.schemaRules.dataSourceConfig')"
            :rows="8"
            v-model="form.dataSourceConfig"
            autocomplete="off"
            type="textarea"
            class="edit-text"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addSchemaDialogVisible = false">{{ $t('btn.cancel') }}</el-button>
        <el-button type="primary" @click="addSchema('form')">{{ $t('btn.submit') }}</el-button>
      </div>
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
      schemaData: [],
      centerDialogVisible: false,
      type: null,
      sname: '',
      scname: '',
      addSchemaDialogVisible: false,
      schemaName: ``,
      rueleConfigTextArea: ``,
      dataSourceConfigTextArea: ``,
      form: {
        name: '',
        ruleConfig: '',
        dataSourceConfig: ''
      },
      rules: {
        name: [
          {
            required: true,
            message: this.$t('ruleConfig').schemaRules.name,
            trigger: 'change'
          }
        ],
        ruleConfig: [
          {
            required: true,
            message: this.$t('ruleConfig').schemaRules.ruleConfig,
            trigger: 'change'
          }
        ],
        dataSourceConfig: [
          {
            required: true,
            message: this.$t('ruleConfig').schemaRules.dataSourceConfig,
            trigger: 'change'
          }
        ]
      }
    }
  },
  computed: {
    textarea2() {
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
      this.addSchemaDialogVisible = true
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
    _onConfirm(res, type) {
      if (res.success) {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.updateCompletedMessage,
          type: 'success'
        })
        this.centerDialogVisible = false
        if (type === 'ADD_SCHEMA') {
          this.addSchemaDialogVisible = false
          this.getSchema()
        }
      } else {
        this.$notify({
          title: this.$t('common').notify.title,
          message: this.$t('common').notify.updateFaildMessage,
          type: 'error'
        })
      }
    },
    addSchema(form) {
      this.$refs[form].validate(valid => {
        if (valid) {
          API.addSchema({
            name: this.form.name,
            ruleConfiguration: this.form.ruleConfig,
            dataSourceConfiguration: this.form.dataSourceConfig
          }).then(res => {
            this._onConfirm(res, 'ADD_SCHEMA')
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
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
      background: url('../../../assets/img/rules.png') no-repeat left center;
    }
    .icon-1 {
      background: url('../../../assets/img/data-source.png') no-repeat left
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
    background: url('../../../assets/img/edit.png') no-repeat left center;
    width: 16px;
    height: 16px;
    display: inline-block;
    float: right;
    cursor: pointer;
  }
  .el-dialog__body {
    padding: 10px 20px;
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
