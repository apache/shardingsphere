+++
toc = true
title = "YAML配置"
weight = 3
+++


## YAML配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 配置示例

#### 分库分表
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
      #绑定表中其余的表的策略与第一张表的策略相同
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
  #默认数据库分片策略
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true
```

##### 分库分表配置项说明

```yaml
dataSources: 数据源配置
  <data_source_name> 可配置多个: !!数据库连接池实现类
    driverClassName: 数据库驱动类名
    url: 数据库url连接
    username: 数据库用户名
    password: 数据库密码
    ... 数据库连接池的其它属性

defaultDataSourceName: 默认数据源，未配置分片规则的表将通过默认数据源定位

tables: 分库分表配置，可配置多个logic_table_name
    <logic_table_name>: 逻辑表名
        actualDataNodes: 真实数据节点，由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。不填写表示将为现有已知的数据源 + 逻辑表名称生成真实数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况。
        databaseStrategy: 分库策略，以下的分片策略只能任选其一
            standard: 标准分片策略，用于单分片键的场景
                shardingColumn: 分片列名
                preciseAlgorithmClassName: 精确的分片算法类名称，用于=和IN。该类需使用默认的构造器或者提供无参数的构造器
                rangeAlgorithmClassName: 范围的分片算法类名称，用于BETWEEN，可以不配置。该类需使用默认的构造器或者提供无参数的构造器
            complex: 复合分片策略，用于多分片键的场景
                shardingColumns : 分片列名，多个列以逗号分隔
                algorithmClassName: 分片算法类名称。该类需使用默认的构造器或者提供无参数的构造器
            inline: inline表达式分片策略
                shardingColumn : 分片列名
                algorithmInlineExpression: 分库算法Inline表达式，需要符合groovy动态语法
            hint: Hint分片策略
                algorithmClassName: 分片算法类名称。该类需使用默认的构造器或者提供无参数的构造器
            none: 不分片
        tableStrategy: 分表策略，同分库策略
  bindingTables: 绑定表列表
  - 逻辑表名列表，多个<logic_table_name>以逗号分隔
  
defaultDatabaseStrategy: 默认数据库分片策略，同分库策略
 
defaultTableStrategy: 默认数据表分片策略，同分库策略

props: 属性配置(可选)
    sql.show: 是否开启SQL显示，默认值: false
    executor.size: 工作线程数量，默认值: CPU核数
```

##### 分库分表数据源构建方式

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

#### 读写分离
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
```

##### 读写分离配置项说明

```yaml
dataSource: 数据源配置，同分库分表

name: 分库分表数据源名称

masterDataSourceName: master数据源名称

slaveDataSourceNames：slave数据源名称，用数组表示多个
```

##### 读写分离数据源构建方式

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

##### YAML格式特别说明
!! 表示实现类

[] 表示多个

#### 编排治理

##### Zookeeper分库分表编排配置项说明
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
      #绑定表中其余的表的策略与第一张表的策略相同
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
  #默认数据库分片策略
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  zookeeper:
    namespace: orchestration-yaml-demo
    serverLists: localhost:2181
```

##### Etcd分库分表编排配置项说明
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
      #绑定表中其余的表的策略与第一张表的策略相同
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
  #默认数据库分片策略
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

##### Zookeeper分库分表编排配置项说明

```yaml
dataSources: 数据源配置

shardingRule: 分片规则配置

orchestration: Zookeeper编排配置
  name: 编排服务节点名称
  overwrite: 本地配置是否可覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  zookeeper: Zookeeper注册中心配置
    namespace: Zookeeper的命名空间
    serverLists: 连接Zookeeper服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    baseSleepTimeMilliseconds: 等待重试的间隔时间的初始值。单位：毫秒
    maxSleepTimeMilliseconds: 等待重试的间隔时间的最大值。单位：毫秒
    maxRetries: 最大重试次数
    sessionTimeoutMilliseconds: 会话超时时间。单位：毫秒
    connectionTimeoutMilliseconds: 连接超时时间。单位：毫秒
    digest: 连接Zookeeper的权限令牌。缺省为不需要权限验证
```

##### Etcd分库分表编排配置项说明

```yaml
dataSources: 数据源配置

shardingRule: 分片规则配置

orchestration: Etcd编排配置
  name: 编排服务节点名称
  overwrite: 本地配置是否可覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准
  etcd: Etcd注册中心配置
    serverLists: 连接Etcd服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379
    timeToLiveSeconds: 临时节点存活时间。单位：秒
    timeoutMilliseconds: 每次请求的超时时间。单位：毫秒
    maxRetries: 每次请求的最大重试次数
    retryIntervalMilliseconds: 重试间隔时间。单位：毫秒
```

##### 分库分表编排数据源构建方式

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

##### 读写分离数据源构建方式

```java
    DataSource dataSource = OrchestrationMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

#### 柔性事务

##### 异步作业YAML文件配置
```yaml
#目标数据库的数据源.
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

#事务日志的数据源.
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
  
  #事务送达的最大尝试次数.
  maxDeliveryTryTimes: 3
  
  #执行送达事务的延迟毫秒数,早于此间隔时间的入库事务才会被作业执行
  maxDeliveryTryDelayMillis: 60000
```
