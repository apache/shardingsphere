+++
title = "YAML 配置"
weight = 1
+++

## 5.0.0-alpha

### 数据分片

#### 配置项说明

```yaml
dataSources: # 省略数据源配置，请参考使用手册

rules:
- !SHARDING
  tables: # 数据分片规则配置
    <logic-table-name> (+): # 逻辑表名称
      actualDataNodes (?): # 由数据源名 + 表名组成（参考Inline语法规则）
      databaseStrategy (?): # 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一
        standard: # 用于单分片键的标准分片场景
          shardingColumn: # 分片列名称
          shardingAlgorithmName: # 分片算法名称
        complex: # 用于多分片键的复合分片场景
          shardingColumns: #分片列名称，多个列以逗号分隔
          shardingAlgorithmName: # 分片算法名称
        hint: # Hint 分片策略
          shardingAlgorithmName: # 分片算法名称
        none: # 不分片
      tableStrategy: # 分表策略，同分库策略
      keyGenerateStrategy: # 分布式序列策略
        column: # 自增列名称，缺省表示不使用自增主键生成器
        keyGeneratorName: # 分布式序列算法名称
  autoTables: # 自动分片表规则配置
    t_order_auto: # 逻辑表名称
      actualDataSources (?): # 数据源名称
      shardingStrategy: # 切分策略
        standard: # 用于单分片键的标准分片场景
          shardingColumn: # 分片列名称
          shardingAlgorithmName: # 自动分片算法名称
  bindingTables (+): # 绑定表规则列表
    - <logic_table_name_1, logic_table_name_2, ...> 
    - <logic_table_name_1, logic_table_name_2, ...> 
  broadcastTables (+): # 广播表规则列表
    - <table-name>
    - <table-name>
  defaultDatabaseStrategy: # 默认数据库分片策略
  defaultTableStrategy: # 默认表分片策略
  defaultKeyGenerateStrategy: # 默认的分布式序列策略
  
  # 分片算法配置
  shardingAlgorithms:
    <sharding-algorithm-name> (+): # 分片算法名称
      type: # 分片算法类型
      props: # 分片算法属性配置
      # ...
  
  # 分布式序列算法配置
  keyGenerators:
    <key-generate-algorithm-name> (+): # 分布式序列算法名称
      type: # 分布式序列算法类型
      props: # 分布式序列算法属性配置
      # ...

props:
  # ...
```

### 读写分离

#### 配置项说明

```yaml
dataSources: # 省略数据源配置，请参考使用手册

rules:
- !REPLICA_QUERY
  dataSources:
    <data-source-name> (+): # 读写分离逻辑数据源名称
      primaryDataSourceName: # 主库数据源名称
      replicaDataSourceNames: 
        - <replica-data_source-name> (+) # 从库数据源名称
      loadBalancerName: # 负载均衡算法名称
  
  # 负载均衡算法配置
  loadBalancers:
    <load-balancer-name> (+): # 负载均衡算法名称
      type: # 负载均衡算法类型
      props: # 负载均衡算法属性配置
        # ...

props:
  # ...
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance)。

### 数据加密

#### 配置项说明

```yaml
dataSource: # 省略数据源配置，请参考使用手册

rules:
- !ENCRYPT
  tables:
    <table-name> (+): # 加密表名称
      columns:
        <column-name> (+): # 加密列名称
          cipherColumn: # 密文列名称
          assistedQueryColumn (?):  # 查询辅助列名称
          plainColumn (?): # 原文列名称
          encryptorName: # 加密算法名称
  
  # 加密算法配置
  encryptors:
    <encrypt-algorithm-name> (+): # 加解密算法名称
      type: # 加解密算法类型
      props: # 加解密算法属性配置
        # ...

  queryWithCipherColumn: # 是否使用加密列进行查询。在有原文列的情况下，可以使用原文列进行查询
```

算法类型的详情，请参见[内置加密算法列表](/cn/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt)。

### 影子库

#### 配置项说明

```yaml
dataSources: # 省略数据源配置，请参考使用手册

rules:
- !SHADOW
  column: # 影子字段名
  sourceDataSourceNames: # 影子前数据库名
     # ...
  shadowDataSourceNames: # 对应的影子库名
     # ... 

props:
  # ...
```

### 分布式治理

#### 配置项说明

```yaml
governance:
  name: # 治理名称
  registryCenter: # 注册中心
    type: # 治理持久化类型。如：Zookeeper, etcd
    serverLists: # 治理服务列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 
  overwrite: # 本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
