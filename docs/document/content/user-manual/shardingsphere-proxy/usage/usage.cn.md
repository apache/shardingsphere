+++
title = "使用手册"
weight = 1
+++

## 分布式事务

ShardingSphere-Proxy接入的分布式事务API同ShardingSphere-JDBC保持一致，支持LOCAL，XA，BASE类型的事务。

### XA事务

ShardingSphere-Proxy原生支持XA事务，默认的事务管理器为Atomikos。
可以通过在ShardingSphere-Proxy的conf目录中添加`jta.properties`来定制化Atomikos配置项。
具体的配置规则请参考Atomikos的[官方文档](https://www.atomikos.com/Documentation/JtaProperties)。

### BASE事务

BASE目前没有打包到ShardingSphere-Proxy中，使用时需要将实现了`ShardingTransactionManager`SPI的jar拷贝至conf/lib目录，然后切换事务类型为BASE。

## SCTL (ShardingSphere-Proxy control language)

SCTL为ShardingSphere-Proxy特有的控制语句，可以在运行时修改和查询ShardingSphere-Proxy的状态，目前支持的语法为：

| 语句                                     | 说明                                                                                            |
|:----------------------------------------|:------------------------------------------------------------------------------------------------|
|sctl:set transaction_type=XX             | 修改当前TCP连接的事务类型, 支持LOCAL，XA，BASE。例：sctl:set transaction_type=XA                       |
|sctl:show transaction_type               | 查询当前TCP连接的事务类型                                                                           |
|sctl:show cached_connections             | 查询当前TCP连接中缓存的物理数据库连接个数                                                              |
|sctl:explain SQL语句                      | 查看逻辑SQL的执行计划，例：sctl:explain select * from t_order;                                      |
|sctl:hint set MASTER_ONLY=true           | 针对当前TCP连接，是否将数据库操作强制路由到主库                                                         |
|sctl:hint set DatabaseShardingValue=yy   | 针对当前TCP连接，设置hint仅对数据库分片有效，并添加分片值，yy：数据库分片值                                 |
|sctl:hint addDatabaseShardingValue xx=yy | 针对当前TCP连接，为表xx添加分片值yy，xx：逻辑表名称，yy：数据库分片值                                      |
|sctl:hint addTableShardingValue xx=yy    | 针对当前TCP连接，为表xx添加分片值yy，xx：逻辑表名称，yy：表分片值                                         |
|sctl:hint clear                          | 针对当前TCP连接，清除hint所有设置                                                                    |
|sctl:hint show status                    | 针对当前TCP连接，查询hint状态，master_only:true/false，sharding_type:databases_only/databases_tables |
|sctl:hint show table status              | 针对当前TCP连接，查询逻辑表的hint分片值                                                               |

ShardingSphere-Proxy 默认不支持hint，如需支持，请在conf/server.yaml中，将`properties`的属性`proxy.hint.enabled`设置为true。在ShardingSphere-Proxy中，HintShardingAlgorithm的泛型只能是String类型。
