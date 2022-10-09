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

SELECT a.attname as colname
FROM (
SELECT
i.indnkeyatts,
i.indrelid,
pg_catalog.unnest(indkey) AS table_colnum,
pg_catalog.unnest(ARRAY(SELECT pg_catalog.generate_series(1, i.indnatts) AS n)) attnum
FROM
pg_catalog.pg_index i
WHERE i.indexrelid = ${cid?c}::OID
) i JOIN pg_catalog.pg_attribute a
ON (a.attrelid = i.indrelid AND i.table_colnum = a.attnum)
WHERE i.attnum > i.indnkeyatts
ORDER BY i.attnum
