+++
title = "Spring 命名空间配置"
weight = 3

+++

## ShardingSphere-5.0.0-beta

### 数据分片

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd)

\<sharding:rule />

| *名称*                                | *类型* | *说明*              |
| ------------------------------------- | ------ | ------------------ |
| id                                    | 属性   | Spring Bean Id     |
| table-rules (?)                       | 标签   | 分片表规则配置       |
| auto-table-rules (?)                  | 标签   | 自动化分片表规则配置  |
| binding-table-rules (?)               | 标签   | 绑定表规则配置        |
| broadcast-table-rules (?)             | 标签   | 广播表规则配置        |
| default-database-strategy-ref (?)     | 属性   | 默认分库策略名称      |
| default-table-strategy-ref (?)        | 属性   | 默认分表策略名称      |
| default-key-generate-strategy-ref (?) | 属性   | 默认分布式序列策略名称 |

\<sharding:table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| table-rule (+) | 标签   | 分片表规则配置 |

\<sharding:table-rule />

| *名称*                     | *类型* | *说明*          |
| ------------------------- | ----- | --------------- |
| logic-table               | 属性  | 逻辑表名称        |
| actual-data-nodes         | 属性  | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| database-strategy-ref     | 属性  | 标准分片表分库策略名称      |
| table-strategy-ref        | 属性  | 标准分片表分表策略名称      |
| key-generate-strategy-ref | 属性  | 分布式序列策略名称 |

\<auto-table-rules/>

| *名称*          | *类型* | *说明*                 |
| -------------- | ------ | --------------------- |
| auto-table-rule (+) | 标签   | 自动化分片表规则配置 |

\<auto-table-rule/>
| *名称*                     | *类型* | *说明*          |
| ------------------------- | ----- | --------------- |
| logic-table               | 属性  | 逻辑表名称        |
| actual-data-sources       | 属性  | 自动分片表数据源名 |
| sharding-strategy-ref     | 属性  | 自动分片表策略名称 |
| key-generate-strategy-ref | 属性  | 分布式序列策略名称 |

\<sharding:binding-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| binding-table-rule (+) | 标签   | 绑定表规则配置 |

\<sharding:binding-table-rule />

| *名称*       | *类型*  | *说明*                   |
| ------------ | ------ | ------------------------ |
| logic-tables | 属性   | 绑定表名称，多个表以逗号分隔 |

\<sharding:broadcast-table-rules />

| *名称*                  | *类型* | *说明*       |
| ---------------------- | ------ | ------------ |
| broadcast-table-rule (+) | 标签   | 广播表规则配置 |

\<sharding:broadcast-table-rule />

| *名称* | *类型* | *说明*   |
| ------ | ----- | -------- |
| table  | 属性  | 广播表名称 |

\<sharding:standard-strategy />

| *名称*          | *类型* | *说明*          |
| --------------- | ----- | -------------- |
| id              | 属性   | 标准分片策略名称 |
| sharding-column | 属性   | 分片列名称      |
| algorithm-ref   | 属性   | 分片算法名称    |

\<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                    |
| ---------------- | ----- | ------------------------- |
| id               | 属性   | 复合分片策略名称            |
| sharding-columns | 属性   | 分片列名称，多个列以逗号分隔 |
| algorithm-ref    | 属性   | 分片算法名称               |

\<sharding:hint-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | Hint 分片策略名称 |
| algorithm-ref | 属性   | 分片算法名称      |

\<sharding:none-strategy />

| *名称* | *类型* | *说明*      |
| ------ | ----- | ----------- |
| id     | 属性   | 分片策略名称 |

\<sharding:key-generate-strategy />

| *名称*        | *类型* | *说明*           |
| ------------- | ----- | ---------------- |
| id            | 属性   | 分布式序列策略名称 |
| column        | 属性   | 分布式序列列名称   |
| algorithm-ref | 属性   | 分布式序列算法名称 |

\<sharding:sharding-algorithm />

| *名称*    | *类型* | *说明*        |
| --------- | ----- | ------------- |
| id        | 属性  | 分片算法名称    |
| type      | 属性  | 分片算法类型    |
| props (?) | 标签  | 分片算法属性配置 |

\<sharding:key-generate-algorithm />

| *名称*    | *类型* | *说明*              |
| --------- | ----- | ------------------ |
| id        | 属性  | 分布式序列算法名称    |
| type      | 属性  | 分布式序列算法类型    |
| props (?) | 标签  | 分布式序列算法属性配置 |

算法类型的详情，请参见[内置分片算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding)和[内置分布式序列算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen)。

#### 注意事项

行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。

### 读写分离

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.0.0.xsd)

\<readwrite-splitting:rule />

| *名称*                | *类型* | *说明*           |
| -------------------- | ------ | --------------- |
| id                   | 属性   | Spring Bean Id   |
| data-source-rule (+) | 标签   | 读写分离数据源规则配置 |

