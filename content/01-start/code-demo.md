+++
toc = true
date = "2017-08-23T22:38:50+08:00"
title = "代码示例"
weight = 2
prev = "/01-start/quick-start"
next = "/01-start/faq"

+++

Sharding-JDBC代码示例github地址：https://github.com/shardingjdbc/sharding-jdbc-example

# 注意事项

1. 由于涉及到真实数据库环境，需要在准备测试的数据库上运行resources/manual_shcema.sql创建数据库，示例中使用的是MySQL环境，如需使用PostgreSQL、SQLServer或Oracle，请自行创建数据库脚本。

1. 所有代码示例均通过DDL语句自动创建数据表，无需用户手动创建。

1. 代码示例中关于数据库URL、驱动、用户名、密码的代码、yaml及Spring配置，需要用户自行修改。

1. 读写分离示例代码中的主库和从库需要用户自行在数据库层面配置主从关系，否则落到从库的读请求查询出来的数据会是空值。

# 原生JDBC代码示例

## 基于Java代码的原生JDBC示例

### 读写分离：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingTableOnlyMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.jdbc.java.RawJdbcJavaShardingAndMasterSlaveMain
```

## 基于Yaml的原生JDBC示例
 
### 读写分离：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingTableOnlyMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.jdbc.yaml.RawJdbcYamlShardingAndMasterSlaveMain
```

# Spring代码示例

## 基于JPA的Spring代码示例

### 读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingTableMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringJpaShardingDatabaseAndMasterSlaveMain
```

## 基于Mybatis的Spring代码示例

### 读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisMasterSlaveOnlyMain 
```

### 分库分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndTableMain
```

### 仅分库：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseOnlyMain
```

### 仅分表：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingTableMain
```

### 分库分表+读写分离：

```java
io.shardingjdbc.example.spring.namespace.jpa.SpringMybatisShardingDatabaseAndMasterSlaveMain
```

# 动态配置及治理代码示例

## 动态配置治理代码示例 sharding-jdbc-orchestration-example 

1. 准备Zookeeper环境，代码示例中使用的地址为localhost:2181

1. 运行

```java
io.shardingjdbc.example.orchestration.OrchestrationMain
```