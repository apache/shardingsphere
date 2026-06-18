+++
title = "Single Table"
weight = 12
+++

## Background

Single rule is used to specify which single tables need to be managed by ShardingSphere, or to set the default single table data source.

## Parameters

```yaml
rules:
- !SINGLE
  tables:
    # MySQL style
    - ds_0.t_single # Load specified single table
    - ds_1.* # Load all single tables in the specified data source
    - "*.*" # Load all single tables
    # PostgreSQL style
    - ds_0.public.t_config
    - ds_1.public.*
    - ds_2.*.*
    - "*.*.*"
  defaultDataSource: ds_0 # The default data source is used when executing CREATE TABLE statement to create a single table. The default value is null, indicating random unicast routing.
```

## Related References

- [Single Table](/en/features/sharding/concept/#single-table)
