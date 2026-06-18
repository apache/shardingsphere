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
    <#include "./mode/${mode}.ftl" />
</#if>

dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://${host}:${port}/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: ${username}
    password: ${(password)?string}
    maxPoolSize: 10
<#if feature!="encrypt" && feature!="mask">
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://${host}:${port}/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: ${username}
    password: ${(password)?string}
    maxPoolSize: 10
<#if feature!="shadow">
  ds_2:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://${host}:${port}/demo_ds_2?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
    username: ${username}
    password: ${(password)?string}
    maxPoolSize: 10
</#if>
</#if>

rules:
<#list feature?split(",") as item>
    <#include "./feature/${item}.ftl">
</#list>

<#if feature?contains("shadow")>
  <#include "sql-parse/sql-parse.ftl" />
</#if>

<#if transaction!="local" && transaction!="base-seata">
  <#include "./transaction/${transaction}.ftl" />
</#if>

props:
  sql-show: true
