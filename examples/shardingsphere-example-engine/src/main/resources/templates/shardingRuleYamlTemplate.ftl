<#--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<#if sharding??>
- !SHARDING
tables:
<#list tables?keys as key>
    ${key}:
    <#assign table= tables[key]>
    logicTable: ${table.logicTable}
    actualDataNodes:  ${table.actualDataNodes}
    <#if table[key].databaseStrategy??>
        databaseStrategy:
        <#assign shardingStrategy=table[key].databaseStrategy>
        <#include "shardingStrategyTemplate.ftl">
    </#if>
    <#if table[key].tableStrategy??>
        tableStrategy:
        <#assign shardingStrategy=table[key].tableStrategy>
        <#include "shardingStrategyTemplate.ftl">
    </#if>
    <#if table.keyGenerateStrategy??>
        keyGenerateStrategy:
        <#assign keyGenerateStrategy=table.keyGenerateStrategy>
        <#include "keyGenerateStrategyTemplate.ftl">
    </#if>
</#list>
autoTables:
<#list tables?keys as key>
    ${key}:
    <#assign table= tables[key]>
    logicTable: ${table.logicTable}
    actualDataNodes:  ${table.actualDataNodes}
    <#if table[key].shardingStrategy??>
        shardingStrategy:
        <#assign shardingStrategy=table[key].shardingStrategy>
        <#include "shardingStrategyTemplate.ftl">
    </#if>
    <#if table.keyGenerateStrategy??>
        keyGenerateStrategy:
        <#assign keyGenerateStrategy=table.keyGenerateStrategy>
        <#include "keyGenerateStrategyTemplate.ftl">
    </#if>
</#list>
bindingTables:
<#list bindingTables as bindingTable>
    ${bindingTable}
</#list>
broadcastTables:
<#list broadcastTables as broadcastTable>
    ${broadcastTable}
</#list>
defaultDatabaseStrategy:
<#assign shardingStrategy = defaultDatabaseStrategy>
<#include "shardingStrategyTemplate.ftl">
defaultTableStrategy:
<#assign shardingStrategy = defaultTableStrategy>
<#include "shardingStrategyTemplate.ftl">
<#if defaultKeyGenerateStrategy??>
    <#assign keyGenerateStrategy = defaultKeyGenerateStrategy>
    <#include "keyGenerateStrategyTemplate.ftl">
</#if>
<#if shardingAlgorithms??>
    <#assign shardingSphereAlgorithms=shardingAlgorithms>
    <#include "shardingSphereAlgorithmTemplate.ftl">
</#if>
<#if keyGenerators??>
    <#assign shardingSphereAlgorithms=keyGenerators>
    <#include "shardingSphereAlgorithmTemplate.ftl">
</#if>
<#if defaultShardingColumn??>
    defaultShardingColumn: ${defaultShardingColumn}
</#if>
</#if>

