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

    <shadow:shadow-algorithm id="user-id-insert-match-algorithm" type="VALUE_MATCH">
        <props>
            <prop key="operation">insert</prop>
            <prop key="column">user_type</prop>
            <prop key="value">1</prop>
        </props>
    </shadow:shadow-algorithm>

    <shadow:shadow-algorithm id="user-id-delete-match-algorithm" type="VALUE_MATCH">
        <props>
            <prop key="operation">delete</prop>
            <prop key="column">user_type</prop>
            <prop key="value">1</prop>
        </props>
    </shadow:shadow-algorithm>

    <shadow:shadow-algorithm id="user-id-select-match-algorithm" type="VALUE_MATCH">
        <props>
            <prop key="operation">select</prop>
            <prop key="column">user_type</prop>
            <prop key="value">1</prop>
        </props>
    </shadow:shadow-algorithm>

    <shadow:shadow-algorithm id="simple-hint-algorithm" type="SIMPLE_HINT">
        <props>
            <prop key="shadow">true</prop>
            <prop key="foo">bar</prop>
        </props>
    </shadow:shadow-algorithm>

    <shadow:rule id="shadowRule">
        <shadow:data-source id="shadow-data-source" source-data-source-name="demo_ds_0" shadow-data-source-name="demo_ds_1"/>
        <shadow:shadow-table name="t_user" data-sources="shadow-data-source">
            <shadow:algorithm shadow-algorithm-ref="user-id-insert-match-algorithm" />
            <shadow:algorithm shadow-algorithm-ref="user-id-delete-match-algorithm" />
            <shadow:algorithm shadow-algorithm-ref="user-id-select-match-algorithm" />
            <shadow:algorithm shadow-algorithm-ref="simple-hint-algorithm" />
        </shadow:shadow-table>
    </shadow:rule>

    <shardingsphere:data-source id="dataSource" data-source-names="demo_ds_0,demo_ds_1" rule-refs="shadowRule">
        <props>
            <prop key="sql-show">true</prop>
            <prop key="sql-comment-parse-enabled">true</prop>
        </props>
    </shardingsphere:data-source>
