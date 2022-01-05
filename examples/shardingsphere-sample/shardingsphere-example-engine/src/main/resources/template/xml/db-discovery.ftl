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
    
    <bean id="ds_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://172.72.0.15:3306/ds_0?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
    </bean>
    
    <bean id="ds_0_replica_0" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://172.72.0.16:3306/ds_0_replica_0?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
    </bean>
    
    <bean id="ds_0_replica_1" class="com.zaxxer.hikari.HikariDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://172.72.0.17:3306/ds_0_replica_1?serverTimezone=UTC&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=UTF-8"/>
        <property name="username" value="root"/>
        <property name="password" value="root"/>
    </bean>
    
    <database-discovery:rule id="databaseDiscoveryRule">
        <database-discovery:data-source-rule id="db" data-source-names="ds_0,ds_0_replica_1,ds_0_replica_1" discovery-heartbeat-name="mgr-heartbeat"
                                             discovery-type-name="mgr"/>
        <database-discovery:discovery-heartbeat id="mgr-heartbeat">
            <props>
                <prop key="keep-alive-cron">0/5 * * * * ?</prop>
            </props>
        </database-discovery:discovery-heartbeat>
    </database-discovery:rule>
    
    <database-discovery:discovery-type id="mgr" type="MGR">
        <props>
            <prop key="keep-alive-cron">0/5 * * * * ?</prop>
            <prop key="group-name">aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa</prop>
        </props>
    </database-discovery:discovery-type>
    
    <shardingsphere:data-source id="dataSource" data-source-names="ds_0,ds_0_replica_1,ds_0_replica_1" rule-refs="databaseDiscoveryRule" />
