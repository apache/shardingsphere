+++
title = "SQL-federation"
weight = 13
+++

## Background

This function is an **experimental one and is currently not suitable for use in core system production environments**.
When multiple tables in a join query are distributed across different database instances, enabling federated query allows for cross-database join queries, as well as subqueries.

## Parameters

Class: org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration

Attributes: 

| *name*                  | *DataType*  | *Description*              | *Default Value* |
|---------------------|---------|-------------------|-------|
| sqlFederationEnabled          | boolean | SQL federation enabled configuration          | -     |
| allQueryUseSQLFederation | boolean | all query use SQL federation configuration | -     |
| executionPlanCache | org.apache.shardingsphere.sql.parser.api.CacheOption | execution plan cache configuration            | -     |

## Cache option Configuration

Class: org.apache.shardingsphere.sql.parser.api.CacheOption

Attributes: 

| *name*            | *DataType* | *Description*         | *Default Value*                                        |
|-----------------|--------|-----------------------|--------------------------------------------------------|
| initialCapacity | int    | Initial capacity of local cache | execution plan local cache default value of 2000       |
| maximumSize     | long   | Maximum capacity of local cache | execution plan local cache maximum default value 65535 |

## Sample

```java
private SQLFederationRuleConfiguration createSQLFederationRuleConfiguration() {
    CacheOption executionPlanCache = new CacheOption(2000, 65535L);
    return new SQLFederationRuleConfiguration(true, false, executionPlanCache);
}
```

## Related References

- [YAML Configuration：SQL Federation](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/sql-federation/)
