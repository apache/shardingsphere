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

<#list (0..colcnt) as n >
<#if n?counter != 1 >
UNION
</#if>
SELECT
i.indoption[${n?index}] AS options,
pg_catalog.pg_get_indexdef(i.indexrelid, ${n?counter}, true) AS coldef,
op.oprname,
CASE WHEN (o.opcdefault = FALSE) THEN o.opcname ELSE null END AS opcname
,
coll.collname,
nspc.nspname as collnspname,
pg_catalog.format_type(ty.oid,NULL) AS datatype,
CASE WHEN pg_catalog.pg_get_indexdef(i.indexrelid, ${n?counter}, true) = a.attname THEN FALSE ELSE TRUE END AS is_exp
FROM pg_catalog.pg_index i
JOIN pg_catalog.pg_attribute a ON (a.attrelid = i.indexrelid AND attnum = ${n?counter})
JOIN pg_catalog.pg_type ty ON ty.oid=a.atttypid
LEFT OUTER JOIN pg_catalog.pg_opclass o ON (o.oid = i.indclass[${n?index}])
LEFT OUTER JOIN pg_catalog.pg_constraint c ON (c.conindid = i.indexrelid) LEFT OUTER JOIN pg_catalog.pg_operator op ON (op.oid = c.conexclop[${n?counter}])
LEFT OUTER JOIN pg_catalog.pg_collation coll ON a.attcollation=coll.oid
LEFT OUTER JOIN pg_catalog.pg_namespace nspc ON coll.collnamespace=nspc.oid
WHERE i.indexrelid = ${cid}::oid
</#list>
