+++
toc = true
title = "Yaml配置"
weight = 2
+++

## 配置示例

### 数据分片

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds${0..1}.t_order${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
  
props:
  sql.show: true
```

### 读写分离

```yaml
dataSources:
  ds_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
  ds_slave0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave0
    username: root
    password: 
  ds_slave1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave0
    - ds_slave1

props:
    sql.show: true
```

### 数据分片 + 读写分离

```yaml
dataSources:
  ds0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds0
    username: root
    password: 
  ds0_slave0: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds0_slave0
      username: root
      password: 
  ds0_slave1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds0_slave1
      username: root
      password: 
  ds1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds1
    username: root
    password: 
  ds1_slave0: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds1_slave0
        username: root
        password: 
  ds1_slave1: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds1_slave1
        username: root
        password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ms_ds${0..1}.t_order${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        Column: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds_0
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ms_ds${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
  
  masterSlaveRules:
      ms_ds0:
        masterDataSourceName: ds0
        slaveDataSourceNames:
          - ds0_slave0
          - ds0_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
      ms_ds1:
        masterDataSourceName: ds1
        slaveDataSourceNames: 
          - ds1_slave0
          - ds1_slave1
        loadBalanceAlgorithmType: ROUND_ROBIN
props:
  sql.show: true
```

### 数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  overwrite: true
  registry:
    namespace: orchestration
    serverLists: localhost:2181
```

## 配置项说明

### config-xxx.yaml 数据分片+读写分离(根据3.1最新版本修订)

```yaml
# 以下配置截止版本为3.1
# 配置文件中,必须配置的项目为schemaName,dataSources,并且sharidngRule,masterSlaveRule,配置其中一个(注意,除非server.yaml中定义了Orchestration,否则必须至少有一个config-xxxx配置文件),除此之外的其他项目为可选项
schemaName: test # schema名称,每个文件都是单独的schema,多个schema则是多个yaml文件,yaml文件命名要求是config-xxxx.yaml格式,虽然没有强制要求,但推荐名称中的xxxx与配置的schemaName保持一致,方便维护

dataSources: # 配置数据源列表,必须是有效的jdbc配置,目前仅支持MySQL与PostgreSQL,另外通过一些未公开(代码中可查,但可能会在未来有变化)的变量,可以配置来兼容其他支持JDBC的数据库,但由于没有足够的测试支持,可能会有严重的兼容性问题,配置时候要求至少有一个
  master_ds_0: # 数据源名称,可以是合法的字符串,目前的校验规则中,没有强制性要求,只要是合法的yaml字符串即可,但如果要用于分库分表配置,则需要有有意义的标志(在分库分表配置中详述),以下为目前公开的合法配置项目,不包含内部配置参数
    # 以下参数为必备参数
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_slave_1?serverTimezone=UTC&useSSL=false # 这里的要求合法的jdbc连接串即可,目前尚未兼容MySQL 8.x,需要在maven编译时候,升级MySQL JDBC版本到5.1.46或者47版本(不建议升级到JDBC的8.x系列版本,需要修改源代码,并且无法通过很多测试case)
    username: root # MySQL用户名
    password: password # MySQL用户的明文密码
    # 以下参数为可选参数,给出示例为默认配置,主要用于连接池控制
    connectionTimeoutMilliseconds: 30000 #连接超时控制
    idleTimeoutMilliseconds: 60000 # 连接空闲时间设置
    maxLifetimeMilliseconds: 0 # 连接的最大持有时间,0为无限制
    maxPoolSize: 50 # 连接池中最大维持的连接数量
    minPoolSize: 1 # 连接池的最小连接数量
    maintenanceIntervalMilliseconds: 30000 # 连接维护的时间间隔 atomikos框架需求
  # 以下配置的假设是,3307是3306的从库,3309,3310是3308的从库
  slave_ds_0:
    url: jdbc:mysql://127.0.0.1:3307/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  master_ds_1:
    url: jdbc:mysql://127.0.0.1:3308/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  slave_ds_1:
    url: jdbc:mysql://127.0.0.1:3309/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
  slave_ds_1_slave2:
    url: jdbc:mysql://127.0.0.1:3310/demo_ds_slave_1?serverTimezone=UTC&useSSL=false
    username: root
    password: password
masterSlaveRule: # 这里配置这个规则的话,相当于是全局读写分离配置
  name: ds_rw # 名称,合法的字符串即可,但如果涉及到在读写分离的基础上设置分库分表,则名称需要有意义才可以,另外,虽然目前没有强制要求,但主从库配置需要配置在实际关联的主从库上,如果配置的数据源之间主从是断开的状态,那么可能会发生写入的数据对于只读会话无法读取到的问题
  # 如果一个会话发生了写入并且没有提交(显式打开事务),sharidng sphere在后续的路由中,select都会在主库执行,直到会话提交
  masterDataSourceName: master_ds_0 # 主库的DataSource名称
  slaveDataSourceNames: # 从库的DataSource列表,至少需要有一个
    - slave_ds_0
  loadBalanceAlgorithmClassName: org.apache.shardingsphere.api.algorithm.masterslave # MasterSlaveLoadBalanceAlgorithm接口的实现类,允许自定义实现 默认提供两个,配置路径为org.apache.shardingsphere.api.algorithm.masterslave下的RandomMasterSlaveLoadBalanceAlgorithm(随机Random)与RoundRobinMasterSlaveLoadBalanceAlgorithm(轮询:次数%从库数量)
  loadBalanceAlgorithmType: #从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若loadBalanceAlgorithmClassName存在则忽略该配置,默认为ROUND_ROBIN

shardingRule: # sharding的配置
  # 配置主要分两类,一类是对整个sharding规则所有表生效的默认配置,一个是sharing具体某张表时候的配置
  # 首先说默认配置
  masterSlaveRules: # 在shardingRule中也可以配置shardingRule,对分片生效,具体内容与全局masterSlaveRule一致,但语法为:
    master_test_0:
      masterDataSourceName: master_ds_0
      slaveDataSourceNames:
        - slave_ds_0
    master_test_1:
      masterDataSourceName: master_ds_1
      slaveDataSourceNames:
        - slave_ds_1
        - slave_ds_1_slave2
  defaultDataSourceName: master_test_0 # 这里的数据源允许是dataSources的配置项目或者masterSlaveRules配置的名称,配置为masterSlaveRule的话相当于就是配置读写分离了
  broadcastTables: # 广播表 这里配置的表列表,对于发生的所有数据变更,都会不经sharidng处理,而是直接发送到所有数据节点,注意此处为列表,每个项目为一个表名称
    - broad_1
    - broad_2
  bindingTables: # 绑定表,也就是实际上哪些配置的sharidng表规则需要实际生效的列表,配置为yaml列表,并且允许单个条目中以逗号切割,所配置表必须已经配置为逻辑表
    - sharding_t1
    - sharding_t2,sharding_t3
  defaultDatabaseShardingStrategy: # 默认库级别sharidng规则,对应代码中ShardingStrategy接口的实现类,目前支持none,inline,hint,complex,standard五种配置 注意此处默认配置仅可以配置五个中的一个
  # 规则配置同样适合表sharding配置,同样是在这些算法中选择
    none: # 不配置任何规则,SQL会被发给所有节点去执行,这个规则没有子项目可以配置
    inline: # 行表达式分片
      shardingColumn: test_id # 分片列名称，目前只支持单列分片，多字段使用 complex方式
      algorithmExpression: master_test_${test_id % 2} # 分片表达式,根据指定的表达式计算得到需要路由到的数据源名称 需要是合法的groovy表达式,示例配置中,取余为0则语句路由到master_test_0,取余为1则路由到master_test_1
    hint: #基于标记的sharding分片
      shardingAlgorithm: # 需要是HintShardingAlgorithm接口的实现,目前代码中,仅有为测试目的实现的OrderDatabaseHintShardingAlgorithm,没有生产环境可用的实现
    complex: # 支持多列的shariding,目前无生产可用实现
      shardingColumns: # 逗号切割的列
      algorithmClassName: # ComplexKeysShardingAlgorithm接口的实现类，使用全路径类名
    standard: # 单列sharidng算法,需要配合对应的preciseShardingAlgorithm,rangeShardingAlgorithm接口的实现使用,目前无生产可用实现
      shardingColumn: # 列名,允许单列
      preciseAlgorithmClassName: # preciseShardingAlgorithm接口的实现类，用于 `=` and `IN` 情况下的分表算法
      rangeAlgorithmClassName: # rangeShardingAlgorithm接口的实现类，用于 `BETWEEN` 情况下的分表算法
  defaultTableStrategy: #配置参考defaultDatabaseShardingStrategy,区别在于,inline算法的配置中,algorithmExpression的配置算法结果需要是实际的物理表名称,而非数据源名称
  defaultKeyGenerator: #默认的主键生成算法 如果没有设置,默认为SNOWFLAKE算法
    column: # 自增键对应的列名称
    type: #自增键的类型,主要用于调用内置的主键生成算法有三个可用值:SNOWFLAKE(时间戳+worker id+自增id),UUID(java.util.UUID类生成的随机UUID),LEAF,其中Snowflake算法与UUID算法已经实现,LEAF目前(2018-01-14)尚未实现
    props:
      # 定制算法需要设置的参数,比如SNOWFLAKE算法的worker.id与max.tolerate.time.difference.milliseconds
  tables: #配置表sharding的主要位置
    sharding_t1:
      actualDataNodes: master_test_${0..1}.t_order${0..1} # sharidng 表对应的数据源以及物理名称,需要用表达式处理,表示表实际上在哪些数据源存在,配置示例中,意思是总共存在4个分片master_test_0.t_order0,master_test_0.t_order1,master_test_1.t_order0,master_test_1.t_order1
      # 需要注意的是,必须保证设置databaseStrategy可以路由到唯一的dataSource,tableStrategy可以路由到dataSource中唯一的物理表上,否则可能导致错误:一个insert语句被插入到多个实际物理表中
      databaseStrategy: # 局部设置会覆盖全局设置,参考defaultDatabaseShardingStrategy
      tableStrategy: # 局部设置会覆盖全局设置,参考defaultTableStrategy
      keyGenerator: # 局部设置会覆盖全局设置,参考defaultKeyGenerator
      logicIndex: # 逻辑索引名称 由于Oracle,PG这种数据库中,索引与表共用命名空间,如果接受到drop index语句,执行之前,会通过这个名称配置的确定对应的实际物理表名称
props:
  sql.show: #是否开启SQL显示，默认值: false
  acceptor.size: # accept连接的线程数量,默认为cpu核数2倍
  executor.size: #工作线程数量最大，默认值: 无限制
  max.connections.size.per.query: # 每个查询可以打开的最大连接数量,默认为1
  proxy.frontend.flush.threshold: # proxy的服务时候,对于单个大查询,每多少个网络包返回一次
  check.table.metadata.enabled: #是否在启动时检查分表元数据一致性，默认值: false
  proxy.transaction.type: # 默认LOCAL,proxy的事务模型 允许LOCAL,XA,BASE三个值 LOCAL无分布式事务,XA则是采用atomikos实现的分布式事务 BASE目前尚未实现
  proxy.opentracing.enabled: # 是否启用opentracing
  proxy.backend.use.nio: # 是否采用netty的NIO机制连接后端数据库,默认False ,使用epoll机制
  proxy.backend.max.connections: # 使用NIO而非epoll的话,proxy后台连接每个netty客户端允许的最大连接数量(注意不是数据库连接限制) 默认为8
  proxy.backend.connection.timeout.seconds: #使用nio而非epoll的话,proxy后台连接的超时时间,默认60s
  check.table.metadata.enabled: # 是否在启动时候,检查sharing的表的实际元数据是否一致,默认False
  
### 读写分离

```yaml
dataSources: #省略数据源配置，与数据分片一致

masterSlaveRule:
  name: #读写分离数据源名称
  masterDataSourceName: #主库数据源名称
  slaveDataSourceNames: #从库数据源名称列表
    - <data_source_name1>
    - <data_source_name2>
    - <data_source_name_x>
  loadBalanceAlgorithmClassName: #从库负载均衡算法类名称。该类需实现MasterSlaveLoadBalanceAlgorithm接口且提供无参数构造器
  loadBalanceAlgorithmType: #从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`loadBalanceAlgorithmClassName`存在则忽略该配置
    
props: #属性配置
  sql.show: #是否开启SQL显示，默认值: false
  executor.size: #工作线程数量，默认值: CPU核数
  check.table.metadata.enabled: #是否在启动时检查分表元数据一致性，默认值: false
  
### 数据治理

```yaml
dataSources: #省略数据源配置
shardingRule: #省略分片规则配置
masterSlaveRule: #省略读写分离规则配置

orchestration:
  name: #数据治理实例名称
  overwrite: #本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  registry: #注册中心配置
    serverLists: #连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: #注册中心的命名空间
    digest: #连接注册中心的权限令牌。缺省为不需要权限验证
    operationTimeoutMilliseconds: #操作超时的毫秒数，默认500毫秒
    maxRetries: #连接失败后的最大重试次数，默认3次
    retryIntervalMilliseconds: #重试间隔毫秒数，默认500毫秒
    timeToLiveSeconds: #临时节点存活秒数，默认60秒
```

## Yaml语法说明

`!!` 表示实例化该类

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
