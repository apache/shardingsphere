+++
title = "SHOW SHADOW ALGORITHMS"
weight = 3
+++

### Description

The `SHOW SHADOW ALGORITHMS` syntax is used to query shadow algorithms for specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowEncryptAlgorithm::=
  'SHOW' 'SHADOW' 'ALGORITHMS' ('FROM' databaseName)?

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
| is_default            | Default                     |

### Example

- Query shadow algorithms for specified database.

```sql
SHOW SHADOW ALGORITHMS FROM shadow_db;
```

```sql
mysql> SHOW SHADOW ALGORITHMS FROM shadow_db;
+-------------------------+-------------+-----------------------------------------+------------+
| shadow_algorithm_name   | type        | props                                   | is_default |
+-------------------------+-------------+-----------------------------------------+------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 | false      |
+-------------------------+-------------+-----------------------------------------+------------+
1 row in set (0.00 sec)
```

- Query shadow algorithms for current database.

```sql
SHOW SHADOW ALGORITHMS;
```

```sql
mysql> SHOW SHADOW ALGORITHMS;
+-------------------------+-------------+-----------------------------------------+------------+
| shadow_algorithm_name   | type        | props                                   | is_default |
+-------------------------+-------------+-----------------------------------------+------------+
| user_id_match_algorithm | VALUE_MATCH | column=user_id,operation=insert,value=1 | false      |
+-------------------------+-------------+-----------------------------------------+------------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHADOW`, `ALGORITHMS`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