\<readwrite-splitting:data-source-rule />

| *名称*                          | *类型* | *说明*                               |
| ------------------------------- | ------ | ------------------------------------ |
| id                              | 属性   | 读写分离数据源规则名称               |
| auto-aware-data-source-name (?) | 属性   | 自动感知数据源名称                   |
| write-data-source-name          | 属性   | 写数据源名称                         |
| read-data-source-names          | 属性   | 读数据源名称，多个读数据源用逗号分隔 |
| load-balance-algorithm-ref (?)  | 属性   | 负载均衡算法名称                     |

\<readwrite-splitting:load-balance-algorithm />

| *名称*    | *类型* | *说明*            |
| --------- | ----- | ----------------- |
| id        | 属性  | 负载均衡算法名称    |
| type      | 属性  | 负载均衡算法类型    |
| props (?) | 标签  | 负载均衡算法属性配置 |

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。

### 数据加密

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd)

\<encrypt:rule />

| *名称*                     | *类型* | *说明*                                               | *默认值* |
| ------------------------- | ----- | ---------------------------------------------------- | ------- |
| id                        | 属性  | Spring Bean Id                                        |         |
| queryWithCipherColumn (?) | 属性  | 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询 | true   |
| table (+)                 | 标签  | 加密表配置                                              |         |

\<encrypt:table />

| *名称*     | *类型* | *说明*    |
| ---------- | ----- | -------- |
| name       | 属性  | 加密表名称 |
| column (+) | 标签  | 加密列配置 |

\<encrypt:column />

| *名称*                    | *类型* | *说明*       |
| ------------------------- | ----- | ------------ |
| logic-column              | 属性  | 加密列逻辑名称 |
| cipher-column             | 属性  | 加密列名称    |
| assisted-query-column (?) | 属性  | 查询辅助列名称 |
| plain-column (?)          | 属性  | 原文列名称     |
| encrypt-algorithm-ref     | 属性  | 加密算法名称   |

\<encrypt:encrypt-algorithm />

| *名称*    | *类型* | *说明*         |
| --------- | ----- | ------------- |
| id        | 属性  | 加密算法名称    |
| type      | 属性  | 加密算法类型    |
| props (?) | 标签  | 加密算法属性配置 |

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/encrypt)。

### 影子库

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd)

\<shadow:rule />

| *名称*      | *类型* | *说明*                          |
| ----------- | ----- | ------------------------------- |
| id          | 属性  | Spring Bean Id                  |
| column      | 属性  | 影子字段名称                      |
| mappings(?) | 标签  | 生产数据库与影子数据库的映射关系配置 |

\<shadow:mapping />

| *名称*                   | *类型* | *说明*                          |
| ------------------------ | ----- | ------------------------------- |
| product-data-source-name | 属性  | 生产数据库名称                    |
| shadow-data-source-name  | 属性  | 影子数据库名称                    |

### 分布式治理

#### 配置项说明

命名空间: [http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/governance/governance-5.0.0.xsd)

\<governance:reg-center />

| *名称*         | *类型* | *说明*                                                                     |
| ------------- | ------ | ------------------------------------------------------------------------- |
| id            | 属性   | 注册中心实例名称                                                              |
| schema-name   | 属性   | JDBC数据源别名，该参数可实现JDBC与PROXY共享配置                                  |
| type          | 属性   | 注册中心类型。如：ZooKeeper, etcd                                              |
| namespace     | 属性   | 注册中心命名空间                                                              |
| server-lists  | 属性   | 注册中心服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| props (?)     | 属性   | 配置本实例需要的其他参数，例如 ZooKeeper 的连接参数等                               |

## ShardingSphere-4.x

### 数据分片

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-4.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-4.0.0.xsd)

\<sharding:data-source />

| *名称*        | *类型* | *说明*           |
| ------------- | ------ | --------------- |
| id            | 属性   | Spring Bean Id   |
| sharding-rule | 标签   | 数据分片配置规则 |
| props (?)     | 标签   | 属性配置         |

\<sharding:sharding-rule />

| *名称*                            | *类型* | *说明*                                                       |
| --------------------------------- | ------ | ------------------------------------------------------------ |
| data-source-names                 | 属性   | 数据源Bean列表，多个Bean以逗号分隔                           |
| table-rules                       | 标签   | 表分片规则配置对象                                           |
| binding-table-rules (?)           | 标签   | 绑定表规则列表                                               |
| broadcast-table-rules (?)         | 标签   | 广播表规则列表                                               |
| default-data-source-name (?)      | 属性   | 未配置分片规则的表将通过默认数据源定位                       |
| default-database-strategy-ref (?) | 属性   | 默认数据库分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示不分库 |
| default-table-strategy-ref (?)    | 属性   | 默认表分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示不分表 |
| default-key-generator-ref (?)     | 属性   | 默认自增列值生成器引用，缺省使用 <span style='background: #FFF7DD'>org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator</span> |
| encrypt-rule (?)                  | 标签   | 脱敏规则                                                     |

