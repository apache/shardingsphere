+++
title = "UNREGISTER STORAGE UNIT"
weight = 3
+++

### Description

The `UNREGISTER STORAGE UNIT` syntax is used to unregister storage unit from the current database

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
UnregisterStorageUnit ::=
  'UNREGISTER' 'STORAGE' 'UNIT' ifExists? storageUnitName (',' storageUnitName)* (ignoreSingleTables | ignoreBroadcastTables | ignoreSingleAndBroadcastTables)?

ignoreSingleTables ::=
    'IGNORE' 'SINGLE' 'TABLES'

ignoreBroadcastTables ::=
    'IGNORE' 'BROADCAST' 'TABLES'

ignoreSingleAndBroadcastTables ::=
    'IGNORE' ('SINGLE' ',' 'BROADCAST' | 'BROADCAST' ',' 'SINGLE') 'TABLES'

ifExists ::=
  'IF' 'EXISTS'

storageUnitName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `UNREGISTER STORAGE UNIT` will only unregister storage unit in Proxy, the real data source corresponding to the storage unit will not be
  unregistered;
- Unable to unregister storage unit already used by rules. `Storage unit are still in used.` will be prompted when removing
  storage units used by rules;
- The storage unit need to be removed only contains `SINGLE RULE`, `BROADCAST RULE` and when the user confirms that this restriction
  can be ignored, the `IGNORE SINGLE TABLES`, `IGNORE BROADCAST TABLES`, `IGNORE SINGLE, BROADCAST TABLES` keyword can be added to remove the storage unit;
- `ifExists` clause is used for avoid `Storage unit not exists` error.

### Example

- Drop a storage unit

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- Drop multiple storage units

```sql
UNREGISTER STORAGE UNIT ds_0, ds_1;
```

- Ignore single rule remove storage unit

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE TABLES;
```

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE BROADCAST TABLES;
```

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE, BROADCAST TABLES;
```

- Drop the storage unit with `ifExists` clause

```sql
UNREGISTER STORAGE UNIT IF EXISTS ds_0;
```

### Reserved word

`DROP`, `STORAGE`, `UNIT`, `IF`, `EXISTS`, `IGNORE`, `SINGLE`, `BROADCAST`, `TABLES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