```

## ShardingSphere-4.x

### 数据分片

#### 配置项说明

```yaml
dataSources: # 数据源配置，可配置多个 data_source_name
  <data_source_name>: # <!!数据库连接池实现类> `!!`表示实例化该类
    driverClassName: # 数据库驱动类名
    url: # 数据库 url 连接
    username: # 数据库用户名
    password: # 数据库密码
    # ... 数据库连接池的其它属性

shardingRule:
  tables: # 数据分片规则配置，可配置多个 logic_table_name
    <logic_table_name>: # 逻辑表名称
      actualDataNodes: # 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持 inline 表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况

      databaseStrategy: # 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一
        standard: # 用于单分片键的标准分片场景
          shardingColumn: # 分片列名称
          preciseAlgorithmClassName: # 精确分片算法类名称，用于 = 和 IN。。该类需实现 PreciseShardingAlgorithm 接口并提供无参数的构造器
          rangeAlgorithmClassName: # 范围分片算法类名称，用于 BETWEEN，可选。。该类需实现 RangeShardingAlgorithm 接口并提供无参数的构造器
        complex: # 用于多分片键的复合分片场景
          shardingColumns: # 分片列名称，多个列以逗号分隔
          algorithmClassName: # 复合分片算法类名称。该类需实现 ComplexKeysShardingAlgorithm 接口并提供无参数的构造器
        inline: # 行表达式分片策略
          shardingColumn: # 分片列名称
          algorithmInlineExpression: # 分片算法行表达式，需符合 groovy 语法
        hint: # Hint 分片策略
          algorithmClassName: # Hint 分片算法类名称。该类需实现 HintShardingAlgorithm 接口并提供无参数的构造器
        none: # 不分片
      tableStrategy: # 分表策略，同分库策略
      keyGenerator:
        column: # 自增列名称，缺省表示不使用自增主键生成器
        type: # 自增列值生成器类型，缺省表示使用默认自增列值生成器。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID
        props: # 属性配置, 注意：使用 SNOWFLAKE 算法，需要配置 worker.id 与 max.tolerate.time.difference.milliseconds 属性。若使用此算法生成值作分片值，建议配置 max.vibration.offset 属性
          <property-name>: # 属性名称

  bindingTables: # 绑定表规则列表
    - <logic_table_name1, logic_table_name2, ...>
    - <logic_table_name3, logic_table_name4, ...>
    - <logic_table_name_x, logic_table_name_y, ...>
  broadcastTables: # 广播表规则列表
    - table_name1
    - table_name2
    - table_name_x

  defaultDataSourceName: # 未配置分片规则的表将通过默认数据源定位  
  defaultDatabaseStrategy: # 默认数据库分片策略，同分库策略
  defaultTableStrategy: # 默认表分片策略，同分库策略
  defaultKeyGenerator: # 默认的主键生成算法 如果没有设置,默认为 SNOWFLAKE 算法
    type: # 默认自增列值生成器类型，缺省将使用 org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID
    props:
      <property-name>: # 自增列值生成器属性配置, 比如 SNOWFLAKE 算法的 worker.id 与 max.tolerate.time.difference.milliseconds

  masterSlaveRules: # 读写分离规则，详见读写分离部分
    <data_source_name>: # 数据源名称，需要与真实数据源匹配，可配置多个 data_source_name
      masterDataSourceName: # 详见读写分离部分
      slaveDataSourceNames: # 详见读写分离部分
      loadBalanceAlgorithmType: # 详见读写分离部分
      props: # 读写分离负载算法的属性配置
        <property-name>: # 属性值

props: # 属性配置
  sql.show: # 是否开启 SQL 显示，默认值: false
  executor.size: # 工作线程数量，默认值: CPU 核数
  max.connections.size.per.query: # 每个查询可以打开的最大连接数量,默认为 1
  check.table.metadata.enabled: # 是否在启动时检查分表元数据一致性，默认值: false
```

### 读写分离

#### 配置项说明

```yaml
dataSources: # 省略数据源配置，与数据分片一致

masterSlaveRule:
  name: # 读写分离数据源名称
  masterDataSourceName: # 主库数据源名称
  slaveDataSourceNames: # 从库数据源名称列表
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmType: # 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若 `loadBalanceAlgorithmClassName` 存在则忽略该配置
  props: # 读写分离负载算法的属性配置
    <property-name>: # 属性值
