<#list keys as keypair >
<#if keypair?counter != 1 >
    UNION  
</#if>
SELECT a1.attname as conattname,
a2.attname as confattname
FROM pg_catalog.pg_attribute a1,
pg_catalog.pg_attribute a2
WHERE a1.attrelid=${tid}::oid
AND a1.attnum=${keypair.conkey}
AND a2.attrelid=${confrelid}::oid
AND a2.attnum=${keypair.confkey}
</#list>