\<sharding:table-rules />

| *名称*         | *类型* | *说明*             |
| -------------- | ------ | ------------------ |
| table-rule (+) | 标签   | 表分片规则配置对象 |

\<sharding:table-rule />

| *名称*                    | *类型* | *说明*                                                       |
| ------------------------- | ------ | ------------------------------------------------------------ |
| logic-table               | 属性   | 逻辑表名称                                                   |
| actual-data-nodes (?)     | 属性   | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| database-strategy-ref (?) | 属性   | 数据库分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示使用 <sharding:sharding-rule /> 配置的默认数据库分片策略 |
| table-strategy-ref (?)    | 属性   | 表分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示使用 \<sharding:sharding-rule /> 配置的默认表分片策略 |
| key-generator-ref (?)     | 属性   | 自增列值生成器引用，缺省表示使用默认自增列值生成器           |

\<sharding:binding-table-rules />

| *名称*                 | *类型* | *说明*     |
| ---------------------- | ------ | ---------- |
| binding-table-rule (+) | 标签   | 绑定表规则 |

\<sharding:binding-table-rule />

| *名称*       | *类型* | *说明*                             |
| ------------ | ------ | ---------------------------------- |
| logic-tables | 属性   | 绑定规则的逻辑表名，多表以逗号分隔 |

\<sharding:broadcast-table-rules />

| *名称*                   | *类型* | *说明*     |
| ------------------------ | ------ | ---------- |
| broadcast-table-rule (+) | 标签   | 广播表规则 |


\<sharding:broadcast-table-rule />

| *名称*                   | *类型* | *说明*     |
| ------------------------ | ------ | ---------- |
| table | 属性   | 广播规则的表名 |

\<sharding:standard-strategy />

| *名称*                  | *类型* | *说明*                                                       |
| ----------------------- | ------ | ------------------------------------------------------------ |
| id                      | 属性   | Spring Bean Id                                               |
| sharding-column         | 属性   | 分片列名称                                                   |
| precise-algorithm-ref   | 属性   | 精确分片算法引用，用于=和IN。该类需实现PreciseShardingAlgorithm接口 |
| range-algorithm-ref (?) | 属性   | 范围分片算法引用，用于BETWEEN。该类需实现RangeShardingAlgorithm接口 |

\<sharding:complex-strategy />

| *名称*                   | *类型* | *说明*     |
| ------------------------ | ------ | ---------- |
| id | 属性   | Spring Bean Id |
| sharding-columns | 属性 | 分片列名称，多个列以逗号分隔 |
| algorithm-ref | 属性 | 复合分片算法引用。该类需实现ComplexKeysShardingAlgorithm接口 |

\<sharding:inline-strategy />

| *名称*               | *类型* | *说明*                             |
| -------------------- | ------ | ---------------------------------- |
| id                   | 属性   | Spring Bean Id                     |
| sharding-column      | 属性   | 分片列名称                         |
| algorithm-expression | 属性   | 分片算法行表达式，需符合groovy语法 |

\<sharding:hint-database-strategy />

| *名称*        | *类型* | *说明*                                            |
| ------------- | ------ | ------------------------------------------------- |
| id            | 属性   | Spring Bean Id                                    |
| algorithm-ref | 属性   | Hint分片算法。该类需实现HintShardingAlgorithm接口 |

\<sharding:none-strategy />

| *名称*               | *类型* | *说明*                             |
| -------------------- | ------ | ---------------------------------- |
| id                   | 属性   | Spring Bean Id                     |

\<sharding:key-generator />

| *名称*    | *类型* | *说明*                                                     |
| --------- | ------ | ---------------------------------------------------------- |
| column    | 属性   | 自增列名称                                                 |
| type      | 属性   | 自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID |
| props-ref | 属性   | 自增列值生成器的属性配置引用                               |

##### Properties

属性配置项，可以为以下自增列值生成器的属性。

###### SNOWFLAKE

| *名称*                                        | *类型* | *说明*                                                       |
| --------------------------------------------- | ------ | ------------------------------------------------------------ |
| worker.id (?)                                 | long   | 工作机器唯一id，默认为0                                      |
| max.tolerate.time.difference.milliseconds (?) | long   | 最大容忍时钟回退时间，单位：毫秒。默认为10毫秒               |
| max.vibration.offset (?)                      | int    | 最大抖动上限值，范围[0, 4096)，默认为1。注：若使用此算法生成值作分片值，建议配置此属性。此算法在不同毫秒内所生成的key取模2^n (2^n一般为分库或分表数) 之后结果总为0或1。为防止上述分片问题，建议将此属性值配置为(2^n)-1 |

