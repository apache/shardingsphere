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
       props.setProperty("aes.key.value", "123456");
       EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("aes", "t_order.order_id", new Properties());
       EncryptRuleConfiguration ruleConfiguration = new EncryptRuleConfiguration();
       ruleConfiguration.getEncryptorRuleConfigs().put("order_encryptor", encryptorConfig);
       
       // 获取数据源对象
       DataSource dataSource = EncryptDataSourceFactory.createDataSource(dataSource, ruleConfiguration);
```

### 基于Yaml的规则配置

或通过Yaml方式配置，与以上配置等价：

```yaml
dataSource:  !!com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.jdbc.Driver
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
  username: root
  password:
  
encryptRule:
  encryptors:
    order_encryptor:
      type: aes
      qualifiedColumns: t_order.user_id
      props:
        aes.key.value: 123456
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
sharding.jdbc.datasource.ds.driver-class-name=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
sharding.jdbc.datasource.ds.username=sa
sharding.jdbc.datasource.ds.password=
sharding.jdbc.datasource.ds.max-total=100

sharding.jdbc.config.encrypt.encryptors.order_encrypt.type=aes
sharding.jdbc.config.encrypt.encryptors.order_encrypt.qualifiedColumns=t_order.user_id
sharding.jdbc.config.encrypt.encryptors.order_encrypt.props.aes.key.value=123456
```

### 基于Spring命名空间的规则配置

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:encrypt="http://shardingsphere.apache.org/schema/shardingsphere/encrypt"
       xmlns:bean="http://www.springframework.org/schema/util"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt 
                        http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd 
                        http://www.springframework.org/schema/util 
                        http://www.springframework.org/schema/util/spring-util.xsd">
    <import resource="datasource/dataSource.xml" />
    
    <bean:properties id="props">
        <prop key="aes.key.value">123456</prop>
    </bean:properties>
    
    <encrypt:data-source id="encryptDataSource">
        <encrypt:encrypt-rule data-source-name="dbtbl_0">
            <encrypt:encryptor-rule id="user_encryptor" type="MD5" qualified-columns="t_order.user_id" />
            <encrypt:encryptor-rule id="order_encryptor" type="AES" qualified-columns="t_order.order_id" props-ref="props" />
        </encrypt:encrypt-rule>
    </encrypt:data-source>
</beans>
```

### 在Spring中使用DataSource

直接通过注入的方式即可使用DataSource，或者将DataSource配置在JPA、Hibernate或MyBatis中使用。

```java
@Resource
private DataSource dataSource;
```