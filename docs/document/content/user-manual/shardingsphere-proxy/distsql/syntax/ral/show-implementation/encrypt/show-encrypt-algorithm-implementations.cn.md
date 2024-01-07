+++
title = "SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS"
weight = 1
+++

### 描述

`SHOW ENCRYPT ALGORITHM IMPLEMENTATIONS` 语法用于查询 `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` 接口所有具体的实现类。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showEncryptAlgorithmImplementations ::=
  'SHOW' 'ENCRYPT' 'ALGORITHM' 'IMPLEMENTATIONS'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列    | 说明      |
|------|---------|
| name | 实现类名称   |
| type | 类型      |
| class_path | 实现类完整路径 |

### 示例

- 查询 `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` 接口的所有实现类

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

### 保留字

`SHOW`、`ENCRYPT`、`ALGORITHM`、`IMPLEMENTATIONS`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)