\<sharding:encrypt-rule />

| *名称*                  | *类型* | *说明*     |
| ----------------------- | ------ | ---------- |
| encrypt:encrypt-rule (?) | 标签   | 加解密规则 |

<sharding:props />

| *名称*                  | *类型* | *说明*     |
| ----------------------- | ------ | ---------- |
| sql.show (?) | 属性   | 是否开启SQL显示，默认值: false |
| executor.size (?) | 属性   | 工作线程数量，默认值: CPU核数 |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?) | 属性   | 是否在启动时检查分表元数据一致性，默认值: false |
| query.with.cipher.column (?) | 属性   | 当存在明文列时，是否使用密文列查询，默认值: true |

### 读写分离

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd](http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd)

\<master-slave:data-source />

| *名称*                  | *类型* | *说明*                                                       |
| ----------------------- | ------ | ------------------------------------------------------------ |
| id                      | 属性   | Spring Bean Id                                               |
| master-data-source-name | 属性   | 主库数据源Bean Id                                            |
| slave-data-source-names | 属性   | 从库数据源Bean Id列表，多个Bean以逗号分隔                    |
| strategy-ref (?)        | 属性   | 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口 |
| strategy-type (?)       | 属性   | 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若 <span style='background: #FFF7DD'>strategy-ref</span> 存在则忽略该配置 |
| props (?)               | 标签   | 属性配置                                                     |

\<master-slave:props />

| *名称*                             | *类型* | *说明*                                                |
| ---------------------------------- | ------ | ----------------------------------------------------- |
| sql.show (?)                       | 属性   | 是否开启SQL显示，默认值: false                        |
| executor.size (?)                  | 属性   | 工作线程数量，默认值: CPU核数                         |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性   | 是否在启动时检查分表元数据一致性，默认值: false       |

\<master-slave:load-balance-algorithm />
4.0.0-RC2 版本 添加

| *名称*        | *类型* | *说明*                                                    |
| ------------- | ------ | --------------------------------------------------------- |
| id            | 属性   | Spring Bean Id                                            |
| type (?)      | 属性   | 负载均衡算法类型，‘RANDOM'或’ROUND_ROBIN’，支持自定义拓展 |
| props-ref (?) | 属性   | 负载均衡算法配置参数                                      |

### 数据脱敏

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd)

\<encrypt:data-source />

| *名称*           | *类型* | *说明*            |
| ---------------- | ------ | ----------------- |
| id               | 属性   | Spring Bean Id    |
| data-source-name | 属性   | 加密数据源Bean Id |
| props (?)        | 标签   | 属性配置          |

\<encrypt:encryptors />

| *名称*       | *类型* | *说明*     |
| ------------ | ------ | ---------- |
| encryptor (+) | 标签   | 加密器配置 |

\<encrypt:encryptor />

| *名称*    | *类型* | *说明*                                                       |
| --------- | ------ | ------------------------------------------------------------ |
| id        | 属性   | 加密器的名称                                                 |
| type      | 属性   | 加解密器类型，可自定义或选择内置类型：MD5/AES                |
| props-ref | 属性   | 属性配置, 注意：使用AES加密器，需要配置AES加密器的KEY属性：aes.key.value |

\<encrypt:tables />

| *名称*       | *类型* | *说明*     |
| ------------ | ------ | ---------- |
| table (+) | 标签   | 加密表配置 |

\<encrypt:table />

| *名称*       | *类型* | *说明*     |
| ------------ | ------ | ---------- |
| column (+) | 标签   | 加密列配置 |

\<encrypt:column />

| *名称*                 | *类型* | *说明*                                                       |
| ---------------------- | ------ | ------------------------------------------------------------ |
| logic-column           | 属性   | 逻辑列名                                                     |
| plain-column           | 属性   | 存储明文的字段                                               |
| cipher-column          | 属性   | 存储密文的字段                                               |
| assisted-query-columns | 属性   | 辅助查询字段，针对ShardingQueryAssistedEncryptor类型的加解密器进行辅助查询 |

\<encrypt:props />

| *名称*                       | *类型* | *说明*                                           |
| ---------------------------- | ------ | ------------------------------------------------ |
| sql.show (?)                 | 属性   | 是否开启SQL显示，默认值: false                   |
| query.with.cipher.column (?) | 属性   | 当存在明文列时，是否使用密文列查询，默认值: true |

### 治理

#### 数据分片 + 治理

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:sharding-data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | ID                                                           |
| data-source-ref (?) | 属性   | 被治理的数据库id                                             |
| registry-center-ref | 属性   | 注册中心id                                                   |
| overwrite           | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。缺省为不覆盖 |

