<#macro PRIMARY_KEY data>
   <#if data.columns?size gt 0 >

    <#if data.name?? >CONSTRAINT ${data.name} </#if>PRIMARY KEY (<#list data.columns as c>
    <#if c?counter != 1 >, </#if>${c.column}</#list>)<#if data.include?size gt 0 >

    INCLUDE(<#list data.include as col ><#if col?counter != 1 >, </#if>${col}</#list>)</#if>
    <#if data.fillfactor?? >

    WITH (FILLFACTOR=${data.fillfactor})</#if>
    <#if data.spcname?? && data.spcname != "pg_default" >

    USING INDEX TABLESPACE ${data.spcname }</#if>
    <#if data.condeferrable!false >

    DEFERRABLE<#if data.condeferred!false > INITIALLY DEFERRED</#if></#if>
   </#if>
</#macro>