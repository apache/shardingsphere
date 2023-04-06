+++
title = "ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE"
weight = 1
+++

## Description

The `ALTER READWRITE_SPLITTING RULE ENABLE/DISABLE` syntax is used enable/disable a specified read source for specified readwrite-splitting rule.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
AlterReadwriteSplittingRule ::=
  'ALTER' 'READWRITE_SPLITTING' 'RULE' groupName ('ENABLE' | 'DISABLE') storageUnitName 'FROM' databaseName

groupName ::=
  identifier

storageUnitName ::=
  identifier

databaseName ::=
  identifier
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Supplement

- When `databaseName` is not specified, the default is the currently used `DATABASE`. If `DATABASE` is not used, `No database selected` will be prompted.

### Example

- Disable a specified read source for specified readwrite-splitting rule in specified database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_ds_0 FROM sharding_db;
```

- Enable a specified read source for specified readwrite-splitting rule in specified database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 ENABLE read_ds_0 FROM sharding_db;
```

- Disable a specified read source for specified readwrite-splitting rule in current database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_0 DISABLE read_ds_0;
```

- Enable a specified read source for specified readwrite-splitting rule in current database

```sql
ALTER READWRITE_SPLITTING RULE ms_group_1 ENABLE read_ds_0;
```

### Reserved word

`ALTER`, `READWRITE_SPLITTING`, `RULE`, `ENABLE`, `DISABLE`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
