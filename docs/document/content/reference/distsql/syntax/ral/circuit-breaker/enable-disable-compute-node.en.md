+++
title = "ENABLE/DISABLE COMPUTE NODE"
weight = 4
+++

## Description

The `ENABLE/DISABLE COMPUTE NODE` syntax is used enable/disable a specified proxy instance

### Syntax

```sql
EnableDisableComputeNode ::=
  ( 'ENABLE' | 'DISABLE' ) 'COMPUTE' 'NODE' instanceId

instanceId ::=
  string
```

### Supplement

- `instanceId` needs to be obtained through `SHOW COMPUTE NODES` syntax query

- The currently in-use proxy instance cannot be disabled

### Example

- Disable a specified proxy instance
```sql
DISABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

- Enable a specified proxy instance

```sql
ENABLE COMPUTE NODE '734bb086-b15d-4af0-be87-2372d8b6a0cd';
```

### Reserved word

`ENABLE`, `DISABLE`, `COMPUTE`, `NODE`

### Related links

- [Reserved word](/en/reference/distsql/syntax/reserved-word/)
