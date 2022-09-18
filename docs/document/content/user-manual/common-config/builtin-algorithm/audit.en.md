+++
title = "Sharding Audit Algorithm"
weight = 8
+++

## Background

The sharding audit is to audit the SQL statements in the sharding database. Sharding audit not only intercept illegal SQL statements, but also gather the SQL statistics.

## Parameters

### DML_SHARDING_CONDITIONS algorithm

Type: DML_SHARDING_CONDITIONS

## Procedure

1. when configuring data sharding rules, create sharding audit configurations.

## Sample

- DML_SHARDING_CONDITIONS

```yaml
auditors:
  sharding_key_required_auditor:
    type: DML_SHARDING_CONDITIONS
```
