+++
title = "变更历史"
weight = 7
+++

## 5.0.0-alpha

### 读写分离

#### 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.primary-data-source-name= # 主数据源名称
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.replica-data-source-names= # 从数据源名称，多个从数据源用逗号分隔
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.load-balancer-name= # 负载均衡算法名称

# 负载均衡算法配置
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.type= # 负载均衡算法类型
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.props.xxx= # 负载均衡算法属性配置
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。

## 4.1.0

### 影子库

#### 配置项说明

```properties
spring.shardingsphere.orchestration.<orchestration.name>.orchestration-type= # 编排治理类型
spring.shardingsphere.orchestration.<orchestration.name>.instance-type= # 实例类型
spring.shardingsphere.orchestration.<orchestration.name>.server-lists= # 注册中心服务器地址，多个使用逗号隔开
spring.shardingsphere.orchestration.<orchestration.name>.namespace= # 注册中心命名空间
spring.shardingsphere.orchestration.<orchestration.name>.props.overwrite= # 本地配置是否覆盖注册中心配置

spring.shardingsphere.shadow.column= # 影子字段名称
spring.shardingsphere.shadow.shadow-mappings.<product-data-source-name>= # 影子数据库名称 

spring.shardingsphere.shadow.shardingRule.default-data-source-name= # 默认影子库
spring.shardingsphere.shadow.shardingRule.default-database-strategy.inline.sharding-column= # 影子库分片规则
spring.shardingsphere.shadow.shardingRule.default-database-strategy.inline.algorithm-expression= # 影子库分片表达式

spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.actual-data-nodes= # 影子库真实节点
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.table-strategy.inline.sharding-column= # 影子库表中分片列 
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.table-strategy.inline.algorithm-expression= # 影子库分表表达式
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.type= # 分布式id算法
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.column= # 分布式id对应的列
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.props.worker.id= # 起始id

spring.shardingsphere.shadow.shardingRule.binding-tables= # 绑定表
spring.shardingsphere.shadow.shardingRule.broadcast-tables= # 广播表

# 加解密器
spring.shardingsphere.shadow.shardingRule.encryptRule.encryptors.<encryptor-name>.type= # 加解密器类型
spring.shardingsphere.shadow.shardingRule.encryptRule.encryptors.<encryptor-name>.props.<property-name>= # 属性配置

# 加解密规则
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.cipherColumn= # 密文列
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.encryptor= # 加解密器名称
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.plainColumn= # 明文列

```

## 4.0.0-RC2

### 支持指定明文列与密文列，支持JNDI

#### 配置项说明

```properties
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.type= # 加解密器类型，可以自定义或选择内置类型：MD5/AES
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.props.<property-name>= # 属性配置
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.cipherColumn= # 存储密文的字段
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.plainColumn= # 存储明文的字段
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.encryptor= # 加密器名称
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.assistedQueryColumn= # 辅助查询字段

spring.shardingsphere.props.query.with.cipher.comlum= # 是否使用加密列

spring.shardingsphere.datasource.<datasource-name>.jndi-name= # jndi名称

spring.shardingsphere.masterslave.load-balance-algorithm-type= # 负载均衡算法

```

## 4.0.0-RC1

### 修改配置项为spring前缀，增加数据脱敏

#### 配置项说明

