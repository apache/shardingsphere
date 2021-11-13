+++
title = "混合规则"
weight = 6
+++

混合配置的规则项之间的叠加使用是通过数据源名称和表名称关联的。

如果前一个规则是面向数据源聚合的，下一个规则在配置数据源时，则需要使用前一个规则配置的聚合后的逻辑数据源名称；
同理，如果前一个规则是面向表聚合的，下一个规则在配置表时，则需要使用前一个规则配置的聚合后的逻辑表名称。

## 配置项说明
```properties
# 数据源配置
# 数据源名称，多数据源以逗号分隔
spring.shardingsphere.datasource.names= write-ds0,write-ds1,write-ds0-read0,write-ds1-read0

spring.shardingsphere.datasource.write-ds0.url= # 数据库 URL 连接
spring.shardingsphere.datasource.write-ds0.type=  # 数据库连接池类名称
spring.shardingsphere.datasource.write-ds0.driver-class-name= # 数据库驱动类名
spring.shardingsphere.datasource.write-ds0.username= # 数据库用户名
spring.shardingsphere.datasource.write-ds0.password= # 数据库密码
spring.shardingsphere.datasource.write-ds0.xxx=  # 数据库连接池的其它属性

spring.shardingsphere.datasource.write-ds1.url= # 数据库 URL 连接
... 忽略其他数据库配置项

spring.shardingsphere.datasource.write-ds0-read0.url= # 数据库 URL 连接
... 忽略其他数据库配置项

spring.shardingsphere.datasource.write-ds1-read0.url= # 数据库 URL 连接
... 忽略其他数据库配置项

# 分片规则配置
# 分库策略
spring.shardingsphere.rules.sharding.default-database-strategy.standard.sharding-column=user_id
spring.shardingsphere.rules.sharding.default-database-strategy.standard.sharding-algorithm-name=default-database-strategy-inline
# 绑定表规则，多组绑定规则使用数组形式配置
spring.shardingsphere.rules.sharding.binding-tables[0]=t_user,t_user_detail # 绑定表名称，多个表之间以逗号分隔
spring.shardingsphere.rules.sharding.binding-tables[1]= # 绑定表名称，多个表之间以逗号分隔
spring.shardingsphere.rules.sharding.binding-tables[x]= # 绑定表名称，多个表之间以逗号分隔
# 广播表规则配置
spring.shardingsphere.rules.sharding.broadcast-tables= # 广播表名称，多个表之间以逗号分隔

# 分表策略
# 表达式 `ds_$->{0..1}`枚举的数据源为读写分离配置的逻辑数据源名称
spring.shardingsphere.rules.sharding.tables.t_user.actual-data-nodes=ds_$->{0..1}.t_user_$->{0..1}
spring.shardingsphere.rules.sharding.tables.t_user.table-strategy.standard.sharding-column=user_id
spring.shardingsphere.rules.sharding.tables.t_user.table-strategy.standard.sharding-algorithm-name=user-table-strategy-inline

spring.shardingsphere.rules.sharding.tables.t_user_detail.actual-data-nodes=ds_$->{0..1}.t_user_detail_$->{0..1}
spring.shardingsphere.rules.sharding.tables.t_user_detail.table-strategy.standard.sharding-column=user_id
spring.shardingsphere.rules.sharding.tables.t_user_detail.table-strategy.standard.sharding-algorithm-name=user-detail-table-strategy-inline

# 数据加密配置
# `t_user` 使用分片规则配置的逻辑表名称
spring.shardingsphere.rules.encrypt.tables.t_user.columns.user_name.cipher-column=user_name
spring.shardingsphere.rules.encrypt.tables.t_user.columns.user_name.encryptor-name=name-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.cipher-column=pwd
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.encryptor-name=pwd-encryptor

# 数据加密算法配置
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.props.aes-key-value=123456abc

# 分布式序列策略配置
spring.shardingsphere.rules.sharding.tables.t_user.key-generate-strategy.column=user_id
spring.shardingsphere.rules.sharding.tables.t_user.key-generate-strategy.key-generator-name=snowflake

# 分片算法配置
spring.shardingsphere.rules.sharding.sharding-algorithms.default-database-strategy-inline.type=INLINE
# 表达式`ds_$->{user_id % 2}` 枚举的数据源为读写分离配置的逻辑数据源名称
spring.shardingsphere.rules.sharding.sharding-algorithms.default-database-strategy-inline.algorithm-expression=ds_$->{user_id % 2}
spring.shardingsphere.rules.sharding.sharding-algorithms.user-table-strategy-inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.user-table-strategy-inline.algorithm-expression=t_user_$->{user_id % 2}

spring.shardingsphere.rules.sharding.sharding-algorithms.user-detail-table-strategy-inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.user-detail-table-strategy-inline.algorithm-expression=t_user_detail_$->{user_id % 2}

# 分布式序列算法配置
spring.shardingsphere.rules.sharding.key-generators.snowflake.type=SNOWFLAKE
spring.shardingsphere.rules.sharding.key-generators.snowflake.props.worker-id=123

# 读写分离策略配置
# ds_0,ds_1为读写分离配置的逻辑数据源名称
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_0.write-data-source-name=write-ds0
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_0.read-data-source-names=write-ds0-read0
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_0.load-balancer-name=read-random
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_1.write-data-source-name=write-ds1
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_1.read-data-source-names=write-ds1-read0
spring.shardingsphere.rules.readwrite-splitting.data-sources.ds_1.load-balancer-name=read-random

# 负载均衡算法配置
spring.shardingsphere.rules.readwrite-splitting.load-balancers.read-random.type=RANDOM
```