```

通过 `YamlMasterSlaveDataSourceFactory` 工厂类创建 `DataSource`：

```java
DataSource dataSource = YamlMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### 数据脱敏

#### 配置项说明

```yaml
dataSource: # 省略数据源配置

encryptRule:
  encryptors:
    <encryptor-name>:
      type: # 加解密器类型，可自定义或选择内置类型：MD5/AES 
      props: # 属性配置, 注意：使用 AES 加密器，需要配置 AES 加密器的 KEY 属性：aes.key.value
        aes.key.value:
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: # 存储明文的字段
          cipherColumn: # 存储密文的字段
          assistedQueryColumn: # 辅助查询字段，针对 ShardingQueryAssistedEncryptor 类型的加解密器进行辅助查询
          encryptor: # 加密器名字
```

### 治理

#### 配置项说明

```yaml
dataSources: # 省略数据源配置
shardingRule: # 省略分片规则配置
masterSlaveRule: # 省略读写分离规则配置
encryptRule: # 省略数据脱敏规则配置

orchestration:
  name: # 治理实例名称
  overwrite: # 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  registry: # 注册中心配置
    type: # 配置中心类型。如：zookeeper
    serverLists: # 连接注册中心服务器的列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: # 注册中心的命名空间
    digest: # 连接注册中心的权限令牌。缺省为不需要权限验证
    operationTimeoutMilliseconds: # 操作超时的毫秒数，默认 500 毫秒
    maxRetries: # 连接失败后的最大重试次数，默认 3 次
    retryIntervalMilliseconds: # 重试间隔毫秒数，默认 500 毫秒
    timeToLiveSeconds: # 临时节点存活秒数，默认 60 秒
```

## ShardingSphere-3.x

### 数据分片

#### 配置项说明