```properties
spring.shardingsphere.datasource.names= # 数据源基本配置

spring.shardingsphere.datasource.<data-source-name>.type=# 数据库类型
spring.shardingsphere.datasource.<data-source-name>.driver-class-name=# 数据库驱动程序
spring.shardingsphere.datasource.<data-source-name>.url= # 数据库链接地址
spring.shardingsphere.datasource.<data-source-name>.username= # 数据库用户名称
spring.shardingsphere.datasource.<data-source-name>.password= # 数据库密码
spring.shardingsphere.datasource.<data-source-name>.max-total= # 最大连接数

# 主从配置
spring.shardingsphere.masterslave.name= # 主库配置
spring.shardingsphere.masterslave.master-data-source-name= # 主库名称
spring.shardingsphere.masterslave.slave-data-source-names= # 从库名称

spring.shardingsphere.orchestration.name= # 数据治理实例名称
spring.shardingsphere.orchestration.overwrite= # 本地配置是否覆盖注册中心配置
spring.shardingsphere.orchestration.registry.type= # 注册中心类型
spring.shardingsphere.orchestration.registry.namespace= # 注册中心命名空间
spring.shardingsphere.orchestration.registry.server-lists= # 连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:3181,host2:3181

spring.shardingsphere.enabled= # 是否开启分片

spring.shardingsphere.sharding.default-data-source-name= # 默认数据源名称
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column= # 分片列
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression= # 分片表达式

spring.shardingsphere.sharding.tables.<logic-table-name>.actual-data-nodes= # 真实节点
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.inline.sharding-column= # 表的分片列
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.inline.algorithm-expression= # 分表表达式
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.type= # 分布式id算法
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.column= # 分布式id对应的列

spring.shardingsphere.sharding.binding-tables= # 绑定的表，多个表使用逗号隔开
spring.shardingsphere.sharding.broadcast-tables= # 广播表

spring.shardingsphere.props.sql.show= # 打印sql
spring.shardingsphere.props.executor.size= # 最大工作线程数

# 加密器
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.type= # 加解密器类型
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.qualifiedColumns= # 加密表的字段
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.props.<property-name>= # 属性配置

# 加密规则
spring.shardingsphere.sharding.encryptRule.encryptors.<encryptor-name>.qualifiedColumns= # 分片加密表的字段
spring.shardingsphere.sharding.encryptRule.encryptors.<encryptor-name>.type= # 分片加密类型
spring.shardingsphere.sharding.encryptRule.encryptors.<encryptor-name>.props.<property-name>= # 属性配置
```

## 3.0.0

### 注册中心支持通用配置

#### 配置项说明

```properties
sharding.jdbc.config.orchestration.name= # 编排治理名称
sharding.jdbc.config.orchestration.overwrite= # 本地配置覆盖注册中心配置
sharding.jdbc.config.orchestration.registry.namespace= # 注册中心命名空间
sharding.jdbc.config.orchestration.registry.server-lists= # 注册中心服务器地址，多个使用逗号隔开
```

## 3.0.0.M1 (Not Apache Release)

### 支持 spring Boot Starter 2.X，适配etcd注册中心

#### 配置项说明

```properties

# etcd 注册中心
sharding.jdbc.config.orchestration.etcd.max-retries= # 最大重试次数
sharding.jdbc.config.orchestration.etcd.retry-interval-milliseconds= # 重试时间间隔
sharding.jdbc.config.orchestration.etcd.server-lists= # 连接etcd服务器列表，多个使用逗号隔开
sharding.jdbc.config.orchestration.etcd.time-to-live-seconds= # 临时节点存活时间
sharding.jdbc.config.orchestration.etcd.timeout-milliseconds= # 每次请求的的超时时间

# zookeeper注册中心
sharding.jdbc.config.orchestration.zookeeper.base-sleep-time-milliseconds= # 等待重试的间隔时间的初始值
sharding.jdbc.config.orchestration.zookeeper.connection-timeout-milliseconds= # 连接超时时间
sharding.jdbc.config.orchestration.zookeeper.digest= # 连接zookeeper的权限令牌
sharding.jdbc.config.orchestration.zookeeper.max-retries= # 最大重试次数
sharding.jdbc.config.orchestration.zookeeper.max-sleep-time-milliseconds= # 等待重试的间隔时间的最大值
sharding.jdbc.config.orchestration.zookeeper.namespace= # zookeeper命名空间
sharding.jdbc.config.orchestration.zookeeper.server-lists= # 连接zookeeper的服务器列表
sharding.jdbc.config.orchestration.zookeeper.session-timeout-milliseconds= # 会话超时时间
```

## 2.0.2 (Not Apache Release)

### 增加分片策略

#### 配置项说明

