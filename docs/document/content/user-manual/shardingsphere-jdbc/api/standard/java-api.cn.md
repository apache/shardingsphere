+++
title = "Java API"
weight = 1
+++

## 引入 Maven 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

## 构建数据源

ShardingSphere-JDBC 的 Java API 通过 Schema 名称、运行模式、数据源集合、规则集合以及属性配置组成。

### 构建运行模式

运行模式配置对象包含三个参数，分别是运行模式类型、用于存储配置数据的仓库配置和是否用当前的 Java 配置覆盖已经持久化的配置。

目前包括三种运行模式，分别是：内存模式、本地模式、集群模式。

对于用于存储配置数据的仓库配置根据运行模式不同，有不同的配置类：

- 内存模式无需仓库配置对象；
- 本地模式和集群模式均需要使用各自的仓库配置对象。

三种运行模式的配置示例分别是：

- 内存模式

```java
ModeConfiguration modeConfig = new ModeConfiguration("Memory", null, true);
```

- 本地模式

```java
StandalonePersistRepositoryConfiguration repositoryConfig = new StandalonePersistRepositoryConfiguration("Local", new Properties());
ModeConfiguration modeConfig = new ModeConfiguration("Standalone", repositoryConfig, false);
```

- 集群模式

```java
ClusterPersistRepositoryConfiguration repositoryConfig = new ClusterPersistRepositoryConfiguration("ZooKeeper", "my_schema", "localhost:2181", new Properties());
ModeConfiguration modeConfig = new ModeConfiguration("Cluster", repositoryConfig, false);
```

### 构建真实数据源

示例的数据库连接池为 HikariCP，可根据业务场景更换为其他数据库连接池。

```java
Map<String, DataSource> dataSourceMap = new HashMap<>();

// 配置第 1 个数据源
HikariDataSource dataSource1 = new HikariDataSource();
dataSource1.setDriverClassName("com.mysql.jdbc.Driver");
dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/ds_1");
dataSource1.setUsername("root");
dataSource1.setPassword("");
dataSourceMap.put("ds_1", dataSource1);

// 配置第 2 个数据源
HikariDataSource dataSource2 = new HikariDataSource();
dataSource2.setDriverClassName("com.mysql.jdbc.Driver");
dataSource2.setJdbcUrl("jdbc:mysql://localhost:3306/ds_2");
dataSource2.setUsername("root");
dataSource2.setPassword("");
dataSourceMap.put("ds_2", dataSource2);
```

### 构建规则

规则属于 Apache ShardingSphere 的可插拔增量功能，请参见相应规则的配置手册。

### 构建 ShardingSphere 数据源

```java
ModeConfiguration modeConfig = ... // 构建运行模式
Map<String, DataSource> dataSourceMap = ... // 构建真实数据源
Collection<RuleConfiguration> ruleConfigs = ... // 构建具体规则
Properties props = ... // 构建属性配置
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("my_schema", modeConfig, dataSourceMap, ruleConfigs, props);
```

## 使用 ShardingSphere 数据源

通过 ShardingSphereDataSourceFactory 工厂创建的 ShardingSphereDataSource 实现自 JDBC 的标准接口 DataSource。
可通过 DataSource 选择使用原生 JDBC，或 JPA、Hibernate、MyBatis 等 ORM 框架。

以原生 JDBC 使用方式为例：

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("my_schema", modeConfig, dataSourceMap, ruleConfigs, props);
String sql = "SELECT xxx FROM xxx WHERE xxx=? AND xxx=?";
try (
        Connection conn = dataSource.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setInt(1, 10);
    ps.setInt(2, 1000);
    try (ResultSet rs = ps.executeQuery()) {
        while(rs.next()) {
            // ...
        }
    }
}
```
