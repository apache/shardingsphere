+++
title = "SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### Description

The `SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showEncryptAlgorithmImplementations ::=
  'SHOW' 'ENCRYPT' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="Railroad diagram" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### Return Value Description

| Columns      | Description  |
|--------------|--------------|
| type         | type         |
| type_aliases | type aliases |
| description  | description  |

### Example

- Query all the implementations for `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` interface

```sql
SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS;
+------+--------------+-------------+
| type | type_aliases | description |
+------+--------------+-------------+
| AES  |              |             |
| MD5  |              |             |
+------+--------------+-------------+
2 rows in set (0.06 sec)
```

### Reserved word

`SHOW`, `ENCRYPT`, `ALGORITHM`, `IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
