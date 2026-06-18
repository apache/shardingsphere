+++
title = "SHOW MIGRATION RULE"
weight = 1

+++

### 描述

`SHOW MIGRATION RULE` 语法用于查询数据迁移规则。
### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowMigrationRule ::=
  'SHOW' 'MIGRATION' 'RULE'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列              | 说明     |
|----------------|--------|
| read           | 数据读取配置 |
| write          | 数据写入配置 |
| stream_channel | 数据通道   |
### 示例

- 查询数据迁移规则

```sql
SHOW MIGRATION RULE;
```

```sql
mysql> SHOW MIGRATION RULE;
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| read                                                         | write                                | stream_channel                                        |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
| {"workerThread":20,"batchSize":1000,"shardingSize":10000000} | {"workerThread":20,"batchSize":1000} | {"type":"MEMORY","props":{"block-queue-size":"2000"}} |
+--------------------------------------------------------------+--------------------------------------+-------------------------------------------------------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`MIGRATION`、`RULE`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)