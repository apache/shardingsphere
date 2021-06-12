+++
title = "数据分片"
weight = 2
+++

## 定义

### Sharding Table Rule

```sql
SHOW SHARDING TABLE tableRule | RULES [FROM schemaName]

tableRule:
    RULE tableName
```
-  支持查询所有数据分片规则和指定表查询

### Sharding Binding Table Rule

```sql
SHOW SHARDING BINDING TABLE RULES [FROM schemaName]
```

### Sharding Broadcast Table Rule

```sql
SHOW SHARDING BROADCAST TABLE RULES [FROM schemaName]
```

## 说明

### Sharding Table Rule

| 列                             | 说明                                 |
| ------------------------------ | ----------------------------------- |
| table                          | 逻辑表名                             |
| actualDataNodes                | 实际的数据节点                        |
| actualDataSources              | 实际的数据源（通过 RDL 创建的规则时显示） |
| databaseStrategyType           | 数据库分片策略类型                     |
| databaseShardingColumn         | 数据库分片键                          |
| databaseShardingAlgorithmType  | 数据库分片算法类型                     |
| databaseShardingAlgorithmProps | 数据库分片算法参数                     |
| tableStrategyType              | 表分片策略类型                        |
| tableShardingColumn            | 表分片键                             |
| tableShardingAlgorithmType     | 表分片算法类型                        |
| tableShardingAlgorithmProps    | 表分片算法参数                        |
| keyGenerateColumn              | 分布式主键生成列                      |
| keyGeneratorType               | 分布式主键生成器类型                   |
| keyGeneratorProps              | 分布式主键生成器参数                   |

### Sharding Binding Table Rule

| 列                    | 说明      |
| --------------------- | -------- |
| shardingBindingTables | 绑定表名称 |

### Sharding Broadcast Table Rule

| 列                      | 说明      |
| ----------------------- | -------- |
| shardingBroadcastTables | 广播表名称 |
