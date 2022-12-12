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

CREATE <#if indisunique!false >UNIQUE </#if>INDEX IF NOT EXISTS <#if isconcurrent!false >CONCURRENTLY </#if>${name}
ON ${schema}.${table} <#if amname?? >USING ${amname}</#if>
(<#list columns as c><#if c?counter != 1 >, </#if>${c.colname}<#if c.collspcname?? && c.collspcname?length gt 0> COLLATE ${c.collspcname}</#if><#if c.op_class?? >
${c.op_class}</#if><#if c.sort_order?? ><#if c.sort_order > DESC<#else> ASC</#if></#if><#if c.nulls?? > NULLS <#if c.nulls >
FIRST<#else>LAST</#if></#if></#list>)
<#if fillfactor?? >
WITH (FILLFACTOR=${fillfactor})
</#if><#if spcname?? >
TABLESPACE ${spcname}</#if><#if indconstraint?? >
WHERE ${indconstraint}</#if>;
