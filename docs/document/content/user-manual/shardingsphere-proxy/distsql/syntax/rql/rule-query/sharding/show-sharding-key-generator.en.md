+++
title = "SHOW SHARDING KEY GENERATOR"
weight = 5
+++

### Description

`SHOW SHARDING KEY GENERATOR` syntax is used to query sharding key generators in specified database.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
ShowShardingKeyGenerators::=
  'SHOW' 'SHARDING' 'KEY' ('GENERATOR' keyGeneratorName | 'GENERATORS') ('FROM' databaseName)?

keyGeneratorName ::=
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

- When databaseName is not specified, the default is the currently used DATABASE. If DATABASE is not used, No database selected will be prompted.
- Use `SHOW SHARDING KEY GENERATORS` to query all sharding key generators.
- Use `SHOW SHARDING KEY GENERATOR keyGeneratorName` to query the specified sharding key generator.

### Return value description

| column | Description                       |
|--------|-----------------------------------|
| name   | Sharding key generator name       |
| type   | Sharding key generator type       |
| props  | Sharding key generator properties |

### Example

- Query the sharding key generators of the specified logical database

```sql
SHOW SHARDING KEY GENERATORS FROM sharding_db;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS FROM sharding_db;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

- Query the sharding key generators of the current logical database

```sql
SHOW SHARDING KEY GENERATORS;
```

```sql
mysql> SHOW SHARDING KEY GENERATORS;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

- Query the specified sharding key generator

```sql
SHOW SHARDING KEY GENERATOR snowflake_key_generator;
```

```sql
mysql> SHOW SHARDING KEY GENERATOR snowflake_key_generator;
+-------------------------+-----------+-------+
| name                    | type      | props |
+-------------------------+-----------+-------+
| snowflake_key_generator | snowflake |       |
+-------------------------+-----------+-------+
1 row in set (0.00 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `KEY`, `GENERATOR`, `GENERATORS`, `FROM`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
- [CREATE SHARDING KEY GENERATOR](/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/sharding/create-sharding-key-generator/)
