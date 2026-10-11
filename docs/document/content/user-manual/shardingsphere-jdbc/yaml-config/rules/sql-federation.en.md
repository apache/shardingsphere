+++
title = "SQL Federation"
weight = 13
+++

## Background

This function is an **experimental one and is currently not suitable for use in core system production environments**.
When multiple tables in a join query are distributed across different database instances, enabling federated query allows for cross-database join queries, as well as subqueries.

## Parameters

```yaml
sqlFederation:
  providerType: # SQL federation provider type; NONE disables federation and is the default
  allQueryUseSQLFederation: # all query use SQL federation configuration
  executionPlanCache: # execution plan cache configuration
    initialCapacity: 2000 # execution plan local cache initial capacity
    maximumSize: 65535 # execution plan local cache maximum size
```

When migrating an existing configuration, remove `sqlFederationEnabled`. Set `providerType` to `NONE` to disable federation, including configurations that previously used `sqlFederationEnabled: false` with another provider.

## Sample

```yaml
sqlFederation:
  providerType: CALCITE
  allQueryUseSQLFederation: false
  executionPlanCache:
    initialCapacity: 2000
    maximumSize: 65535
```

## Related References

- [JAVA API: SQL Federation](/en/user-manual/shardingsphere-jdbc/java-api/rules/sql-federation/)
