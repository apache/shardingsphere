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

SELECT att.attname as name, att.*, def.*, pg_catalog.pg_get_expr(def.adbin, def.adrelid) AS defval,
CASE WHEN att.attndims > 0 THEN 1 ELSE 0 END AS isarray,
pg_catalog.format_type(ty.oid,NULL) AS typname,
pg_catalog.format_type(ty.oid,att.atttypmod) AS displaytypname,
CASE WHEN ty.typelem > 0 THEN ty.typelem ELSE ty.oid END as elemoid,
tn.nspname as typnspname, et.typname as elemtypname,
ty.typstorage AS defaultstorage, cl.relname, na.nspname,
pg_catalog.quote_ident(na.nspname) || '.' || pg_catalog.quote_ident(cl.relname) AS parent_tbl,
att.attstattarget, description, cs.relname AS sername,
ns.nspname AS serschema,
(SELECT count(1) FROM pg_catalog.pg_type t2 WHERE t2.typname=ty.typname) > 1 AS isdup,
indkey, NULL as attoptions,
pg_catalog.format_type(ty.oid,att.atttypmod) AS cltype,
EXISTS(SELECT 1 FROM pg_catalog.pg_constraint WHERE conrelid=att.attrelid AND contype='f' AND att.attnum=ANY(conkey)) As is_fk,
NULL AS seclabels,
(CASE WHEN (att.attnum < 1) THEN true ElSE false END) AS is_sys_column
FROM pg_catalog.pg_attribute att
JOIN pg_catalog.pg_type ty ON ty.oid=atttypid
JOIN pg_catalog.pg_namespace tn ON tn.oid=ty.typnamespace
JOIN pg_catalog.pg_class cl ON cl.oid=att.attrelid
JOIN pg_catalog.pg_namespace na ON na.oid=cl.relnamespace
LEFT OUTER JOIN pg_catalog.pg_type et ON et.oid=ty.typelem
LEFT OUTER JOIN pg_catalog.pg_attrdef def ON adrelid=att.attrelid AND adnum=att.attnum
LEFT OUTER JOIN pg_catalog.pg_description des ON (des.objoid=att.attrelid AND des.objsubid=att.attnum AND des.classoid='pg_class'::regclass)
LEFT OUTER JOIN (pg_catalog.pg_depend JOIN pg_catalog.pg_class cs ON classid='pg_class'::regclass AND objid=cs.oid AND cs.relkind='S') ON refobjid=att.attrelid AND refobjsubid=att.attnum
LEFT OUTER JOIN pg_catalog.pg_namespace ns ON ns.oid=cs.relnamespace
LEFT OUTER JOIN pg_catalog.pg_index pi ON pi.indrelid=att.attrelid AND indisprimary
WHERE att.attrelid = ${tid?c}::oid
<#if clid?? >
AND att.attnum = ${clid?c}::int
</#if>
AND att.attnum > 0
AND att.attisdropped IS FALSE
ORDER BY att.attnum;
