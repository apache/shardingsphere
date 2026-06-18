+++
title = "SHOW SHADOW ALGORITHM PLUGINS"
weight = 6
+++

### Description

The `SHOW SHADOW ALGORITHM PLUGINS` syntax is used to query all the implementations of the interface `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm`.

### Syntax

{{< tabs >}}
{{% tab name="Grammar" %}}
```sql
showShadowAlgorithmPlugins ::=
  'SHOW' 'SHADOW' 'ALGORITHM' 'PLUGINS'
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

- Query all the implementations for `org.apache.shardingsphere.shadow.spi.ShadowAlgorithm` interface

```sql
SHOW SHADOW ALGORITHM PLUGINS
```

```sql
SHOW SHADOW ALGORITHM PLUGINS;
+-------------+--------------+-------------+
| type        | type_aliases | description |
+-------------+--------------+-------------+
| SQL_HINT    |              |             |
| REGEX_MATCH |              |             |
| VALUE_MATCH |              |             |
+-------------+--------------+-------------+
3 rows in set (0.37 sec)
```

### Reserved word

`SHOW`, `SHADOW`, `ALGORITHM`, `PLUGINS`

### Related links

- [Reserved word](/en/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
