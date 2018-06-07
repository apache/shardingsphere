+++
toc = true
title = "Yaml配置"
weight = 2
+++

## 配置示例

### 数据分片

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds_${user_id % 2}
  
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
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
  ds_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_0
    username: root
    password: 
  ds_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave_0
    - ds_slave_1
```

### 数据分片 + 读写分离

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_0_slave_0: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds_0_slave_0
      username: root
      password: 
  ds_0_slave_1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds_0_slave_1
      username: root
      password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 
  ds_1_slave_0: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds_1_slave_0
        username: root
        password: 
  ds_1_slave_1: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds_1_slave_1
        username: root
        password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds_${user_id % 2}
  
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
  masterSlaveRules:
      ms_ds_0:
        masterDataSourceName: ds_0
        slaveDataSourceNames:
          - ds_0_slave_0
          - ds_0_slave_1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key0: master-slave-value0
      ms_ds_1:
        masterDataSourceName: ds_1
        slaveDataSourceNames: 
          - ds_1_slave_0
          - ds_1_slave_1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key1: master-slave-value1

  props:
    sql.show: true
```

### 使用Zookeeper的数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  type: SHARDING
  overwrite: true
  zookeeper:
    namespace: orchestration
    serverLists: localhost:2181
```

### 使用Etcd的数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  type: SHARDING
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

## 配置项说明

### 数据分片

```yaml
dataSources: #数据源配置，可配置多个data_source_name
  <data_source_name>: #<!!数据库连接池实现类> `!!`表示实例化该类
    driverClassName: #数据库驱动类名
    url: #数据库url连接
    username: #数据库用户名
    password: #数据库密码
    # ... 数据库连接池的其它属性

shardingRule:
  tables: #数据分片规则配置，可配置多个logic_table_name
    <logic_table_name>: #逻辑表名称
      actualDataNodes: #由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况
        
      databaseStrategy: #分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一
        standard: #用于单分片键的标准分片场景
          shardingColumn: #分片列名称
          preciseAlgorithmClassName: #精确分片算法类名称，用于=和IN。。该类需实现PreciseShardingAlgorithm接口并提供无参数的构造器
          rangeAlgorithmClassName: #范围分片算法类名称，用于BETWEEN，可选。。该类需实现RangeShardingAlgorithm接口并提供无参数的构造器
        complex: #用于多分片键的复合分片场景
          shardingColumns: #分片列名称，多个列以逗号分隔
          algorithmClassName: #复合分片算法类名称。该类需实现ComplexKeysShardingAlgorithm接口并提供无参数的构造器
        inline: #行表达式分片策略
          shardingColumn: #分片列名称
          algorithmInlineExpression: #分片算法行表达式，需符合groovy语法
        hint: #Hint分片策略
          algorithmClassName: #Hint分片算法类名称。该类需实现HintShardingAlgorithm接口并提供无参数的构造器
        none: #不分片
      tableStrategy: #分表策略，同分库策略
        
      keyGeneratorColumnName: #自增列名称，缺省表示不使用自增主键生成器
      keyGeneratorClassName: #自增列值生成器类名称。该类需实现KeyGenerator接口并提供无参数的构造器
        
      logicIndex: #逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表
  bindingTables: #绑定表规则列表
  - <logic_table_name_1, logic_table_name_2, ...> 
  - <logic_table_name_3, logic_table_name_4, ...>
  - <logic_table_name_x, logic_table_name_y, ...>
  
  defaultDataSourceName: #未配置分片规则的表将通过默认数据源定位  
  defaultDatabaseStrategy: #默认数据库分片策略，同分库策略
  defaultTableStrategy: #默认表分片策略，同分库策略
  defaultKeyGeneratorClassName: #默认自增列值生成器类名称，缺省使用io.shardingsphere.core.keygen.DefaultKeyGenerator。该类需实现KeyGenerator接口并提供无参数的构造器
  
  masterSlaveRules: #读写分离规则，详见读写分离部分
    <data_source_name>: #数据源名称，需要与真实数据源匹配，可配置多个data_source_name
      masterDataSourceName: #详见读写分离部分
      slaveDataSourceNames: #详见读写分离部分
      loadBalanceAlgorithmClassName: #详见读写分离部分
      loadBalanceAlgorithmType: #详见读写分离部分
      configMap: #用户自定义配置
          key1: value1
          key2: value2
          keyx: valuex
  
  props: #属性配置
    sql.show: #是否开启SQL显示，默认值: false
    executor.size: #工作线程数量，默认值: CPU核数
    
  configMap: #用户自定义配置
    key1: value1
    key2: value2
    keyx: valuex
