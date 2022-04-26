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
