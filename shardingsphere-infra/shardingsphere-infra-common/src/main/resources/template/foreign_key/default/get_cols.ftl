<#list (0..colcnt) as n >
<#if n?counter != 1 >
UNION SELECT  pg_catalog.pg_get_indexdef(${ cid?c }, ${ n?counter?c }, true) AS column
<#else>
SELECT  pg_catalog.pg_get_indexdef(${ cid?c } , ${ n?counter?c } , true) AS column
</#if>
</#list>
