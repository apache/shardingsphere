+++
title = "资源查询"
weight = 1
+++

## 语法说明

```sql
SHOW SCHEMA RESOURCES [FROM schemaName]
```

## 返回值说明

| 列        | 说明      |
| --------- | -------- |
| name      | 数据源名称 |
| type      | 数据源类型 |
| host      | 数据源地址 |
| port      | 数据源端口 |
| db        | 数据库名称 |
| attribute | 数据源参数 |

## 示例

```sql
mysql> show schema resources;
+------+-------+-----------+------+------+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
| name | type  | host      | port | db   | attribute                                                                                                                                                   |
+------+-------+-----------+------+------+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
| ds_0 | MySQL | 127.0.0.1 | 3306 | ds_0 | {"minPoolSize":1,"connectionTimeoutMilliseconds":30000,"maxLifetimeMilliseconds":1800000,"readOnly":false,"idleTimeoutMilliseconds":60000,"maxPoolSize":50} |
| ds_1 | MySQL | 127.0.0.1 | 3306 | ds_1 | {"minPoolSize":1,"connectionTimeoutMilliseconds":30000,"maxLifetimeMilliseconds":1800000,"readOnly":false,"idleTimeoutMilliseconds":60000,"maxPoolSize":50} |
+------+-------+-----------+------+------+-------------------------------------------------------------------------------------------------------------------------------------------------------------+
2 rows in set (0.84 sec)
```
