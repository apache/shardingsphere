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
- !SHADOW
  dataSources:
    shadow_group:
      productionDataSourceName: ds_0
      shadowDataSourceName: ds_1
  tables:
    t_order:
      dataSourceNames:
        - shadow_group
      shadowAlgorithmNames:
        - user_id_insert_match_algorithm
        - sql_hint_algorithm
  defaultShadowAlgorithmName: sql-hint-algorithm
  shadowAlgorithms:
    user_id_insert_match_algorithm:
      type: VALUE_MATCH
      props:
        operation: insert
        column: order_type
        value: 1
    sql-hint-algorithm:
      type: SQL_HINT
      props:
        shadow: true
        foo: bar