#### 读写分离 + 治理

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:master-slave-data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | ID                                                           |
| data-source-ref (?) | 属性   | 被治理的数据库id                                             |
| registry-center-ref | 属性   | 注册中心id                                                   |
| overwrite           | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。缺省为不覆盖 |

#### 数据脱敏 + 治理

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:encrypt-data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | ID                                                           |
| data-source-ref (?) | 属性   | 被治理的数据库id                                             |
| registry-center-ref | 属性   | 注册中心id                                                   |
| overwrite           | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。缺省为不覆盖 |

#### 治理注册中心

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:registry-center />

| *名称*                             | *类型* | *说明*                                                       |
| ---------------------------------- | ------ | ------------------------------------------------------------ |
| id                                 | 属性   | 注册中心的Spring Bean Id                                     |
| type                               | 属性   | 注册中心类型。如：zookeeper                                  |
| server-lists                       | 属性   | 连接注册中心服务器的列表，包括IP地址和端口号，多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace (?)                      | 属性   | 注册中心的命名空间                                           |
| digest (?)                         | 属性   | 连接注册中心的权限令牌。缺省为不需要权限验证                 |
| operation-timeout-milliseconds (?) | 属性   | 操作超时的毫秒数，默认500毫秒                                |
| max-retries (?)                    | 属性   | 连接失败后的最大重试次数，默认3次                            |
| retry-interval-milliseconds (?)    | 属性   | 重试间隔毫秒数，默认500毫秒                                  |
| time-to-live-seconds (?)           | 属性   | 临时节点存活秒数，默认60秒                                   |
| props-ref (?)                      | 属性   | 配置中心其它属性                                             |

## ShardingSphere-3.x

### 数据分片

#### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd)

\<sharding:data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | Spring Bean Id                                                           |
| sharding-rule | 标签   | 数据分片配置规则  |
| config-map (?) | 标签   | 用户自定义配置   |
| props (?)           | 标签   | 属性配置 |

\<sharding:sharding-rule />

| *名称*                            | *类型* | *说明*                                                       |
| --------------------------------- | ------ | ------------------------------------------------------------ |
| data-source-names                 | 属性   | 数据源Bean列表，多个Bean以逗号分隔                           |
| table-rules                       | 标签   | 表分片规则配置对象                                           |
| binding-table-rules (?)           | 标签   | 绑定表规则列表                                               |
| broadcast-table-rules (?)         | 标签   | 广播表规则列表                                               |
| default-data-source-name (?)      | 属性   | 未配置分片规则的表将通过默认数据源定位                       |
| default-database-strategy-ref (?) | 属性   | 默认数据库分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示不分库 |
| default-table-strategy-ref (?)    | 属性   | 默认表分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示不分表 |
| default-key-generator-ref (?)     | 属性   | 默认自增列值生成器引用，缺省使用 <span style='background: #FFF7DD'>io.shardingsphere.core.keygen.DefaultKeyGenerator</span>。该类需实现KeyGenerator接口 |

\<sharding:table-rules />

| *名称*         | *类型* | *说明*             |
| -------------- | ------ | ------------------ |
| table-rule (+) | 标签   | 表分片规则配置对象 |

\<sharding:table-rule />

| *名称*                       | *类型* | *说明*                                                       |
| ---------------------------- | ------ | ------------------------------------------------------------ |
| logic-table                  | 属性   | 逻辑表名称                                                   |
| actual-data-nodes (?)        | 属性   | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| database-strategy-ref (?)    | 属性   | 数据库分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示使用 \<sharding:sharding-rule /> 配置的默认数据库分片策略 |
| table-strategy-ref (?)       | 属性   | 表分片策略，对应 \<sharding:xxx-strategy> 中的策略Id，缺省表示使用 \<sharding:sharding-rule /> 配置的默认表分片策略 |
| generate-key-column-name (?) | 属性   | 自增列名称，缺省表示不使用自增主键生成器                     |
| key-generator-ref (?)        | 属性   | 自增列值生成器引用，缺省表示使用默认自增列值生成器.该类需实现KeyGenerator接口 |
| logic-index (?)              | 属性   | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表 |

\<sharding:binding-table-rules />

| *名称*         | *类型* | *说明*             |
| -------------- | ------ | ------------------ |
| binding-table-rule (+) | 标签   | 绑定表规则 |

<sharding:broadcast-table-rule />

| *名称* | *类型* | *说明*         |
| ------ | ------ | -------------- |
| table  | 属性   | 广播规则的表名 |

\<sharding:standard-strategy />

| *名称*                  | *类型* | *说明*                                                       |
| ----------------------- | ------ | ------------------------------------------------------------ |
| id                      | 属性   | Spring Bean Id                                               |
| sharding-column         | 属性   | 分片列名称                                                   |
| precise-algorithm-ref   | 属性   | 精确分片算法引用，用于=和IN。该类需实现PreciseShardingAlgorithm接口 |
| range-algorithm-ref (?) | 属性   | 范围分片算法引用，用于BETWEEN。该类需实现RangeShardingAlgorithm接口 |

