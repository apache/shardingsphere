+++
title = "SHOW COMPUTE NODES"
weight = 3
+++

### 描述

`SHOW COMPUTE NODES` 语法用于查询计算节点信息。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
ShowComputeNodes ::=
  'SHOW' 'COMPUTE' 'NODES'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 返回值说明

| 列             | 说明      |
|---------------|-----------|
| instance_id   | 实例 id    |
| instance_type | 实例类型    |
| host          | 主机地址    |
| port          | 端口号     |
| status        | 实例状态    |
| mode_type     | 模式类型    |
| worker_id     | worker id |
| labels        | 标签       |
| version       | 版本       |

### 示例

```sql
mysql> SHOW COMPUTE NODES;
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
| instance_id                          | instance_type | host       | port | status | mode_type  | worker_id | labels | version  |
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
| 3e84d33e-cb97-42f2-b6ce-f78fea0ded89 | PROXY         | 127.0.0.1  | 3307 | OK     | Cluster    | -1        |        | 5.4.2    |
+--------------------------------------+---------------+------------+------+--------+------------+-----------+--------+----------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`COMPUTE`、`NODES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)