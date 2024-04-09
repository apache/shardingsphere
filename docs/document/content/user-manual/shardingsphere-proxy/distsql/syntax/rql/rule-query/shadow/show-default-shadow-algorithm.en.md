+++
title = "SHOW DEFAULT SHADOW ALGORITHM"
weight = 4
+++

### Description

The `SHOW DEFAULT SHADOW ALGORITHM` syntax is used to query default shadow algorithm for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowEncryptAlgorithm::=
  'SHOW' 'SHADOW' 'ALGORITHM' ('FROM' databaseName)?

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

### Return value description

| Column                | Description                 |
|-----------------------|-----------------------------|
| shadow_algorithm_name | Shadow algorithm name       |
| type                  | Shadow algorithm type       |
| props                 | Shadow algorithm properties |

### Example

- Query default shadow algorithm for specified database.

```sql
SHOW DEFAULT SHADOW ALGORITHM FROM shadow_db;
```

```sql
mysql> SHOW DEFAULT SHADOW ALGORITHM FROM shadow_db;
+-------------------------+-------------+-----------------------------------------+
| shadow_algorithm_name   | type        | props                                   |
+-------------------------+-------------+-----------------------------------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 |
+-------------------------+-------------+-----------------------------------------+
1 row in set (0.00 sec)
```

- Query default shadow algorithm for current database.

```sql
SHOW SHADOW ALGORITHM;
```

```sql
mysql> SHOW SHADOW ALGORITHM;
+-------------------------+-------------+-----------------------------------------+
| shadow_algorithm_name   | type        | props                                   |
+-------------------------+-------------+-----------------------------------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 |
+-------------------------+-------------+-----------------------------------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `DEFAULT`,`SHADOW`, `ALGORITHM`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
