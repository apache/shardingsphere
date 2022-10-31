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
    
    <sharding:sharding-algorithm id="databaseAlgorithm" type="INLINE">
        <props>
            <prop key="algorithm-expression">ds_${r'${user_id % 2}'}</prop>
        </props>
    </sharding:sharding-algorithm>
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" algorithm-ref="databaseAlgorithm" />
    
    <sharding:key-generate-algorithm id="snowflakeAlgorithm" type="SNOWFLAKE">
    </sharding:key-generate-algorithm>

    <sharding:audit-algorithm id="auditAlgorithm" type="DML_SHARDING_CONDITIONS">
    </sharding:audit-algorithm>
    
    <sharding:key-generate-strategy id="orderKeyGenerator" column="order_id" algorithm-ref="snowflakeAlgorithm" />
    <sharding:key-generate-strategy id="accountKeyGenerator" column="account_id" algorithm-ref="snowflakeAlgorithm" />
    <sharding:key-generate-strategy id="itemKeyGenerator" column="order_item_id" algorithm-ref="snowflakeAlgorithm" />

    <sharding:audit-strategy id="shardingKeyAudit" allow-hint-disable="true">
        <sharding:auditors>
            <sharding:auditor algorithm-ref="auditAlgorithm" />
        </sharding:auditors>
    </sharding:audit-strategy>
    
    <sharding:rule id="shardingRule">
        <sharding:table-rules>
            <!--todo add audit-->
            <sharding:table-rule logic-table="t_order" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="orderKeyGenerator" />
            <sharding:table-rule logic-table="t_order_item" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="itemKeyGenerator" />
            <sharding:table-rule logic-table="t_account" database-strategy-ref="databaseStrategy" key-generate-strategy-ref="accountKeyGenerator" />
        </sharding:table-rules>
        <sharding:binding-table-rules>
            <sharding:binding-table-rule logic-tables="t_order,t_order_item"/>
        </sharding:binding-table-rules>
        <sharding:broadcast-table-rules>
            <sharding:broadcast-table-rule table="t_address"/>
        </sharding:broadcast-table-rules>
    </sharding:rule>
