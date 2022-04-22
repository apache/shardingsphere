SELECT * FROM (
<#list 1..(colcnt?number)!1 as i>
    <#if i != 1>
        UNION SELECT  pg_catalog.pg_get_indexdef(${ cid?c }, ${ i?c }, true) AS column, ${ i } AS dummy 
    <#else>
        SELECT  pg_catalog.pg_get_indexdef(${ cid?c }, ${ i?c }, true) AS column, ${ i } AS dummy
    </#if>
</#list>
) tmp
ORDER BY dummy