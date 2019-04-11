+++
toc = true
title = "Data Masking"
weight = 6
+++

This chapter mainly introduces how to use the feather of Data Masking. On one hand User can use Data Masking and Sharding together, which will
create ShardingDataSource, On another hand, when user only adopt the feather of Data Masking, ShardingSphere will create EncryptDataSource.

## Not Use Spring

### Introduce Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${sharding-sphere.version}</version>
</dependency>
```

### Rule Configuration Based on Java

```java
       // Configure actual data source
       BasicDataSource dataSource = new BasicDataSource();
       dataSource.setDriverClassName("com.mysql.jdbc.Driver");
       dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/encrypt");
       dataSource.setUsername("root");
       dataSource.setPassword("");
       
       // Configure the encrypt rule
       Properties props = new Properties();
       props.setProperty("aes.key.value", "123456");
       EncryptorRuleConfiguration encryptorConfig = new EncryptorRuleConfiguration("aes", "t_order.order_id", new Properties());
       EncryptRuleConfiguration ruleConfiguration = new EncryptRuleConfiguration();
       ruleConfiguration.getEncryptorRuleConfigs().put("order_encryptor", encryptorConfig);
       
       // Get data source
       DataSource dataSource = EncryptDataSourceFactory.createDataSource(dataSource, ruleConfiguration);
```

### Rule Configuration Based on Yaml


```yaml
dataSource:  !!org.apache.commons.dbcp2.BasicDataSource
  driverClassName: com.mysql.jdbc.Driver
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
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

## Use Native JDBC

### Introduce Maven Dependency

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

### Rule Configuration Based on Spring Boot

```properties
sharding.jdbc.datasource.name=ds

sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp2.BasicDataSource
sharding.jdbc.datasource.ds.driver-class-name=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=
sharding.jdbc.datasource.ds.max-total=100

sharding.jdbc.config.encrypt.encryptors.order_encrypt.type=aes
sharding.jdbc.config.encrypt.encryptors.order_encrypt.qualifiedColumns=t_order.user_id
sharding.jdbc.config.encrypt.encryptors.order_encrypt.props.aes.key.value=123456
```

### Rule Configuration Based on Spring Name Space

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
   
    <bean id="db" class="org.apache.commons.dbcp2.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://127.0.0.1:3306/encrypt?serverTimezone=UTC&useSSL=false" />
        <property name="username" value="root" />
        <property name="password" value="" />
        <property name="maxTotal" value="100" />
    </bean>
    
    <bean:properties id="props">
        <prop key="aes.key.value">123456</prop>
    </bean:properties>
    
    <encrypt:data-source id="encryptDataSource">
        <encrypt:encrypt-rule data-source-name="db">
            <encrypt:encryptor-rule id="user_encryptor" type="MD5" qualified-columns="t_order.user_id" />
            <encrypt:encryptor-rule id="order_encryptor" type="AES" qualified-columns="t_order.order_id" props-ref="props" />
        </encrypt:encrypt-rule>
    </encrypt:data-source>
</beans>
```

### Use DataSource in Spring

```java
@Resource
private DataSource dataSource;
```