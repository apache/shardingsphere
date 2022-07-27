#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
<#if mode?exists>
    <#include "../mode/spring-boot-starter/${mode}.ftl" />
</#if>
<#if transaction!="local" && transaction!="base-seata">
    <#include "./transaction/${transaction}.ftl" />
</#if>
<#if framework?contains("mybatis")>

mybatis.mapper-locations=classpath*:mappers/*Mapper.xml
<#elseif framework?contains("jpa")>

spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5Dialect
spring.jpa.properties.hibernate.show_sql=false
</#if>

spring.shardingsphere.datasource.names=ds-0,ds-1,ds-2

spring.shardingsphere.datasource.ds-0.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-0.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds-0.jdbc-url=jdbc:mysql://${host}:${port}/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.shardingsphere.datasource.ds-0.username=${username}
spring.shardingsphere.datasource.ds-0.password=${(password)?string}
spring.shardingsphere.datasource.ds-0.max-active=16

spring.shardingsphere.datasource.ds-1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-1.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds-1.jdbc-url=jdbc:mysql://${host}:${port}/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.shardingsphere.datasource.ds-1.username=${username}
spring.shardingsphere.datasource.ds-1.password=${(password)?string}
spring.shardingsphere.datasource.ds-1.max-active=16

spring.shardingsphere.datasource.ds-2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-2.driver-class-name=com.mysql.cj.jdbc.Driver
spring.shardingsphere.datasource.ds-2.jdbc-url=jdbc:mysql://${host}:${port}/demo_ds_2?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.shardingsphere.datasource.ds-2.username=${username}
spring.shardingsphere.datasource.ds-2.password=${(password)?string}
spring.shardingsphere.datasource.ds-2.max-active=16
<#list feature?split(",") as item>
    <#include "${item}.ftl">
</#list>

spring.shardingsphere.props.sql-show=true
