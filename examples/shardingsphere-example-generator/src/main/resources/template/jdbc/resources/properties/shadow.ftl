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

spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.production-data-source-name=ds-0
spring.shardingsphere.rules.shadow.data-sources.shadow-data-source.shadow-data-source-name=ds-1

spring.shardingsphere.rules.shadow.default-shadow-algorithm-name=simple-hint-algorithm

spring.shardingsphere.rules.shadow.tables.t_order.data-source-names=shadow-data-source
spring.shardingsphere.rules.shadow.tables.t_order.shadow-algorithm-names=user-id-insert-match-algorithm,user-id-delete-match-algorithm,user-id-select-match-algorithm,simple-hint-algorithm

spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.type=VALUE_MATCH
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.operation=insert
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.column=order_type
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-insert-match-algorithm.props.value=1

spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-delete-match-algorithm.type=VALUE_MATCH
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-delete-match-algorithm.props.operation=delete
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-delete-match-algorithm.props.column=order_type
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-delete-match-algorithm.props.value=1

spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-select-match-algorithm.type=VALUE_MATCH
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-select-match-algorithm.props.operation=select
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-select-match-algorithm.props.column=order_type
spring.shardingsphere.rules.shadow.shadow-algorithms.user-id-select-match-algorithm.props.value=1

spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.type=SIMPLE_HINT
spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.props.shadow=true
spring.shardingsphere.rules.shadow.shadow-algorithms.simple-hint-algorithm.props.foo=bar

spring.shardingsphere.rules.sql-parser.sql-comment-parse-enabled=true
