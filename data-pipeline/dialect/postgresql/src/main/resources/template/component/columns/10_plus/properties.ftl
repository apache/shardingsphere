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

SELECT att.attname as name, att.atttypid, att.attlen, att.attnum, att.attndims,
att.atttypmod, att.attacl, att.attnotnull, att.attoptions, att.attstattarget,
att.attstorage, att.attidentity,
pg_catalog.pg_get_expr(def.adbin, def.adrelid) AS defval,
pg_catalog.format_type(ty.oid,NULL) AS typname,
pg_catalog.format_type(ty.oid,att.atttypmod) AS displaytypname,
pg_catalog.format_type(ty.oid,att.atttypmod) AS cltype,
CASE WHEN ty.typelem > 0 THEN ty.typelem ELSE ty.oid END as elemoid,
(SELECT nspname FROM pg_catalog.pg_namespace WHERE oid = ty.typnamespace) as typnspname,
ty.typstorage AS defaultstorage,
description, pi.indkey,
(SELECT count(1) FROM pg_catalog.pg_type t2 WHERE t2.typname=ty.typname) > 1 AS isdup,
CASE WHEN length(coll.collname::text) > 0 AND length(nspc.nspname::text) > 0  THEN
pg_catalog.concat(pg_catalog.quote_ident(nspc.nspname),'.',pg_catalog.quote_ident(coll.collname))
ELSE '' END AS collspcname,
EXISTS(SELECT 1 FROM pg_catalog.pg_constraint WHERE conrelid=att.attrelid AND contype='f' AND att.attnum=ANY(conkey)) As is_fk,
(SELECT pg_catalog.array_agg(provider || '=' || label) FROM pg_catalog.pg_seclabels sl1 WHERE sl1.objoid=att.attrelid AND sl1.objsubid=att.attnum) AS seclabels,
(CASE WHEN (att.attnum < 1) THEN true ElSE false END) AS is_sys_column,
(CASE WHEN (att.attidentity in ('a', 'd')) THEN 'i' ELSE 'n' END) AS colconstype, tab.relname as relname,
(CASE WHEN tab.relkind = 'v' THEN true ELSE false END) AS is_view_only,
seq.*
FROM pg_catalog.pg_attribute att
JOIN pg_catalog.pg_type ty ON ty.oid=atttypid
LEFT OUTER JOIN pg_catalog.pg_attrdef def ON adrelid=att.attrelid AND adnum=att.attnum
LEFT OUTER JOIN pg_catalog.pg_description des ON (des.objoid=att.attrelid AND des.objsubid=att.attnum AND des.classoid='pg_class'::regclass)
LEFT OUTER JOIN (pg_catalog.pg_depend dep JOIN pg_catalog.pg_class cs ON dep.classid='pg_class'::regclass AND dep.objid=cs.oid AND cs.relkind='S') ON dep.refobjid=att.attrelid AND dep.refobjsubid=att.attnum
LEFT OUTER JOIN pg_catalog.pg_index pi ON pi.indrelid=att.attrelid AND indisprimary
LEFT OUTER JOIN pg_catalog.pg_collation coll ON att.attcollation=coll.oid
LEFT OUTER JOIN pg_catalog.pg_namespace nspc ON coll.collnamespace=nspc.oid
LEFT OUTER JOIN pg_catalog.pg_sequence seq ON cs.oid=seq.seqrelid
LEFT OUTER JOIN pg_catalog.pg_class tab on tab.oid = att.attrelid
WHERE att.attrelid = ${tid?c}::oid
<#if clid?? >
AND att.attnum = ${clid?c}::int
</#if>
AND att.attnum > 0
AND att.attisdropped IS FALSE
ORDER BY att.attnum;