\<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                                                       |
| ---------------- | ------ | ------------------------------------------------------------ |
| id               | 属性   | Spring Bean Id                                               |
| sharding-columns | 属性   | 分片列名称，多个列以逗号分隔                                 |
| algorithm-ref    | 属性   | 复合分片算法引用。该类需实现ComplexKeysShardingAlgorithm接口 |

\<sharding:inline-strategy />

| *名称*               | *类型* | *说明*                             |
| -------------------- | ------ | ---------------------------------- |
| id                   | 属性   | Spring Bean Id                     |
| sharding-column      | 属性   | 分片列名称                         |
| algorithm-expression | 属性   | 分片算法行表达式，需符合groovy语法 |

\<sharding:hint-database-strategy />

| *名称*        | *类型* | *说明*                                            |
| ------------- | ------ | ------------------------------------------------- |
| id            | 属性   | Spring Bean Id                                    |
| algorithm-ref | 属性   | Hint分片算法。该类需实现HintShardingAlgorithm接口 |

\<sharding:none-strategy />

| *名称* | *类型* | *说明*         |
| ------ | ------ | -------------- |
| id     | 属性   | Spring Bean Id |

\<sharding:props />

| *名称*                             | *类型* | *说明*                                                |
| ---------------------------------- | ------ | ----------------------------------------------------- |
| sql.show (?)                       | 属性   | 是否开启SQL显示，默认值: false                        |
| executor.size (?)                  | 属性   | 工作线程数量，默认值: CPU核数                         |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性   | 是否在启动时检查分表元数据一致性，默认值: false       |

\<sharding:config-map />

### 读写分离

#### 配置项说明

