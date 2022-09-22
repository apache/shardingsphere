<#--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: ${r'ds_${0..1}.t_order_${0..1}'}
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: t_order_inline
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
      t_order_item:
        actualDataNodes: ${r'ds_${0..1}.t_order_item_${0..1}'}
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: t_order_item_inline
        keyGenerateStrategy:
          column: order_item_id
          keyGeneratorName: snowflake
        auditStrategy:
          auditorNames:
            - sharding_key_required_auditor
          allowHintDisable: true
    autoTables:
      t_order_auto:
        actualDataSources: ds_0
        shardingStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: t_order_inline
    bindingTables:
      - t_order,t_order_item
    broadcastTables:
      - t_address
    defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: database_inline
    defaultTableStrategy:
      none:
    defaultKeyGenerateStrategy:
      none:

    shardingAlgorithms:
      database_inline:
        type: INLINE
        props:
          algorithm-expression: ${r'ds_${user_id % 2}'}
      t_order_inline:
        type: INLINE
        props:
          algorithm-expression: ${r't_order_${order_id % 2}'}
      t_order_item_inline:
        type: INLINE
        props:
          algorithm-expression: ${r't_order_item_${order_id % 2}'}

    keyGenerators:
      snowflake:
        type: SNOWFLAKE

    auditors:
      sharding_key_required_auditor:
        type: DML_SHARDING_CONDITIONS
