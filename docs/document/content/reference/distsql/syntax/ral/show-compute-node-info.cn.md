+++
title = "SHOW COMPUTE NODE INFO"
weight = 2
+++

### 描述

`SHOW COMPUTE NODE INFO` 语法用于查询当前 proxy 实例信息

### 语法

```sql
ShowComputeNodeInfo ::=
  'SHOW' 'COMPUTE' 'NODE' 'INFO'
```

### 返回值说明

| 列             | 说明               |
|----------------|-------------------|
| instance_id    | proxy 实例编号     |
| host           | 主机地址           |
| port           | 端口号             |
| status         | proxy 实例状态     |
| mode_type      | proxy 实例模式     |
| worker_id      | worker id         |
| labels         | 标签               |

### 示例

- 查询当前 proxy 实例信息

```sql
SHOW COMPUTE NODE INFO;
```

```sql
mysql> SHOW COMPUTE NODE INFO;
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| instance_id                          | host          | port | status | mode_type | worker_id | labels |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
| 734bb036-b15d-4af0-be87-2372d8b6a0cd | 192.168.5.163 | 3307 | OK     | Cluster   | -1        |        |
+--------------------------------------+---------------+------+--------+-----------+-----------+--------+
1 row in set (0.01 sec)
```

### 保留字

`SHOW`、`COMPUTE`、`NODE`、`INFO`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)