```properties

# 复合分片策略
sharding.jdbc.config.sharding.default-database-strategy.complex.algorithm-class-name= # 复合分片算法
sharding.jdbc.config.sharding.default-database-strategy.complex.sharding-columns= # 复合分片列名，多个使用多个隔开

# 强制路由策略
sharding.jdbc.config.sharding.default-database-strategy.hint.algorithm-class-name # 路由策略类

# inline行表达式分片策略
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression= # inline行表达式
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column= # 行表达式分片策略分片列

# 无分片策略
sharding.jdbc.config.sharding.default-database-strategy.none= # 无分片策略

# 标准分片策略
sharding.jdbc.config.sharding.default-database-strategy.standard.precise-algorithm-class-name= # 精准分片策略类
sharding.jdbc.config.sharding.default-database-strategy.standard.range-algorithm-class-name= # 范围分片策略类
sharding.jdbc.config.sharding.default-database-strategy.standard.sharding-column= # 标准分片策略分片列

# 表分片策略
# 复合分片策略
sharding.jdbc.config.sharding.default-table-strategy.complex.algorithm-class-name= # 复合分片算法类
sharding.jdbc.config.sharding.default-table-strategy.complex.sharding-columns= # 复合分片列名，多个使用多个隔开

# inline行表达式分片策略
sharding.jdbc.config.sharding.default-table-strategy.inline.algorithm-expression= # inline行表达式
sharding.jdbc.config.sharding.default-table-strategy.inline.sharding-column= # 行表达式分片策略分片列

# 无策略
sharding.jdbc.config.sharding.default-table-strategy.none

# 标准分片策略
sharding.jdbc.config.sharding.default-table-strategy.standard.precise-algorithm-class-name= # inline行表达式
sharding.jdbc.config.sharding.default-table-strategy.standard.range-algorithm-class-name= # 范围分片策略类
sharding.jdbc.config.sharding.default-table-strategy.standard.sharding-column= # 标准分片策略分片列

# 强制路由策略
sharding.jdbc.config.sharding.default-table-strategy.hint.algorithm-class-name= # 路由策略类
```

## 2.0.0 (Not Apache Release)

### 增加注册中心

#### 配置项说明

```properties

sharding.jdbc.config.orchestration.zookeeper.namespace= # zookeeper注册中心命名空间
sharding.jdbc.config.orchestration.zookeeper.server-lists= # zookeeper连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:3181,host2:3181
```

## 2.0.0.M3 (Not Apache Release)

### 兼容'.'和'—'配置

#### 配置项说明

```properties
sharding.jdbc.config.masterslave.master-data-source-name= # 主数据源名称
sharding.jdbc.config.masterslave.slave-data-source-names= # 从节点数据源名称

sharding.jdbc.config.orchestration.registry-center.namespace= # 注册中心命名空间
sharding.jdbc.config.orchestration.registry-center.server-lists= # 连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:3181,host2:3181

sharding.jdbc.config.masterslave.config-map.key1= # 自定义配置
```

## 2.0.0.M2 (Not Apache Release)

### 支持服务编排治理

#### 配置项说明

```properties

sharding.jdbc.config.orchestration.name= # 数据治理实例名称
sharding.jdbc.config.orchestration.overwrite= # 本地配置覆盖注册中心配置
sharding.jdbc.config.orchestration.registryCenter.namespace= # 注册中心命名空间
sharding.jdbc.config.orchestration.registryCenter.server-lists= # 连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:3181,host2:3181
```

## 2.0.0.M1 (Not Apache Release)

### 支持sharding-jdbc-spring-boot-starter

#### 配置项说明

```properties
sharding.jdbc.datasource.names= # 数据源名称，多个使用逗号隔开
# 数据源基本配置
sharding.jdbc.datasource.<data-source-name>.type= # 数据库类型
sharding.jdbc.datasource.<data-source-name>.driver-class-name= # 数据库驱动文件
sharding.jdbc.datasource.<data-source-name>.url= # 数据库链接地址
sharding.jdbc.datasource.<data-source-name>.username= # 数据库用户名称
sharding.jdbc.datasource.<data-source-name>.password= # 数据库密码
sharding.jdbc.datasource.<data-source-name>.maxActive= # 最大连接数

sharding.jdbc.config.masterslave.name= # 主数据源名称
sharding.jdbc.config.masterslave.masterDataSourceName= # 主数据源名称
sharding.jdbc.config.masterslave.slaveDataSourceNames= # 从库数据源名称

# 配置默认数据源
sharding.jdbc.config.sharding.default-data-source-name= # 默认数据源
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column= # 分库字段
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression= # 分库逻辑

# 数据库中相关数据表表的配置
sharding.jdbc.config.sharding.tables.<table-name>.actualDataNodes= # 相关表在数据库的具体位置
sharding.jdbc.config.sharding.tables.<table-name>.tableStrategy.inline.shardingColumn= # 相关数据表的切分字段
sharding.jdbc.config.sharding.tables.<table-name>.tableStrategy.inline.algorithmInlineExpression= # 相关数据表的切分策略
sharding.jdbc.config.sharding.tables.<table-name>.keyGeneratorColumnName= # id自动生成的列

sharding.jdbc.config.sharding.props.sql.show= # 打印sql
sharding.jdbc.config.sharding.props.executor.size= # 工作线程数
```