命名空间：[http://apache.shardingsphere.org/schema/shardingsphere/masterslave/master-slave.xsd](http://apache.shardingsphere.org/schema/shardingsphere/masterslave/master-slave.xsd)

\<master-slave:data-source />

| *名称*      | *类型* | *说明*                       |
| ----------- | ------ | ---------------------------- |
| id   | 属性   | Spring Bean Id  |
| master-data-source-name | 属性   | 主库数据源Bean Id |
| slave-data-source-names | 属性   | 从库数据源Bean Id列表，多个Bean以逗号分隔 |
| strategy-ref (?) | 属性   | 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口 |
| strategy-type (?) | 属性   | 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若strategy-ref存在则忽略该配置 |
| config-map (?) | 属性   | 用户自定义配置 |
| props (?) | 属性   | 属性配置 |

\<master-slave:config-map />

\<master-slave:props />

| *名称*                             | *类型* | *说明*                                                |
| ---------------------------------- | ------ | ----------------------------------------------------- |
| sql.show (?)                       | 属性   | 是否开启SQL显示，默认值: false                        |
| executor.size (?)                  | 属性   | 工作线程数量，默认值: CPU核数                         |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性   | 是否在启动时检查分表元数据一致性，默认值: false       |

### 治理

#### 数据分片 + 数据治理

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:sharding-data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | ID                                                           |
| data-source-ref (?) | 属性   | 被治理的数据库id                                             |
| registry-center-ref | 属性   | 注册中心id                                                   |
| overwrite (?)       | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。 缺省为不覆盖 |

#### 读写分离 + 数据治理

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:master-slave-data-source />

| *名称*              | *类型* | *说明*                                                       |
| ------------------- | ------ | ------------------------------------------------------------ |
| id                  | 属性   | ID                                                           |
| data-source-ref (?) | 属性   | 被治理的数据库id                                             |
| registry-center-ref | 属性   | 注册中心id                                                   |
| overwrite (?)       | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。 缺省为不覆盖 |

#### 数据治理注册中心

##### 配置项说明

命名空间：[http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:registry-center />

| *名称*                             | *类型* | *说明*                                                       |
| ---------------------------------- | ------ | ------------------------------------------------------------ |
| id                                 | 属性   | 注册中心的Spring Bean Id                                     |
| server-lists                       | 属性   | 连接注册中心服务器的列表，包括IP地址和端口号，多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace (?)                      | 属性   | 注册中心的命名空间                                           |
| digest (?)                         | 属性   | 连接注册中心的权限令牌。缺省为不需要权限验证                 |
| operation-timeout-milliseconds (?) | 属性   | 操作超时的毫秒数，默认500毫秒                                |
| max-retries (?)                    | 属性   | 连接失败后的最大重试次数，默认3次                            |
| retry-interval-milliseconds (?)    | 属性   | 重试间隔毫秒数，默认500毫秒                                  |
| time-to-live-seconds (?)           | 属性   | 临时节点存活秒数，默认60秒                                   |

## ShardingSphere-2.x

### 数据分片

#### 配置项说明

\<sharding:data-source/>

定义sharding-jdbc数据源

| *名称*                 | *类型* | *数据类型* | *必填* | *说明*         |
| ---------------------- | ------ | ---------- | ------ | -------------- |
| id                     | 属性   | String     | 是     | Spring Bean ID |
| sharding-rule          | 标签   | -          | 是     | 分片规则       |
| binding-table-rules (?) | 标签   | -          | 否     | 分片规则       |
| props (?)              | 标签   | -          | 否     | 相关属性配置   |

\<sharding:sharding-rule/>

| *名称*                        | *类型* | *数据类型* | *必填* | *说明*                                                       |
| ----------------------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| data-source-names             | 属性   | String     | 是     | 数据源Bean列表，需要配置所有需要被 ShardingSphere-JDBC 管理的数据源BEAN ID（包括默认数据源），多个Bean以逗号分隔 |
| default-data-source-name (?)   | 属性   | String     | 否     | 默认数据源名称，未配置分片规则的表将通过默认数据源定位       |
| default-database-strategy-ref (?) | 属性   | String     | 否     | 默认分库策略，对应 \<sharding:xxx-strategy> 中的策略id，不填则使用不分库的策略 |
| default-table-strategy-ref (?)  | 属性   | String     | 否     | 默认分表策略，对应 \<sharding:xxx-strategy> 中的策略id，不填则使用不分表的策略 |
| table-rules                   | 标签   | -          | 是     | 分片规则列表                                                 |

\<sharding:table-rules/>

| *名称*        | *类型* | *数据类型* | *必填* | *说明*   |
| ------------- | ------ | ---------- | ------ | -------- |
| table-rule(+) | 标签   | -          | 是     | 分片规则 |

\<sharding:table-rule/>

| *名称*                | *类型* | *数据类型* | *必填* | *说明*                                                       |
| --------------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| logic-table           | 属性   | String     | 是     | 逻辑表名                                                     |
| actual-data-nodes (?) | 属性   | String     | 否     | 真实数据节点，由数据源名（读写分离引用[master-slave:data-source](master-slave:data-source)中的id属性） + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。不填写表示将为现有已知的数据源 + 逻辑表名称生成真实数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况。 |
| database-strategy-ref (?) | 属性   | String     | 否     | 分库策略，对应 \<sharding:xxx-strategy> 中的策略id，不填则使用 \<sharding:sharding-rule/> 配置的default-database-strategy-ref |
| table-strategy-ref (?) | 属性   | String     | 否     | 分表策略，对应 \<sharding:xxx-strategy> 中的略id，不填则使用 \<sharding:sharding-rule/> 配置的default-table-strategy-ref |
| logic-index (?)       | 属性   | String     | 否     | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表 |

\<sharding:binding-table-rules/>

| *名称*             | *类型* | *数据类型* | *必填* | *说明*   |
| ------------------ | ------ | ---------- | ------ | -------- |
| binding-table-rule | 标签   | -          | 是     | 绑定规则 |

\<sharding:binding-table-rule/>

| *名称*             | *类型* | *数据类型* | *必填* | *说明*   |
| ------------------ | ------ | ---------- | ------ | -------- |
| logic-tables | 属性   | String | 是     | 逻辑表名，多个表名以逗号分隔 |

\<sharding:standard-strategy/>
标准分片策略，用于单分片键的场景

| *名称*                  | *类型* | *数据类型* | *必填* | *说明*                                                       |
| ----------------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| sharding-column         | 属性   | String     | 是     | 分片列名                                                     |
| precise-algorithm-class | 属性   | String     | 是     | 精确的分片算法类名称，用于=和IN。该类需使用默认的构造器或者提供无参数的构造器 |
| range-algorithm-class (?) | 属性   | String     | 否     | 范围的分片算法类名称，用于BETWEEN。该类需使用默认的构造器或者提供无参数的构造器 |

\<sharding:complex-strategy/>
复合分片策略，用于多分片键的场景

| *名称*           | *类型* | *数据类型* | *必填* | *说明*                                                       |
| ---------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| sharding-columns | 属性   | String     | 是     | 分片列名，多个列以逗号分隔                                   |
| algorithm-class  | 属性   | String     | 是     | 分片算法全类名，该类需使用默认的构造器或者提供无参数的构造器 |

\<sharding:inline-strategy/>
inline表达式分片策略

| *名称*               | *类型* | *数据类型* | *必填* | *说明*         |
| -------------------- | ------ | ---------- | ------ | -------------- |
| sharding-column      | 属性   | String     | 是     | 分片列名       |
| algorithm-expression | 属性   | String     | 是     | 分片算法表达式 |

\<sharding:hint-database-strategy/>
Hint方式分片策略

| *名称*          | *类型* | *数据类型* | *必填* | *说明*                                                       |
| --------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| algorithm-class | 属性   | String     | 是     | 分片算法全类名，该类需使用默认的构造器或者提供无参数的构造器 |

\<sharding:none-strategy/>
不分片的策略

\<sharding:props/>

| *名称*        | *类型* | *数据类型* | *必填* | *说明*                             |
| ------------- | ------ | ---------- | ------ | ---------------------------------- |
| sql.show      | 属性   | boolean    | 是     | 是否开启SQL显示，默认为false不开启 |
| executor.size (?) | 属性   | int        | 否     | 最大工作线程数量                   |

### 读写分离

#### 配置项说明

\<master-slave:data-source/>
定义sharding-jdbc读写分离的数据源

| *名称*                  | *类型* | *数据类型* | *必填* | *说明*                                                       |
| ----------------------- | ------ | ---------- | ------ | ------------------------------------------------------------ |
| id                      | 属性   | String     | 是     | Spring Bean ID                                               |
| master-data-source-name | 标签   | -          | 是     | 主库数据源Bean ID                                            |
| slave-data-source-names | 标签   | -          | 是     | 从库数据源Bean列表，多个Bean以逗号分隔                       |
| strategy-ref (?)         | 标签   | -          | 否     | 主从库复杂策略Bean ID，可以使用自定义复杂策略                |
| strategy-type (?)        | 标签   | String     | 否     | 主从库复杂策略类型 <br/>可选值：ROUND_ROBIN, RANDOM <br/>默认值：ROUND_ROBIN |

##### Spring格式特别说明

如需使用inline表达式，需配置ignore-unresolvable为true，否则placeholder会把inline表达式当成属性key值导致出错。

#### 分片算法表达式语法说明

##### inline表达式特别说明

${begin..end} 表示范围区间

${[unit1, unit2, unitX]} 表示枚举值

inline表达式中连续多个${…}表达式，整个inline最终的结果将会根据每个子表达式的结果进行笛卡尔组合，例如正式表inline表达式如下：

```xml

dbtbl_${['online', 'offline']}_${1..3}

```

最终会解析为dbtbl_online_1，dbtbl_online_2，dbtbl_online_3，dbtbl_offline_1，dbtbl_offline_2和dbtbl_offline_3这6张表。

##### 字符串内嵌groovy代码

表达式本质上是一段字符串，字符串中使用${}来嵌入groovy代码。

```xml

data_source_${id % 2 + 1}

```

上面的表达式中data_source_是字符串前缀，id % 2 + 1是groovy代码。

### 治理

#### Zookeeper标签说明

| *名称*        | *类型* | *是否必填* | *缺省值* | *描述*                             |
| ------------- | ------ | ---------- | ------ | ---------------------------------- |
| id      | String | 是 |  | 注册中心在Spring容器中的主键 |
| server-lists | String | 是 |  | 	连接Zookeeper服务器的列表<br/>包括IP地址和端口号<br/>多个地址用逗号分隔<br/>如: host1:2181,host2:2181 |
| namespace | String | 是 |  | Zookeeper的命名空间 |
| base-sleep-time-milliseconds (?) | int | 否 | 1000 | 等待重试的间隔时间的初始值<br/>单位：毫秒 |
| max-sleep-time-milliseconds (?) | int | 否 | 3000 | 等待重试的间隔时间的最大值<br/>单位：毫秒 |
| max-retries (?) | int | 否 | 3 | 最大重试次数 |
| session-timeout-milliseconds (?) | int | 否 | 60000 | 会话超时时间<br/>单位：毫秒 |
| connection-timeout-milliseconds (?) | int | 否 | 15000 | 连接超时时间<br/>单位：毫秒 |
| digest (?) | String | 否 |  | 	连接Zookeeper的权限令牌<br/>缺省为不需要权限验证 |

#### Etcd配置示例

| *名称*                         | *类型* | *是否必填* | *缺省值* | *描述*                                                       |
| ------------------------------ | ------ | ---------- | -------- | ------------------------------------------------------------ |
| id                             | String | 是         |          | 注册中心在Spring容器中的主键                                 |
| server-lists                   | String | 是         |          | 连接Etcd服务器的列表<br/>包括IP地址和端口号<br/>多个地址用逗号分隔<br/>如: http://host1:2379,http://host2:2379 |
| time-to-live-seconds (?)        | int    | 否         | 60       | 临时节点存活时间<br/>单位：秒                                |
| timeout-milliseconds (?)        | int    | 否         | 500      | 每次请求的超时时间<br/>单位：毫秒                            |
| max-retries (?)                 | int    | 否         | 3        | 每次请求的最大重试次数                                       |
| retry-interval-milliseconds (?) | int    | 否         | 200      | 重试间隔时间<br/>单位：毫秒                                  |

