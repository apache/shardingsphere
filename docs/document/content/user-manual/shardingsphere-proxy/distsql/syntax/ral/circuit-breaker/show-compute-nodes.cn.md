+++
title = "SHOW COMPUTE NODES"
weight = 3
+++

### 描述

`SHOW COMPUTE NODES` 语法用于查询 proxy 实例信息。

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

| 列           | 说明         |
|-------------|------------|
| instance_id | proxy 实例编号 |
| host        | 主机地址       |
| port        | 端口号        |
| status      | proxy 实例状态 |
| mode_type   | proxy 实例模式 |
| worker_id   | worker id  |
| labels      | 标签         |
| version     | 版本         |

### 示例

- 查询 proxy 实例信息

```sql
SHOW COMPUTE NODES;
```

```sql
mysql> SHOW COMPUTE NODES;
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+---------+
| instance_id                          | host          | port | status | mode_type | worker_id | labels | version |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+---------+
| 734bb036-b15d-4af0-be87-2372d8b6a0cd | 192.168.5.163 | 3307 | OK     | Cluster   | -1        |        | 5.3.0   |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+---------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`COMPUTE`、`NODES`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)