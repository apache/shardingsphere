+++
toc = true
title = "编排治理"
weight = 1
prev = "/04-orchestration/"
next = "/04-orchestration/apm/"
+++

2.x版本开始，Sharding-JDBC提供了数据库治理功能，主要包括：

* 配置集中化与动态化，可支持数据源、表与分片及读写分离策略的动态切换
* 数据治理。提供熔断数据库访问程序对数据库的访问和禁用从库的访问的能力
* 支持Zookeeper和Etcd的注册中心

## Zookeeper注册中心

请使用Zookeeper 3.4.6及其以上版本搭建注册中心。[详情参见](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)

## Etcd注册中心

请使用Etcd V3及其以上版本搭建注册中心。[详情参见](https://coreos.com/etcd/docs/latest)

## 注册中心数据结构

注册中心在定义的命名空间下，创建数据库访问对象运行节点，用于区分不同数据库访问实例。命名空间中包含2个数据子节点，分别是config, state。

## config节点

数据治理相关配置信息，以JSON格式存储，包括数据源，分库分表，读写分离、ConfigMap及Properties配置，可通过修改节点来实现对于配置的动态管理。

```
config
    ├──datasource                                数据源配置
    ├──sharding                                  分库分表（包括分库分表+读写分离）配置根节点
    ├      ├──rule                               分库分表（包括分库分表+读写分离）规则
    ├      ├──configmap                          分库分表ConfigMap配置，以K/V形式存储，如：{"key1":"value1"}
    ├      ├──props                              Properties配置
    ├──masterslave                               读写分离独立使用配置
    ├      ├──rule                               读写分离规则
    ├      ├──configmap                          读写分离ConfigMap配置，以K/V形式存储，如：{"key1":"value1"}
```

### datasource子节点

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）

```json
[{"name":"demo_ds","clazz":"org.apache.commons.dbcp.BasicDataSource","defaultAutoCommit":"true","defaultReadOnly":"false","defaultTransactionIsolation":"-1","driverClassName":"com.mysql.jdbc.Driver","initialSize":"0","logAbandoned":"false","maxActive":"8","maxIdle":"8","maxOpenPreparedStatements":"-1","maxWait":"-1","minEvictableIdleTimeMillis":"1800000","minIdle":"0","numTestsPerEvictionRun":"3","password":"","removeAbandoned":"false","removeAbandonedTimeout":"300","testOnBorrow":"false","testOnReturn":"false","testWhileIdle":"false","timeBetweenEvictionRunsMillis":"-1","url":"jdbc:mysql://localhost:3306/demo_ds","username":"root","validationQueryTimeout":"-1"}]
```

### sharding子节点

#### rule子节点

分库分表配置，包括分库分表+读写分离配置

```json
{"tableRuleConfigs":[{"logicTable":"t_order","actualDataNodes":"demo_ds.t_order_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_id"},{"logicTable":"t_order_item","actualDataNodes":"demo_ds.t_order_item_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_item_id"}],"bindingTableGroups":["t_order, t_order_item"],"defaultDatabaseShardingStrategyConfig":{},"defaultTableShardingStrategyConfig":{},"masterSlaveRuleConfigs":[]}
```

#### configmap子节点

分库分表ConfigMap配置，以K/V形式存储

```json
{"key1":"value1"}
```

#### props子节点

相对于sharding-jdbc配置里面的Sharding Properties

```json
{"executor.size":"20","sql.show":"true"}
```

### masterslave子节点

#### rule子节点

读写分离独立使用时使用该配置

```json
{"name":"ds_ms","masterDataSourceName":"ds_master","slaveDataSourceNames":["ds_slave_0","ds_slave_1"],"loadBalanceAlgorithmType":"ROUND_ROBIN"}
```

#### configmap子节点

读写分离ConfigMap配置，以K/V形式存储

```json
{"key1":"value1"}
```

## state节点

state节点包括instances和datasource节点。

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....                                    
```

### instances节点
数据库访问对象运行实例信息，子节点是当前运行实例的标识。运行实例标识由运行服务器的IP地址和PID构成。运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### datasource节点
可以治理读写分离从库，可动态添加删除以及禁用，预计2.0.0.M3发布

# 操作指南

## instances节点 

可在IP地址@-@PID节点写入DISABLED（忽略大小写）表示禁用该实例，删除DISABLED表示启用。

Zookeeper命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

Etcd命令如下：

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

## datasources节点 

在读写分离（或分库分表+读写分离）场景下，可在数据源名称子节点中写入DISABLED表示禁用从库数据源，删除DISABLED或节点表示启用。（2.0.0.M3及以上版本支持）。

Zookeeper命令如下：

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

Etcd命令如下：

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

# 使用示例

## 1.JAVA配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Zookeeper配置示例

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                     new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
     
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

### Etcd配置示例

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

## 2.YAML配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### 配置示例

#### Zookeeper分库分表编排配置项说明
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

#### Etcd分库分表编排配置项说明
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

### YAML格式特别说明
!! 表示实现类

[] 表示多个

## 3.Spring命名空间配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Zookeeper配置示例
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:zookeeper id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
</beans>
```

### Zookeeper标签说明

#### \<reg:zookeeper/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                         |
| server-lists                    | String | 是     |       | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                       | String | 是     |       | Zookeeper的命名空间                                                                                |
| base-sleep-time-milliseconds    | int    | 否     | 1000  | 等待重试的间隔时间的初始值<br />单位：毫秒                                                               |
| max-sleep-time-milliseconds     | int    | 否     | 3000  | 等待重试的间隔时间的最大值<br />单位：毫秒                                                               |
| max-retries                     | int    | 否     | 3     | 最大重试次数                                                                                          |
| session-timeout-milliseconds    | int    | 否     | 60000 | 会话超时时间<br />单位：毫秒                                                                           |
| connection-timeout-milliseconds | int    | 否     | 15000 | 连接超时时间<br />单位：毫秒                                                                           |
| digest                          | String | 否     |       | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证                                                      |

### Etcd配置示例
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:etcd id="regCenter" server-lists="http://localhost:2379" time-to-live-seconds="60" timeout-milliseconds="500" max-retries="3" retry-interval-milliseconds="200"/>
</beans>
```

### Etcd标签说明

#### \<reg:etcd/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                           |
| server-lists                    | String | 是     |       | 连接Etcd服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: http://host1:2379,http://host2:2379 |
| time-to-live-seconds            | int    | 否     | 60    | 临时节点存活时间<br />单位：秒                                                                         |
| timeout-milliseconds            | int    | 否     | 500   | 每次请求的超时时间<br />单位：毫秒                                                                      |
| max-retries                     | int    | 否     | 3     | 每次请求的最大重试次数                                                                                 |
| retry-interval-milliseconds     | int    | 否     | 200   | 重试间隔时间<br />单位：毫秒                                                                           |


## 4.Spring Boot配置

### 引入maven依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Zookeeper配置示例

#### 编排分库分表Spring Boot配置

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

### Etcd配置示例

#### 编排分库分表Spring Boot配置

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

#### 编排分库分表Spring Boot配置项说明
同[分库分表Yaml配置](#分库分表编排配置项说明)