```yaml
# 以下配置截止版本为3.1
# 配置文件中,必须配置的项目为 schemaName,dataSources,并且 shardingRule,masterSlaveRule,配置其中一个(注意,除非 server.yaml 中定义了 Orchestration,否则必须至少有一个 config-xxxx 配置文件),除此之外的其他项目为可选项
schemaName: test # schema 名称,每个文件都是单独的 schema,多个 schema 则是多个 yaml 文件,yaml 文件命名要求是 config-xxxx.yaml 格式,虽然没有强制要求,但推荐名称中的 xxxx 与配置的 schemaName 保持一致,方便维护

dataSources: # 配置数据源列表,必须是有效的 jdbc 配置,目前仅支持 MySQL 与 PostgreSQL,另外通过一些未公开(代码中可查,但可能会在未来有变化)的变量,可以配置来兼容其他支持 JDBC 的数据库,但由于没有足够的测试支持,可能会有严重的兼容性问题,配置时候要求至少有一个
  master_ds_0: # 数据源名称,可以是合法的字符串,目前的校验规则中,没有强制性要求,只要是合法的 yaml 字符串即可,但如果要用于分库分表配置,则需要有有意义的标志(在分库分表配置中详述),以下为目前公开的合法配置项目,不包含内部配置参数
    # 以下参数为必备参数
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_slave_1?serverTimezone=UTC&useSSL=false # 这里的要求合法的 jdbc 连接串即可,目前尚未兼容 MySQL 8.x,需要在 maven 编译时候,升级 MySQL JDBC 版本到 5.1.46 或者 47 版本(不建议升级到 JDBC 的 8.x 系列版本,需要修改源代码,并且无法通过很多测试 case)
    username: root # MySQL 用户名
    password: password # MySQL 用户的明文密码
    # 以下参数为可选参数,给出示例为默认配置,主要用于连接池控制
    connectionTimeoutMilliseconds: 30000 # 连接超时控制
    idleTimeoutMilliseconds: 60000 # 连接空闲时间设置
    maxLifetimeMilliseconds: 0 # 连接的最大持有时间,0 为无限制
    maxPoolSize: 50 # 连接池中最大维持的连接数量
    minPoolSize: 1 # 连接池的最小连接数量
    maintenanceIntervalMilliseconds: 30000 # 连接维护的时间间隔 atomikos 框架需求
  # 以下配置的假设是,3307 是 3306 的从库,3309,3310 是 3308 的从库
  slave_ds_0:
    url: jdbc:mysql://127.0.0.1:3307/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  master_ds_1:
    url: jdbc:mysql://127.0.0.1:3308/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  slave_ds_1:
    url: jdbc:mysql://127.0.0.1:3309/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  slave_ds_1_slave2:
    url: jdbc:mysql://127.0.0.1:3310/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
masterSlaveRule: # 这里配置这个规则的话,相当于是全局读写分离配置
  name: ds_rw # 名称,合法的字符串即可,但如果涉及到在读写分离的基础上设置分库分表,则名称需要有意义才可以,另外,虽然目前没有强制要求,但主从库配置需要配置在实际关联的主从库上,如果配置的数据源之间主从是断开的状态,那么可能会发生写入的数据对于只读会话无法读取到的问题
  # 如果一个会话发生了写入并且没有提交(显式打开事务),sharding sphere 在后续的路由中,select 都会在主库执行,直到会话提交
  masterDataSourceName: master_ds_0 # 主库的 DataSource 名称
  slaveDataSourceNames: # 从库的 DataSource 列表,至少需要有一个
    - slave_ds_0
  loadBalanceAlgorithmClassName: io.shardingsphere.api.algorithm.masterslave # MasterSlaveLoadBalanceAlgorithm 接口的实现类,允许自定义实现 默认提供两个,配置路径为 io.shardingsphere.api.algorithm.masterslave 下的 RandomMasterSlaveLoadBalanceAlgorithm(随机 Random)与 RoundRobinMasterSlaveLoadBalanceAlgorithm(轮询:次数 % 从库数量)
  loadBalanceAlgorithmType: # 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若 loadBalanceAlgorithmClassName 存在则忽略该配置,默认为 ROUND_ROBIN

shardingRule: # sharding 的配置
  # 配置主要分两类,一类是对整个sharding规则所有表生效的默认配置,一个是 sharing 具体某张表时候的配置
  # 首先说默认配置
  masterSlaveRules: # 在 shardingRule 中也可以配置 shardingRule,对分片生效,具体内容与全局 masterSlaveRule 一致,但语法为:
    master_test_0:
      masterDataSourceName: master_ds_0
      slaveDataSourceNames:
        - slave_ds_0
    master_test_1:
      masterDataSourceName: master_ds_1
      slaveDataSourceNames:
        - slave_ds_1
        - slave_ds_1_slave2
  defaultDataSourceName: master_test_0 # 这里的数据源允许是 dataSources 的配置项目或者 masterSlaveRules 配置的名称,配置为 masterSlaveRule 的话相当于就是配置读写分离了
  broadcastTables: # 广播表 这里配置的表列表,对于发生的所有数据变更,都会不经 sharding 处理,而是直接发送到所有数据节点,注意此处为列表,每个项目为一个表名称
    - broad_1
    - broad_2
  bindingTables: # 绑定表,也就是实际上哪些配置的 sharding 表规则需要实际生效的列表,配置为 yaml 列表,并且允许单个条目中以逗号切割,所配置表必须已经配置为逻辑表
    - sharding_t1
    - sharding_t2,sharding_t3
  defaultDatabaseShardingStrategy: # 默认库级别 sharding 规则,对应代码中 ShardingStrategy 接口的实现类,目前支持 none,inline,hint,complex,standard 五种配置 注意此处默认配置仅可以配置五个中的一个
    # 规则配置同样适合表 sharding 配置,同样是在这些算法中选择
    none: # 不配置任何规则,SQL 会被发给所有节点去执行,这个规则没有子项目可以配置
    inline: # 行表达式分片
      shardingColumn: test_id # 分片列名称
      algorithmExpression: master_test_${test_id % 2} # 分片表达式,根据指定的表达式计算得到需要路由到的数据源名称 需要是合法的 groovy 表达式,示例配置中,取余为 0 则语句路由到 master_test_0,取余为 1 则路由到 master_test_1
    hint: # 基于标记的 sharding 分片
      shardingAlgorithm: # 需要是 HintShardingAlgorithm 接口的实现,目前代码中,仅有为测试目的实现的 OrderDatabaseHintShardingAlgorithm,没有生产环境可用的实现
    complex: # 支持多列的 sharding,目前无生产可用实现
      shardingColumns: # 逗号切割的列
      shardingAlgorithm: # ComplexKeysShardingAlgorithm 接口的实现类
    standard: # 单列 sharding 算法,需要配合对应的 preciseShardingAlgorithm,rangeShardingAlgorithm 接口的实现使用,目前无生产可用实现
      shardingColumn: # 列名,允许单列
      preciseShardingAlgorithm: # preciseShardingAlgorithm 接口的实现类
      rangeShardingAlgorithm: # rangeShardingAlgorithm 接口的实现类
  defaultTableStrategy: # 配置参考 defaultDatabaseShardingStrategy,区别在于,inline 算法的配置中,algorithmExpression 的配置算法结果需要是实际的物理表名称,而非数据源名称
  defaultKeyGenerator: # 默认的主键生成算法 如果没有设置,默认为 SNOWFLAKE 算法
    column: # 自增键对应的列名称
    type: # 自增键的类型,主要用于调用内置的主键生成算法有三个可用值:SNOWFLAKE(时间戳 +worker id+ 自增 id),UUID(java.util.UUID 类生成的随机 UUID),LEAF,其中 Snowflake 算法与 UUID 算法已经实现,LEAF 目前(2018-01-14)尚未实现
    className: # 非内置的其他实现了 KeyGenerator 接口的类,需要注意,如果设置这个,就不能设置 type,否则 type 的设置会覆盖 class 的设置
    props:
    # 定制算法需要设置的参数,比如 SNOWFLAKE 算法的 worker.id 与 max.tolerate.time.difference.milliseconds
  tables: # 配置表 sharding 的主要位置
    sharding_t1:
      actualDataNodes: master_test_${0..1}.t_order${0..1} # sharding 表对应的数据源以及物理名称,需要用表达式处理,表示表实际上在哪些数据源存在,配置示例中,意思是总共存在 4 个分片 master_test_0.t_order0,master_test_0.t_order1,master_test_1.t_order0,master_test_1.t_order1
      # 需要注意的是,必须保证设置 databaseStrategy 可以路由到唯一的 dataSource,tableStrategy 可以路由到 dataSource 中唯一的物理表上,否则可能导致错误:一个 insert 语句被插入到多个实际物理表中
      databaseStrategy: # 局部设置会覆盖全局设置,参考 defaultDatabaseShardingStrategy
      tableStrategy: # 局部设置会覆盖全局设置,参考 defaultTableStrategy
      keyGenerator: # 局部设置会覆盖全局设置,参考 defaultKeyGenerator
      logicIndex: # 逻辑索引名称 由于 Oracle,PG 这种数据库中,索引与表共用命名空间,如果接受到 drop index 语句,执行之前,会通过这个名称配置的确定对应的实际物理表名称
props:
  sql.show: # 是否开启 SQL 显示，默认值: false
  acceptor.size: # accept 连接的线程数量,默认为 cpu 核数 2 倍
  executor.size: # 工作线程数量最大，默认值: 无限制
  max.connections.size.per.query: # 每个查询可以打开的最大连接数量,默认为 1
  proxy.frontend.flush.threshold: # proxy 的服务时候,对于单个大查询,每多少个网络包返回一次
  check.table.metadata.enabled: # 是否在启动时检查分表元数据一致性，默认值: false
  proxy.transaction.type: # 默认 LOCAL,proxy 的事务模型 允许 LOCAL,XA,BASE 三个值 LOCAL 无分布式事务,XA 则是采用 atomikos 实现的分布式事务 BASE 目前尚未实现
  proxy.opentracing.enabled: # 是否启用 opentracing
  proxy.backend.use.nio: # 是否采用 netty 的 NIO 机制连接后端数据库,默认 False ,使用 epoll 机制
  proxy.backend.max.connections: # 使用 NIO 而非 epoll 的话,proxy 后台连接每个 netty 客户端允许的最大连接数量(注意不是数据库连接限制) 默认为 8
  proxy.backend.connection.timeout.seconds: # 使用 nio 而非 epoll 的话,proxy 后台连接的超时时间,默认 60s
  check.table.metadata.enabled: # 是否在启动时候,检查 sharing 的表的实际元数据是否一致,默认 False

configMap: # 用户自定义配置
  key1: value1
  key2: value2
  keyx: valuex
```

