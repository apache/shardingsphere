+++
title = "SHOW PLUGINS"
weight = 1
+++

### 描述

`SHOW PLUGINS OF interfaceClass` 语法用于查询指定接口的全部实现。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
showPluginImplementations ::=
  'SHOW' 'PLUGINS' 'OF' interfaceClass

interfaceClass ::=
  string
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列            | 说明     |
|--------------|--------|
| type         | 类型     |
| type_aliases | 类型别名   |
| description  | 描述     |

### 示例

- 查询 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` 接口的所有实现类

```sql
SHOW PLUGINS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm'
```

```sql
SHOW PLUGINS OF 'org.apache.shardingsphere.sharding.spi.ShardingAlgorithm';
+----------------+--------------+-------------+
| type           | type_aliases | description |
+----------------+--------------+-------------+
| MOD            |              |             |
| HASH_MOD       |              |             |
| VOLUME_RANGE   |              |             |
| BOUNDARY_RANGE |              |             |
| AUTO_INTERVAL  |              |             |
| INTERVAL       |              |             |
| CLASS_BASED    |              |             |
| INLINE         |              |             |
| COMPLEX_INLINE |              |             |
| HINT_INLINE    |              |             |
+----------------+--------------+-------------+
10 rows in set (0.52 sec)
```

### 补充说明

针对一些常用的接口，ShardingSphere 提供了语法糖，可以简化操作，目前已提供的插件查询语法糖如下：

- 查询 `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` 接口实现：[SHOW SHARDING ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-sharding-algorithm-plugins/)
- 查询 `org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm` 接口实现：[SHOW LOAD BALANCE ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-load-balance-algorithm-plugins/)
- 查询 `org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm` 接口实现：[SHOW ENCRYPT ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-encrypt-algorithm-plugins/)
- 查询 `org.apache.shardingsphere.mask.spi.MaskAlgorithm` 接口实现：[SHOW MASK ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-mask-algorithm-plugins/)
- 查询 `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` 接口实现：[SHOW SHADOW ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-shadow-algorithm-plugins/)
- 查询 `org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm` 接口实现：[SHOW KEY GENERATE ALGORITHM PLUGINS](/cn/user-manual/shardingsphere-proxy/distsql/syntax/ral/plugin/show-key-generate-algorithm-plugins/)

### 保留字

`SHOW`、`PLUGINS`、`OF`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