```

### 读写分离

```yaml
dataSources: #省略数据源配置，与数据分片一致

masterSlaveRule:
  name: #读写分离数据源名称
  masterDataSourceName: #主库数据源名称
  slaveDataSourceNames: #从库数据源名称列表
    - <data_source_name_1>
    - <data_source_name_2>
    - <data_source_name_x>
  loadBalanceAlgorithmClassName: #从库负载均衡算法类名称。该类需实现MasterSlaveLoadBalanceAlgorithm接口且提供无参数构造器
  loadBalanceAlgorithmType: #从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`loadBalanceAlgorithmClassName`存在则忽略该配置
  
  configMap: #用户自定义配置
    key1: value1
    key2: value2
    keyx: valuex
```

### 使用Zookeeper的数据治理

```yaml
dataSources: #省略数据源配置
shardingRule: #省略分片规则配置
masterSlaveRule: #省略读写分离规则配置

orchestration:
  name: #数据治理实例名称
  overwrite: #本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  type: #数据源类型，可选值：SHARDING，MASTER_SLAVE
  zookeeper: #Zookeeper注册中心配置
    serverLists: #连接Zookeeper服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: #Zookeeper的命名空间
    baseSleepTimeMilliseconds: #等待重试的间隔时间的初始毫秒数，默认1000毫秒
    maxSleepTimeMilliseconds: #等待重试的间隔时间的最大毫秒数，默认3000毫秒
    maxRetries: #连接失败后的最大重试次数，默认3次
    sessionTimeoutMilliseconds: #会话超时毫秒数，默认60000毫秒
    connectionTimeoutMilliseconds: #连接超时毫秒数，默认15000毫秒
    digest: #连接Zookeeper的权限令牌。缺省为不需要权限验证
```

### 使用Etcd的数据治理

```yaml
dataSources: #省略数据源配置
shardingRule: #省略分片规则配置
masterSlaveRule: #省略读写分离规则配置

orchestration:
  name: #同Zookeeper
  overwrite: #同Zookeeper
  type: #同Zookeeper
  etcd: #Etcd注册中心配置
    serverLists: #连接Etcd服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379
    timeToLiveSeconds: #临时节点存活秒数，默认60秒
    timeoutMilliseconds: #请求超时毫秒数，默认500毫秒
    retryIntervalMilliseconds: #重试间隔毫秒数，默认200毫秒
    maxRetries: #请求失败后的最大重试次数，默认3次
```

## 柔性事务

### 异步作业Yaml配置

```yaml
#目标数据库的数据源
targetDataSource:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password:
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password:

#事务日志的数据源
transactionLogDataSource:
  ds_trans: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/trans_log
    username: root
    password:

#注册中心配置
zkConfig:
  #注册中心的连接地址
  connectionString: localhost:2181
  
  #作业的命名空间
  namespace: Best-Efforts-Delivery-Job
  
  #注册中心的等待重试的间隔时间的初始值
  baseSleepTimeMilliseconds: 1000
  
  #注册中心的等待重试的间隔时间的最大值
  maxSleepTimeMilliseconds: 3000
  
  #注册中心的最大重试次数
  maxRetries: 3

#作业配置
jobConfig:
  #作业名称
  name: bestEffortsDeliveryJob
  
  #触发作业的cron表达式
  cron: 0/5 * * * * ?
  
  #每次作业获取的事务日志最大数量
  transactionLogFetchDataCount: 100
  
  #事务送达的最大尝试次数
  maxDeliveryTryTimes: 3
  
  #执行送达事务的延迟毫秒数,早于此间隔时间的入库事务才会被作业执行
  maxDeliveryTryDelayMillis: 60000
```

## Yaml语法说明

`!!` 表示实例化该类

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