### 读写分离

#### 配置项说明

```yaml
dataSources: # 省略数据源配置，与数据分片一致

masterSlaveRule:
  name: # 读写分离数据源名称
  masterDataSourceName: # 主库数据源名称
  slaveDataSourceNames: # 从库数据源名称列表
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmClassName: # 从库负载均衡算法类名称。该类需实现 MasterSlaveLoadBalanceAlgorithm 接口且提供无参数构造器
  loadBalanceAlgorithmType: # 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若 `loadBalanceAlgorithmClassName` 存在则忽略该配置

props: # 属性配置
  sql.show: # 是否开启 SQL 显示，默认值: false
  executor.size: # 工作线程数量，默认值: CPU 核数
  check.table.metadata.enabled: # 是否在启动时检查分表元数据一致性，默认值: false

configMap: # 用户自定义配置
  key1: value1
  key2: value2
  keyx: valuex
```

通过 `MasterSlaveDataSourceFactory` 工厂类创建 `DataSource`：

```java
DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### 治理

#### 配置项说明

```yaml
dataSources: # 省略数据源配置
shardingRule: # 省略分片规则配置
masterSlaveRule: # 省略读写分离规则配置

orchestration:
  name: # 数据治理实例名称
  overwrite: # 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  registry: # 注册中心配置
    serverLists: # 连接注册中心服务器的列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: # 注册中心的命名空间
    digest: # 连接注册中心的权限令牌。缺省为不需要权限验证
    operationTimeoutMilliseconds: # 操作超时的毫秒数，默认 500 毫秒
    maxRetries: # 连接失败后的最大重试次数，默认 3 次
    retryIntervalMilliseconds: # 重试间隔毫秒数，默认 500 毫秒
    timeToLiveSeconds: # 临时节点存活秒数，默认 60 秒
