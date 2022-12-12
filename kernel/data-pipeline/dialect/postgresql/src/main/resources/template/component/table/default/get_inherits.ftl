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

<#import "../../../macro/db_catalogs.ftl" as CATALOG>
SELECT c.oid, c.relname , nspname,
CASE WHEN nspname NOT LIKE 'pg\_%' THEN
pg_catalog.quote_ident(nspname)||'.'||pg_catalog.quote_ident(c.relname)
ELSE pg_catalog.quote_ident(c.relname)
END AS inherits
FROM pg_catalog.pg_class c
JOIN pg_catalog.pg_namespace n
ON n.oid=c.relnamespace
WHERE relkind='r'
<@CATALOG.VALID_CATALOGS server_type />
ORDER BY relnamespace, c.relname
