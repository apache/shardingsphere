+++
title = "UNREGISTER MIGRATION SOURCE STORAGE UNIT"
weight = 4
+++

### Description

The `UNREGISTER MIGRATION SOURCE STORAGE UNIT` syntax is used to unregister migration source storage unit from the current connection

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
UnregisterMigrationSourceStorageUnit ::=
  'UNREGISTER' 'MIGRATION' 'SOURCE' 'STORAGE' 'UNIT' storageUnitName  (',' storageUnitName)* 

storageUnitName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- `UNREGISTER MIGRATION SOURCE STORAGE UNIT` will only unregister storage unit in Proxy, the real data source corresponding to the storage unit will not be
  dropped;

### Example

- Drop a migration source storage unit

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_0;
```

- Drop multiple migration source storage units

```sql
UNREGISTER MIGRATION SOURCE STORAGE UNIT ds_1, ds_2;
```

### Reserved word

`UNREGISTER`、`MIGRATION`、`SOURCE`、`STORAGE`、`UNIT`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
