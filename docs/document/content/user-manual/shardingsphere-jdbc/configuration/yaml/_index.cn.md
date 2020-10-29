+++
title = "YAML 配置"
weight = 2
+++

## 简介

YAML 提供通过配置文件的方式与 ShardingSphere-JDBC 交互。配合治理模块一同使用时，持久化在配置中心的配置均为 YAML 格式。

YAML 配置是最常见的配置方式，可以省略编程的复杂度，简化用户配置。

## 使用方式

### 创建简单数据源

通过 YamlShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 指定 YAML 文件路径
File yamlFile = // ...

DataSource dataSource = YamlShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### 创建携带治理功能的数据源

通过 YamlGovernanceShardingSphereDataSourceFactory 工厂创建的 GovernanceShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。

```java
// 指定 YAML 文件路径
File yamlFile = // ...

DataSource dataSource = YamlGovernanceShardingSphereDataSourceFactory.createDataSource(yamlFile);
```

### 使用数据源

可通过 DataSource 选择使用原生 JDBC，或JPA， MyBatis 等 ORM 框架。

以原生 JDBC 使用方式为例：

```java
DataSource dataSource = // 通过Apache ShardingSphere 工厂创建的数据源
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```

## YAML 配置项

### 数据源配置

分为单数据源配置和多数据源配置。
单数据源配置用于数据加密规则；多数据源配置用于分片、读写分离等规则。
如果加密和分片等功能混合使用，则应该使用多数据源配置。

#### 单数据源配置

##### 配置示例

```yaml
dataSource: !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  url: jdbc:mysql://127.0.0.1:3306/ds_name
  username: root
  password: root
```

##### 配置项说明

```yaml
dataSource: # <!!数据库连接池实现类> `!!`表示实例化该类
  driverClassName: # 数据库驱动类名
  url: # 数据库 URL 连接
  username: # 数据库用户名
  password: # 数据库密码
  # ... 数据库连接池的其它属性
```

#### 多数据源配置

##### 配置示例

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password:
  ds_1: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password:
```

##### 配置项说明

```yaml
dataSources: # 数据源配置，可配置多个 <data-source-name>
  <data-source-name>: # <!!数据库连接池实现类>，`!!` 表示实例化该类
    driverClassName: # 数据库驱动类名
    url: # 数据库 URL 连接
    username: # 数据库用户名
    password: # 数据库密码
    # ... 数据库连接池的其它属性
```

### 规则配置

以规则别名开启配置，可配置多个规则。

#### 配置示例

```yaml
rules:
-! XXX_RULE_0
  xxx
-! XXX_RULE_1
  xxx
```

#### 配置项说明

```yaml
rules:
-! XXX_RULE # 规则别名，`-` 表示可配置多个规则
  # ... 具体的规则配置
```

更多详细配置请参见具体的规则配置部分。

### 属性配置

#### 配置示例

```yaml
props:
  xxx: xxx
```

#### 配置项说明

```yaml
props:
  xxx: xxx # 属性名称以及对应的值
```

更多详细配置请参见具体的规则配置部分。

### 语法说明

`!!` 表示实例化该类

`!` 表示自定义别名

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
