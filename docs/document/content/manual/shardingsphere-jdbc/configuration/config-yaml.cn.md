+++
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
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds0
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
  
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

### 数据脱敏
```yaml
dataSource:  !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
  username: root
  password:

encryptRule:
  encryptors:
    encryptor_aes:
      type: aes
      props:
        aes.key.value: 123456abc
    encryptor_md5:
      type: md5
  tables:
    t_encrypt:
      columns:
        user_id:
          plainColumn: user_plain
          cipherColumn: user_cipher
          encryptor: encryptor_aes
        order_id:
          cipherColumn: order_cipher
          encryptor: encryptor_md5
props:
  query.with.cipher.column: true #是否使用密文列查询
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
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ms_ds${0..1}.t_order_item${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ms_ds${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item${order_id % 2}  
  bindingTables:
    - t_order,t_order_item
  broadcastTables:
    - t_config
  
  defaultDataSourceName: ds_0
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
  
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

### 数据分片 + 数据脱敏

```yaml
dataSources:
  ds_0: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0
    username: root
    password:
  ds_1: !!com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1
    username: root
    password:

shardingRule:
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds_${user_id % 2}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGenerator:
        type: SNOWFLAKE
        column: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: ds_${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item 
  
  defaultTableStrategy:
    none:
    
  encryptRule:
    encryptors:
      encryptor_aes:
        type: aes
        props:
          aes.key.value: 123456abc
    tables:
      t_order:
        columns:
          order_id:
            plainColumn: order_plain
            cipherColumn: order_cipher
            encryptor: encryptor_aes

props:
  sql.show: true
```

### 治理

```yaml
#省略数据分片、读写分离和数据脱敏配置

orchestration:
  orchestration_ds:
    orchestrationType: config_center,registry_center,metadata_center
    instanceType: zookeeper
    serverLists: localhost:2181
    namespace: orchestration
    props:
      overwrite: true
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
      actualDataNodes: #由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况
        
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
      keyGenerator: 
        column: #自增列名称，缺省表示不使用自增主键生成器
        type: #自增列值生成器类型，缺省表示使用默认自增列值生成器。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID
        props: #属性配置, 注意：使用SNOWFLAKE算法，需要配置worker.id与max.tolerate.time.difference.milliseconds属性。若使用此算法生成值作分片值，建议配置max.vibration.offset属性
          <property-name>: 属性名称
      
  bindingTables: #绑定表规则列表
  - <logic_table_name1, logic_table_name2, ...> 
  - <logic_table_name3, logic_table_name4, ...>
  - <logic_table_name_x, logic_table_name_y, ...>
  broadcastTables: #广播表规则列表
  - table_name1
  - table_name2
  - table_name_x
  
  defaultDataSourceName: #未配置分片规则的表将通过默认数据源定位  
  defaultDatabaseStrategy: #默认数据库分片策略，同分库策略
  defaultTableStrategy: #默认表分片策略，同分库策略
  defaultKeyGenerator: #默认的主键生成算法 如果没有设置,默认为SNOWFLAKE算法
    type: #默认自增列值生成器类型，缺省将使用org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator。可使用用户自定义的列值生成器或选择内置类型：SNOWFLAKE/UUID
    props:
      <property-name>: #自增列值生成器属性配置, 比如SNOWFLAKE算法的worker.id与max.tolerate.time.difference.milliseconds

  masterSlaveRules: #读写分离规则，详见读写分离部分
    <data_source_name>: #数据源名称，需要与真实数据源匹配，可配置多个data_source_name
      masterDataSourceName: #详见读写分离部分
      slaveDataSourceNames: #详见读写分离部分
      loadBalanceAlgorithmType: #详见读写分离部分
      props: #读写分离负载算法的属性配置
        <property-name>: #属性值
      
props: #属性配置
  sql.show: #是否开启SQL显示，默认值: false
  executor.size: #工作线程数量，默认值: CPU核数
  max.connections.size.per.query: # 每个查询可以打开的最大连接数量,默认为1
  check.table.metadata.enabled: #是否在启动时检查分表元数据一致性，默认值: false
```

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
  loadBalanceAlgorithmType: #从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`loadBalanceAlgorithmClassName`存在则忽略该配置
  props: #读写分离负载算法的属性配置
    <property-name>: #属性值
```

### 数据脱敏

```yaml
dataSource: #省略数据源配置

encryptRule:
  encryptors:
    <encryptor-name>:
      type: #加解密器类型，可自定义或选择内置类型：MD5/AES 
      props: #属性配置, 注意：使用AES加密器，需要配置AES加密器的KEY属性：aes.key.value
        aes.key.value: 
  tables:
    <table-name>:
      columns:
        <logic-column-name>:
          plainColumn: #存储明文的字段
          cipherColumn: #存储密文的字段
          assistedQueryColumn: #辅助查询字段，针对ShardingQueryAssistedEncryptor类型的加解密器进行辅助查询
          encryptor: #加密器名字
```

### 治理

```yaml
dataSources: #省略数据源配置
shardingRule: #省略分片规则配置
masterSlaveRule: #省略读写分离规则配置
encryptRule: #省略数据脱敏规则配置

orchestration:
  orchestration_ds: #治理实例名称
    orchestrationType: #治理类型，例如config_center/registry_center/metadata_center
    instanceType: #配置/注册/元数据中心类型。如：zookeeper
    serverLists: #连接配置/注册/元数据中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181
    namespace: #配置/注册/元数据中心的命名空间
    props: #其它配置
      overwrite: #本地配置是否覆盖配置中心配置。如果可覆盖，每次启动都以本地配置为准
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
