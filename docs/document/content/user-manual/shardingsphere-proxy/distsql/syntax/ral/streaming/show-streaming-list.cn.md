+++
title = "SHOW STREAMING LIST"
weight = 3

+++

### 描述

`SHOW STREAMING LIST` 语法用于查询 CDC Streaming 作业列表。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowStreamingList ::=
  'SHOW' 'STREAMING' 'LIST'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列              | 说明                  |
|----------------|---------------------|
| id             | CDC Streaming 作业 ID |
| database       | 数据库名称               |
| tables         | CDC Streaming 表      |
| job_item_count | CDC Streaming 作业项数量 |
| active         | 作业是否活跃              |
| create_time    | 作业创建时间              |
| stop_time      | 作业停止时间              |

### 示例

- 查询 CDC Streaming 作业列表

```sql
SHOW STREAMING LIST;
```

```sql
sharding_db=> SHOW STREAMING LIST;
                     id                     |  database   | tables  | job_item_count | active |     create_time     | stop_time
--------------------------------------------+-------------+---------+----------------+--------+---------------------+-----------
 j0302p0000702a83116fcee83f70419ca5e2993791 | sharding_db | t_order | 1              | true   | 2023-10-27 22:01:27 |
(1 row)
```

### 保留字

`SHOW`、`STREAMING`、`LIST`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
