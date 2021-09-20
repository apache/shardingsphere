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

<#if target?? && target == "server">
<#include "modeType.ftl">

</#if>
<#if target?? && target == "conf">
<#include "confYamlTemplate.ftl">

</#if>
<#if ruleType?? && (ruleType?size>0)>
rules:
<#if ruleType?seq_contains("authority")>
<#include "authorityRuleYamlTemplate.ftl">
</#if>
<#if ruleType?seq_contains("encrypt")>
<#include "encryptRuleYamlTemplate.ftl">
</#if>
<#if ruleType?seq_contains("shadow")>
  <#include "shardingRuleYamlTemplate.ftl">
</#if>
<#if ruleType?seq_contains("readwrite_splitting")>
  <#include "readwriteSplittingRuleYamlTemplate.ftl">
</#if>
<#if ruleType?seq_contains("sharding")>
<#include "readwriteSplittingRuleYamlTemplate.ftl">
</#if>
</#if>

<#include "properties.ftl">
