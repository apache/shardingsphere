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

SELECT ct.oid,
conname as name,
condeferrable,
condeferred,
confupdtype,
confdeltype,
CASE confmatchtype
WHEN 's' THEN FALSE
WHEN 'f' THEN TRUE
END AS confmatchtype,
conkey,
confkey,
confrelid,
nl.nspname as fknsp,
cl.relname as fktab,
nr.nspname as refnsp,
cr.relname as reftab,
description as comment,
convalidated,
conislocal
FROM pg_catalog.pg_constraint ct
JOIN pg_catalog.pg_class cl ON cl.oid=conrelid
JOIN pg_catalog.pg_namespace nl ON nl.oid=cl.relnamespace
JOIN pg_catalog.pg_class cr ON cr.oid=confrelid
JOIN pg_catalog.pg_namespace nr ON nr.oid=cr.relnamespace
LEFT OUTER JOIN pg_catalog.pg_description des ON (des.objoid=ct.oid AND des.classoid='pg_constraint'::regclass)
WHERE contype='f' AND
conrelid = ${tid?c}::oid
ORDER BY conname
