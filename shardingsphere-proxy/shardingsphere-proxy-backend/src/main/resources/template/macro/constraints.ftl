<#macro PRIMARY_KEY>
   <#if columns?size gt 0 >

    <#if name?? >CONSTRAINT ${name} </#if>PRIMARY KEY (<#list columns as c>
    <#if c?counter != 1 >, </#if>${c.column}</#list>)<#if include?size gt 0 >

    INCLUDE(<#list include as col ><#if col?counter != 1 >, </#if>${col}</#list>)</#if>
    <#if fillfactor?? >

    WITH (FILLFACTOR=${fillfactor})</#if>
    <#if spcname?? && spcname != "pg_default" >

    USING INDEX TABLESPACE ${spcname }</#if>
    <#if condeferrable!false >

    DEFERRABLE<#if condeferred!false > INITIALLY DEFERRED</#if></#if>
   </#if>
</#macro>