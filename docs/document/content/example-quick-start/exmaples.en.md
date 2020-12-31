# How to compile shardingsphere-example
The shardingsphere-example module is based on the ShardingSphere 5.0.0-RC1-SNAPSHOT(master) version, 5.0.0-RC1-SNAPSHOT has not been released, which may cause your dependency import to fail. You have the following two most effective methods to choose from, which may help you compile and run the examples module effectively.
## Method 1
If you want to select a stable version of ShardingSphere, you can reduce the version of ShardingSphere 5.0.0-alpha, you only need to reduce the shardingsphere.version in pom.xml under the examples module from 5.0.0-RC1-SNAPSHOT to 5.0.0-alpha. <br />In fact, 5.0.0-alpha and 5.0.0-RC1-SNAPSHOT have some differences, which may cause your running or even compilation failure. To avoid troubles, other Markdown files will provide you with effective modification solutions.
## Method 2
Maybe you want to use 5.0.0-RC1-SNAPSHOT running examples, you need to perform the following operations.
### GetSource
Run in your local directory:
`git clone https://github.com/apache/shardingsphere.git`
Or download the package to your local computer.
### Compile source code
After the source code is ready, go to the root directory where the source code is located and run the following command: <br />`mvn clean install -P release`<br />Can be used after Compilation5.0.0-RC1-SNAPSHOT to run your examples.
# About ShardingSphere-example
In order to make it easier for you to use, each module will be analyzed next, and some modules will even propose solutions to the problems you may encounter.<br />The structure is as follows:
```java
shardingsphere-example
  ├── example-core
  │   ├── config-utility
  │   ├── example-api
  │   ├── example-raw-jdbc
  │   ├── example-spring-jpa
  │   └── example-spring-mybatis
  ├── shardingsphere-jdbc-example
  │   ├── sharding-example
  │   │   ├── sharding-raw-jdbc-example
  │   │   ├── sharding-spring-boot-jpa-example
  │   │   ├── sharding-spring-boot-mybatis-example
  │   │   ├── sharding-spring-namespace-jpa-example
  │   │   └── sharding-spring-namespace-mybatis-example
  │   ├── governance-example
  │   │   ├── governance-raw-jdbc-example
  │   │   ├── governance-spring-boot-example
  │   │   └── governance-spring-namespace-example
  │   ├── transaction-example
  │   │   ├── transaction-2pc-xa-example
  │   │   └── transaction-base-seata-example
  │   ├── other-feature-example
  │   │   ├── hint-example
  │   │   └── encrypt-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
```


