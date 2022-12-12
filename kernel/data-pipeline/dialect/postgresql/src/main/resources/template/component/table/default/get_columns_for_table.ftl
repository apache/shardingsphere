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

SELECT
    a.attname AS name, pg_catalog.format_type(a.atttypid, NULL) AS cltype,
    pg_catalog.pg_get_expr(def.adbin, def.adrelid) AS defval,
    pg_catalog.quote_ident(n.nspname)||'.'||pg_catalog.quote_ident(c.relname) as inheritedfrom,
    c.oid as inheritedid
FROM
    pg_catalog.pg_class c
JOIN
    pg_catalog.pg_namespace n ON c.relnamespace=n.oid
JOIN
    pg_catalog.pg_attribute a ON a.attrelid = c.oid AND NOT a.attisdropped AND a.attnum > 0
LEFT OUTER JOIN
    pg_catalog.pg_attrdef def ON adrelid=a.attrelid AND adnum=a.attnum
WHERE
<#if tid?? >
    c.oid = ${tid?c}::OID
<#else >
    c.relname = '${tname}'
</#if>
