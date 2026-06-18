+++
title = "SET DEFAULT SINGLE TABLE STORAGE UNIT"
weight = 3
+++

## Description

The `SET DEFAULT SINGLE TABLE STORAGE UNIT` syntax is used to set default single table storage unit.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
SetDefaultSingleTableStorageUnit ::=
  'SET' 'DEFAULT' 'SINGLE' 'TABLE' 'STORAGE' 'UNIT' singleTableDefinition

singleTableDefinition ::=
  '=' (storageUnitName | 'RANDOM')

storageUnitName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `STORAGE UNIT` needs to use storage unit managed by RDL. The `RANDOM` keyword stands for random storage.


### Example

- Set a default single table storage unit

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = ds_0;
```

- Set the default single table storage unit to random storage

```sql
SET DEFAULT SINGLE TABLE STORAGE UNIT = RANDOM;
```

### Reserved word

`SET`, `DEFAULT`, `SINGLE`, `TABLE`, `STORAGE`, `UNIT`, `RANDOM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
