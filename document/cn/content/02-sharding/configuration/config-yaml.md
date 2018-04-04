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

#### 分库分表配置项说明

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

#### 分库分表数据源构建方式

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

#### 读写分离配置项说明

```yaml
dataSource: 数据源配置，同分库分表

name: 分库分表数据源名称

masterDataSourceName: master数据源名称

slaveDataSourceNames：slave数据源名称，用数组表示多个
```

#### 读写分离数据源构建方式

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### YAML格式特别说明
!! 表示实现类

[] 表示多个
