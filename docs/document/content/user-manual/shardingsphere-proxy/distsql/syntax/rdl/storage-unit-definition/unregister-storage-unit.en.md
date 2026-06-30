+++
title = "UNREGISTER STORAGE UNIT"
weight = 3
+++

### Description

The `UNREGISTER STORAGE UNIT` syntax is used to unregister storage units from the current database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
UnregisterStorageUnit ::=
  'UNREGISTER' 'STORAGE' 'UNIT' ifExists? storageUnitName (',' storageUnitName)* ignoreTables?

ifExists ::=
  'IF' 'EXISTS'

storageUnitName ::=
  identifier

ignoreTables ::=
  'IGNORE' 'SINGLE' 'TABLES' | 'IGNORE' 'BROADCAST' 'TABLES' |
  'IGNORE' ('SINGLE' ',' 'BROADCAST' | 'BROADCAST' ',' 'SINGLE') 'TABLES'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `UNREGISTER STORAGE UNIT` only unregisters storage units in Proxy. The real data sources corresponding to the storage units are not unregistered;
- Storage units already used by rules can not be unregistered. `Storage unit '%s' still used by '%s'.` will be prompted when unregistering storage units used by rules;
- When the storage units to be unregistered are only used by single table rules or broadcast rules, and the user confirms that this restriction can be ignored,
  `IGNORE SINGLE TABLES`, `IGNORE BROADCAST TABLES`, `IGNORE SINGLE, BROADCAST TABLES`, or `IGNORE BROADCAST, SINGLE TABLES` can be added to unregister the storage units;
- `ifExists` clause is used to avoid `Storage unit not exists` error.

### Example

- Unregister a storage unit

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- Unregister multiple storage units

```sql
UNREGISTER STORAGE UNIT ds_0, ds_1;
```

- Unregister storage unit and ignore single tables

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE TABLES;
```

- Unregister storage unit and ignore broadcast tables

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE BROADCAST TABLES;
```

- Unregister storage unit, ignore single and broadcast tables

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE SINGLE, BROADCAST TABLES;
```

- Unregister storage unit, ignore broadcast and single tables

```sql
UNREGISTER STORAGE UNIT ds_0 IGNORE BROADCAST, SINGLE TABLES;
```

- Unregister storage unit with `ifExists` clause

```sql
UNREGISTER STORAGE UNIT IF EXISTS ds_0;
```

### Reserved word

`UNREGISTER`, `STORAGE`, `UNIT`, `IF`, `EXISTS`, `IGNORE`, `SINGLE`, `BROADCAST`, `TABLES`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
