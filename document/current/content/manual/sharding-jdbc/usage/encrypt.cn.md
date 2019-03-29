+++
toc = true
title = "数据脱敏"
weight = 6
+++

该章节主要介绍如何使用数据脱敏功能，如何进行相关配置。数据脱敏功能即可与数据分片功能共同使用，又可作为单独功能组件，独立使用。
与数据分片功能共同使用时，会创建ShardingDataSource；单独使用时，会创建EncryptDataSource来完成数据脱敏功能。

## 不使用Spring

### 引入Maven依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Java编码的规则配置

```java
       // 配置数据源
       BasicDataSource dataSource = new BasicDataSource();
       dataSource.setDriverClassName("com.mysql.jdbc.Driver");
       dataSource.setUrl("jdbc:mysql://localhost:3306/encrypt");
       dataSource.setUsername("root");
       dataSource.setPassword("");
       
       // 配置脱敏规则
       Properties props = new Properties();
       props..setProperty("aes.key.value", "123456");
       EncryptorConfiguration encryptorConfig = new EncryptorConfiguration("aes", "user_id", props);
       EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration();
       encryptTableRuleConfig.setTable("t_order");
       encryptTableRuleConfig.setEncryptorConfig(encryptorConfig);
       EncryptRuleConfiguration ruleConfiguration = new EncryptRuleConfiguration();
       ruleConfiguration.getTableRuleConfigs().add(encryptTableRuleConfig);
       
       // 获取数据源对象
       DataSource dataSource = EncryptDataSourceFactory.createDataSource(dataSource, ruleConfiguration);
```

### 基于Yaml的规则配置

或通过Yaml方式配置，与以上配置等价：

```yaml
dataSource: !!com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.jdbc.Driver
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
  username: root
  password: 
  
encryptRule:
  tables:
    t_order:     
      encryptor:
        type: aes
        columns: user_id        
        props:
          aes.key.value: 123456
  defaultEncryptor:
    type: md5
    columns: order_id        
```

```java
    DataSource dataSource = YamlEncryptDataSourceFactory.createDataSource(yamlFile);
```

## 使用Spring

### 引入Maven依赖

```xml
<!-- for spring boot -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-boot-starter</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>

<!-- for spring namespace -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-spring-namespace</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### 基于Spring boot的规则配置

```properties
sharding.jdbc.datasource.name=ds

sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/encrypt
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.config.encrypt.tables.t_order.encryptor.type=aes
sharding.jdbc.config.encrypt.tables.t_order.encryptor.columns=user_id
sharding.jdbc.config.encrypt.tables.t_order.encryptor.props.aes.key.value=123456

sharding.jdbc.config.encrypt.defaultEncryptor.type=md5
sharding.jdbc.config.encrypt.defaultEncryptor.columns=order_id
```

### 基于Spring命名空间的规则配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding 
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd 
                        ">
    <bean id="ds" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/encrypt" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean:properties id="props">
        <prop key="aes.key.value">123456</prop>
    </bean:properties>

    <encrypt:encryptor id="md5" type="MD5" columns="order_id" />
    <encrypt:encryptor id="aes" type="AES" columns="user_id" props-ref="props" />
    
    <encrypt:data-source id="encryptDataSource">
        <encrypt:encrypt-rule data-source-name="ds">
            <encrypt:table-rules>
                <encrypt:table-rule logic-table="t_order" encryptor-ref="aes" />
                <encrypt:default-encryptor encryptor-ref="md5" />
            </encrypt:table-rules>
        </encrypt:sharding-rule>
    </encrypt:data-source>
</beans>
```

### 在Spring中使用DataSource

直接通过注入的方式即可使用DataSource，或者将DataSource配置在JPA、Hibernate或MyBatis中使用。

```java
@Resource
private DataSource dataSource;
```