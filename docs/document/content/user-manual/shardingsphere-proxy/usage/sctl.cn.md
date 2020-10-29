+++
title = "SCTL"
weight = 4
+++

SCTL (ShardingSphere Control Language) 为 ShardingSphere 特有的控制语句，
可以在运行时修改和查询 ShardingSphere-Proxy 的状态，目前支持的语法为：

| 语句                                     | 说明                                                                                            |
|:----------------------------------------|:------------------------------------------------------------------------------------------------|
|sctl:set transaction_type=XX             | 修改当前连接的事务类型, 支持LOCAL，XA，BASE。例：sctl:set transaction_type=XA                        |
|sctl:show transaction_type               | 查询当前连接的事务类型                                                                             |
|sctl:show cached_connections             | 查询当前连接中缓存的物理数据库连接个数                                                               |
|sctl:explain SQL                         | 查看逻辑 SQL 的执行计划，例：sctl:explain select * from t_order;                                   |
|sctl:hint set PRIMARY_ONLY=true          | 针对当前连接，是否将数据库操作强制路由到主库                                                          |
|sctl:hint set DatabaseShardingValue=yy   | 针对当前连接，设置 hint 仅对数据库分片有效，并添加分片值，yy：数据库分片值                               |
|sctl:hint addDatabaseShardingValue xx=yy | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：数据库分片值                                   |
|sctl:hint addTableShardingValue xx=yy    | 针对当前连接，为表 xx 添加分片值 yy，xx：逻辑表名称，yy：表分片值                                       |
|sctl:hint clear                          | 针对当前连接，清除 hint 所有设置                                                                     |
|sctl:hint show status                    | 针对当前连接，查询 hint 状态，primary_only:true/false，sharding_type:databases_only/databases_tables |
|sctl:hint show table status              | 针对当前连接，查询逻辑表的 hint 分片值                                                               |

ShardingSphere-Proxy 默认不支持 hint，如需支持，请在 `conf/server.yaml` 中，将 `properties` 的属性 `proxy-hint-enabled` 设置为 true。
