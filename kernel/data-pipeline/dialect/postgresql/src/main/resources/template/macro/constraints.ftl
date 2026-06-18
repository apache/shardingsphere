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

<#macro PRIMARY_KEY data>
<#if data.columns?size gt 0 >
<#if data.name?? >CONSTRAINT ${data.name} </#if>PRIMARY KEY (<#list data.columns as c>
<#if c?counter != 1 >, </#if>${c.column}</#list>)<#if data.include?size gt 0 >
INCLUDE(<#list data.include as col ><#if col?counter != 1 >, </#if>${col}</#list>)</#if>
<#if data.fillfactor?? >
WITH (FILLFACTOR=${data.fillfactor})</#if>
<#if data.spcname?? && data.spcname != "pg_default" >
USING INDEX TABLESPACE ${data.spcname }</#if>
<#if data.condeferrable!false >
DEFERRABLE<#if data.condeferred!false > INITIALLY DEFERRED</#if></#if>
</#if>
</#macro>

<#macro UNIQUE unique_data >
<#list unique_data as data >
<#if data.columns?size gt 0 ><#if data?counter !=1 >,</#if>
<#if data.name?? >CONSTRAINT ${data.name} </#if>UNIQUE (<#list data.columns as c>
<#if c?counter != 1 >, </#if>${c.column}</#list>)<#if data.include?size gt 0 >
INCLUDE(<#list data.include as col><#if col?counter != 1 >, </#if>${col}</#list>)</#if>
<#if data.fillfactor?? >
WITH (FILLFACTOR=${data.fillfactor})</#if>
<#if data.spcname?? && data.spcname != "pg_default" >
USING INDEX TABLESPACE ${data.spcname}</#if>
<#if data.condeferrable!false >
DEFERRABLE<#if data.condeferred!false > INITIALLY DEFERRED</#if></#if>
</#if>
</#list>
</#macro>

<#macro FOREIGN_KEY foreign_key_data >
<#list foreign_key_data as data><#if data?counter != 1 >,</#if >
<#if data.name?? >CONSTRAINT ${data.name} </#if >FOREIGN KEY (<#list data.columns as columnobj ><#if columnobj?counter != 1 >
, </#if >${columnobj.local_column}</#list>)
REFERENCES ${data.remote_schema}${data.remote_table } (<#list data.columns as columnobj><#if columnobj?counter != 1 >
, </#if >${columnobj.referenced}</#list>) <#if data.confmatchtype!false >MATCH FULL<#else >MATCH SIMPLE</#if>
ON UPDATE<#if data.confupdtype  == 'a' >
NO ACTION<#elseif data.confupdtype  == 'r' >
RESTRICT<#elseif data.confupdtype  == 'c' >
CASCADE<#elseif data.confupdtype  == 'n' >
SET NULL<#elseif data.confupdtype  == 'd' >
SET DEFAULT</#if >
ON DELETE<#if data.confdeltype  == 'a' >
NO ACTION<#elseif data.confdeltype  == 'r' >
RESTRICT<#elseif data.confdeltype  == 'c' >
CASCADE<#elseif data.confdeltype  == 'n' >
SET NULL<#elseif data.confdeltype  == 'd' >
SET DEFAULT</#if >
<#if data.condeferrable >
DEFERRABLE<#if data.condeferred >
INITIALLY DEFERRED</#if>
</#if>
<#if !data.convalidated >
NOT VALID</#if>
</#list>
</#macro>

<#macro CHECK check_data >
<#list check_data as data><#if data?counter !=1 >,</#if >
<#if data.name?? >CONSTRAINT ${data.name } </#if>CHECK (${ data.consrc })<#if data.convalidated!false >
NOT VALID</#if ><#if data.connoinherit!false > NO INHERIT</#if >
</#list>
</#macro>

<#macro EXCLUDE exclude_data >
<#list exclude_data as data ><#if data?counter != 1 >,</#if >
<#if data.name?? >CONSTRAINT ${data.name } </#if>EXCLUDE <#if data.amname?? && data.amname != '' >USING ${data.amname}</#if > (
<#list data.columns as col><#if col?counter != 1 >,
</#if ><#if col.is_exp!false >${col.column}<#else >${col.column}</#if ><#if col.oper_class?? && col.oper_class != '' > ${col.oper_class}</#if><#if col.order?? && col.is_sort_nulls_applicable!false ><#if col.order!false > ASC<#else > DESC</#if > NULLS</#if > <#if col.nulls_order!false && col.is_sort_nulls_applicable!false ><#if col.nulls_order!false >FIRST <#else >LAST </#if ></#if >WITH ${col.operator}</#list>)
<#if data.include?size gt 0 >
INCLUDE(<#list data.include as col ><#if col?counter != 1 >, </#if >${col}</#list>)
</#if ><#if data.fillfactor?? >
WITH (FILLFACTOR=${data.fillfactor})</#if ><#if data.spcname?? && data.spcname != "pg_default" >
USING INDEX TABLESPACE ${data.spcname }</#if ><#if data.indconstraint?? >
WHERE (${data.indconstraint})</#if><#if data.condeferrable!false >
DEFERRABLE<#if data.condeferred!false >
INITIALLY DEFERRED</#if>
</#if>
</#list>
</#macro>

<#macro CONSTRAINT_COMMENTS schema, table, data >
<#list data as d>
<#if d.name?? && d.comment?? >
COMMENT ON CONSTRAINT ${d.name } ON ${schema}.${table}
IS '${d.comment}';
</#if>
</#list>
</#macro>
