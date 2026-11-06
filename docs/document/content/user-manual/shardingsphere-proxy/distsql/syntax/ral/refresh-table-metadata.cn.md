+++
title = "REFRESH TABLE METADATA"
weight = 9
+++

### 描述

`REFRESH TABLE METADATA` 语法用于刷新表元数据。

### 语法

{{< tabs >}}
{{% tab name="语法" %}}
```sql
RefreshTableMetadata ::=
  'REFRESH' 'TABLE' 'METADATA' (tableName | tableName 'FROM' 'STORAGE' 'UNIT' storageUnitName ('SCHEMA' schemaName)?)?

tableName ::=
  distSQLIdentifier

storageUnitName ::=
  distSQLIdentifier

schemaName ::=
  distSQLIdentifier

distSQLIdentifier ::=
  identifier | quotedIdentifier

quotedIdentifier ::=
  '`' identifier '`' | '"' identifier '"'
```
{{% /tab %}}
{{% tab name="铁路图" %}}
<iframe frameborder="0" name="diagram" id="diagram" width="100%" height="100%"></iframe>
{{% /tab %}}
{{< /tabs >}}

### 补充说明

- 未指定 `tableName` 和 `storageUnitName` 时，默认刷新所有表的元数据；

- 刷新元数据需要使用 `DATABASE` 如果未使用 `DATABASE` 则会提示 `No database selected`；

- 如果 `SCHEMA` 中既不存在表也不存在视图，则会删除该 `SCHEMA`。

- `tableName`、`storageUnitName` 和 `schemaName` 可以写成不带引号的标识符、反引号包裹的标识符或双引号包裹的标识符。

- 显式刷新始终校验所选逻辑表关联的所有物理表，包括其他存储单元或 `SCHEMA` 中的分表，不受 `check-table-metadata-enabled` 配置影响。
  结构不一致或部分物理表缺失时，刷新会在发布新元数据之前失败。
  `FROM STORAGE UNIT` 用于选择刷新目标，不会将一致性校验限制在该存储单元。

- 节点应用表结构变更或删除后，基于旧结构 prepare 的语句会报错并提示 `Table metadata changed; prepare the statement again.`，需要重新 prepare。
  ShardingSphere-JDBC 和 MySQL 协议暴露错误码 `10025`、SQLState `HY000`；PostgreSQL 和 openGauss 通过既有协议映射返回 SQLState `58000`。
  结构未变或不相关表的旧绑定仍可使用，已经创建的结果集仍可读取。
  集群模式下，各节点应用元数据变更后分别生效。

- 直接在后端修改表后，应先确保所有相关物理表结构一致，再显式刷新元数据，最后重新 prepare 受影响的语句。
  prepare 本身不会自动刷新共享元数据。

### 示例

- 刷新指定存储单元中指定 `SCHEMA` 中指定表的元数据

```sql
REFRESH TABLE METADATA `t_order` FROM STORAGE UNIT "ds_1" SCHEMA `db_schema`;
```

- 刷新指定存储单元中指定 `SCHEMA` 中所有表的元数据

```sql
REFRESH TABLE METADATA FROM STORAGE UNIT `ds_1` SCHEMA "db_schema";
```

- 刷新指定存储单元中指定表的元数据

```sql
REFRESH TABLE METADATA "t_order" FROM STORAGE UNIT ds_1;
```

- 刷新指定表的元数据

```sql
REFRESH TABLE METADATA `t_order`;
```

- 刷新所有表的元数据

```sql
REFRESH TABLE METADATA;
```

### 保留字

`REFRESH`、`TABLE`、`METADATA`、`FROM`、`STORAGE`、`UNIT`

### 相关链接

- [保留字](/cn/user-manual/shardingsphere-proxy/distsql/syntax/reserved-word/)
