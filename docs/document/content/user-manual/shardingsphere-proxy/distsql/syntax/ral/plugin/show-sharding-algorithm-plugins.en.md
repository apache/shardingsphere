+++
title = "SHOW SHARDING ALGORITHM PLUGINS"
weight = 2
+++

### Description

The `SHOW SHARDING ALGORITHM PLUGINS` syntax is used to query all the plugins of the interface `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showShardingAlgorithmPlugins ::=
  'SHOW' 'SHARDING' 'ALGORITHM' 'PLUGINS'
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

- Query all the plugins for `org.apache.shardingsphere.sharding.spi.ShardingAlgorithm` interface

```sql
SHOW SHARDING ALGORITHM PLUGINS
```

```sql
SHOW SHARDING ALGORITHM PLUGINS;
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
10 rows in set (0.27 sec)
```

### Reserved word

`SHOW`, `SHARDING`, `ALGORITHM`, `PLUGINS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