```

## ShardingSphere-2.x

### 数据分片

#### 配置项说明

```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

shardingRule:
  tables:
    config:
      actualDataNodes: db${0..1}.t_config
    t_order: 
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
      keyGeneratorClass: io.shardingjdbc.core.yaml.fixture.IncrementKeyGenerator
    t_order_item:
      actualDataNodes: db${0..1}.t_order_item_${0..1}
      # 绑定表中其余的表的策略与第一张表的策略相同
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
  # 默认数据库分片策略
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true
```

### 读写分离

#### 概念

为了缓解数据库压力，将写入和读取操作分离为不同数据源，写库称为主库，读库称为从库，一主库可配置多从库。

#### 支持项

1. 提供了一主多从的读写分离配置，可独立使用，也可配合分库分表使用。
2. 独立使用读写分离支持 SQL 透传。
3. 同一线程且同一数据库连接内，如有写入操作，以后的读操作均从主库读取，用于保证数据一致性。
4. Spring 命名空间。
5. 基于 Hin t的强制主库路由。

#### 不支持范围

1. 主库和从库的数据同步。
2. 主库和从库的数据同步延迟导致的数据不一致。
3. 主库双写或多写。

#### 配置规则

```yaml
dataSources:
  db_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: org.h2.Driver
      url: jdbc:h2:mem:db_slave_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
      username: sa
      password: 
      maxActive: 100

masterSlaveRule:
  name: db_ms
  masterDataSourceName: db_master
  slaveDataSourceNames: [db_slave_0, db_slave_1]
  configMap:
    key1: value1
```

通过 `MasterSlaveDataSourceFactory` 工厂类创建 `DataSource`：

```java
DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### 治理

#### 配置项说明

Zookeeper 分库分表编排配置项说明

```yaml
dataSources: # 数据源配置

shardingRule: # 分片规则配置

orchestration: # Zookeeper 编排配置
  name: # 编排服务节点名称
  overwrite: # 本地配置是否可覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  zookeeper: # Zookeeper 注册中心配置
    namespace: # Zookeeper 的命名空间
    serverLists: # 连接 Zookeeper 服务器的列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    baseSleepTimeMilliseconds: # 等待重试的间隔时间的初始值。单位：毫秒
    maxSleepTimeMilliseconds: # 等待重试的间隔时间的最大值。单位：毫秒
    maxRetries: # 最大重试次数
    sessionTimeoutMilliseconds: # 会话超时时间。单位：毫秒
    connectionTimeoutMilliseconds: # 连接超时时间。单位：毫秒
    digest: # 连接 Zookeeper 的权限令牌。缺省为不需要权限验证
```

Etcd 分库分表编排配置项说明

```yaml
dataSources: # 数据源配置

shardingRule: # 分片规则配置

orchestration: # Etcd 编排配置
  name: # 编排服务节点名称
  overwrite: # 本地配置是否可覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  etcd: # Etcd 注册中心配置
    serverLists: # 连接 Etcd 服务器的列表。包括 IP 地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379
    timeToLiveSeconds: # 临时节点存活时间。单位：秒
    timeoutMilliseconds: # 每次请求的超时时间。单位：毫秒
    maxRetries: # 每次请求的最大重试次数
    retryIntervalMilliseconds: # 重试间隔时间。单位：毫秒
```

分库分表编排数据源构建方式

```java
DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

读写分离数据源构建方式

```java
DataSource dataSource = OrchestrationMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```
