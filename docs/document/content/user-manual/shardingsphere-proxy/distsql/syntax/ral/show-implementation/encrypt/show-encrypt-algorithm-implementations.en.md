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

| Columns     | Description                           |
|-------------|---------------------------------------|
| name        | class name of the implementation      |
| type        | type of the implementation            |
| class_path  | full class name of the implementation |

### Example

- Query all the implementations for `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` interface

```sql
SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS
```

```sql
SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS;
+-----------------------------+------+----------------------------------------------------------------------------------+
| name                        | type | class_path                                                                       |
+-----------------------------+------+----------------------------------------------------------------------------------+
| AESEncryptAlgorithm         | AES  | org.apache.shardingsphere.encrypt.algorithm.standard.AESEncryptAlgorithm         |
| MD5AssistedEncryptAlgorithm | MD5  | org.apache.shardingsphere.encrypt.algorithm.assisted.MD5AssistedEncryptAlgorithm |
+-----------------------------+------+----------------------------------------------------------------------------------+
2 rows in set (0.06 sec)
```

### Reserved word

`SHOW`, `ENCRYPT`, `ALGORITHM`, `IMPLEMENTATIONS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
