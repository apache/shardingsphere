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
      shadowDataSource:
        productionDataSourceName: ds
        shadowDataSourceName: shadow_ds
    tables:
      t_order:
        dataSourceNames:
          - shadowDataSource
        shadowAlgorithmNames:
          - user-id-insert-match-algorithm
          - user-id-select-match-algorithm
          - simple-hint-algorithm
    shadowAlgorithms:
      user-id-insert-match-algorithm:
        type: REGEX_MATCH
        props:
          operation: insert
          column: user_id
          regex: "[1]"
      user-id-select-match-algorithm:
        type: REGEX_MATCH
        props:
          operation: insert
          column: user_id
          regex: "[1]"
      simple-hint-algorithm:
        type: SIMPLE_HINT
        props:
          foo: bar
