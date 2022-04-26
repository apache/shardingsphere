SELECT c.oid, conname as name, relname, nspname, description as comment,
pg_catalog.pg_get_expr(conbin, conrelid, true) as consrc,
connoinherit, NOT convalidated as convalidated, conislocal
FROM pg_catalog.pg_constraint c
JOIN pg_catalog.pg_class cl ON cl.oid=conrelid
JOIN pg_catalog.pg_namespace nl ON nl.oid=relnamespace
LEFT OUTER JOIN
pg_catalog.pg_description des ON (des.objoid=c.oid AND
des.classoid='pg_constraint'::regclass)
WHERE contype = 'c'
AND conrelid = ${ tid?c }::oid
