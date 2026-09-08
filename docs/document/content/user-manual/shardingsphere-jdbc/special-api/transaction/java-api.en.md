+++
title = "Use Java API"
weight = 1
+++

## Background

With ShardingSphere-JDBC, XA and BASE mode transactions can be used through the API.

## Prerequisites

Add the ShardingSphere-JDBC Maven dependency:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

When using XA transactions, `shardingsphere-jdbc` already includes the XA core module as a transitive dependency. Add one XA provider that matches `providerType`.

For Narayana:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

For Atomikos:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-atomikos</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

When using BASE transactions, add both the ShardingSphere Seata AT integration module and Seata Client:

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
<dependency>
    <groupId>org.apache.seata</groupId>
    <artifactId>seata-all</artifactId>
    <version>2.6.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.antlr</groupId>
            <artifactId>antlr4-runtime</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Procedure

Perform the business logic using transactions

## Sample

```java
// Use ShardingSphereDataSource to get a connection and perform transaction operations.
try (Connection connection = dataSource.getConnection()) {
    connection.setAutoCommit(false);
    PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (user_id, status) VALUES (?, ?)");
    preparedStatement.setObject(1, 1000);
    preparedStatement.setObject(2, "init");
    preparedStatement.executeUpdate();
    connection.commit();
}
```
