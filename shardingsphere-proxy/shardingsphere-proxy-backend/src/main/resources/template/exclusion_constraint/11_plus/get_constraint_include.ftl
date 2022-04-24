SELECT a.attname as colname
FROM (
SELECT
i.indnkeyatts,
i.indrelid,
pg_catalog.unnest(indkey) AS table_colnum,
pg_catalog.unnest(ARRAY(SELECT pg_catalog.generate_series(1, i.indnatts) AS n)) attnum
FROM
pg_catalog.pg_index i
WHERE i.indexrelid = ${cid}::OID
) i JOIN pg_catalog.pg_attribute a
ON (a.attrelid = i.indrelid AND i.table_colnum = a.attnum)
WHERE i.attnum > i.indnkeyatts
ORDER BY i.attnum
