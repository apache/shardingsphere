# 如何编译shardingsphere-example
shardingsphere-example模块是基于ShardingSphere 5.0.0-RC1-SNAPSHOT（master）版本的，5.0.0-RC1-SNAPSHOT版本尚未发布，因此可能导致您的依赖导入失败。您有如下两只最有效方式可以选择，或许可以有效地帮助您编译并运行examples模块。
## 方式一
如果您想选择稳定版本的ShardingSphere，您可以将ShardingSphere的版本降低为5.0.0-alpha，您只需将examples模块下的pom.xml中的shardingsphere.version由5.0.0-RC1-SNAPSHOT降低为5.0.0-alpha即可。<br />事实上，5.0.0-alpha版本与5.0.0-RC1-SNAPSHOT存在一些差异，因此可能会导致您的运行，甚至编译失败。为了避免造成困扰，在其他的Markdown文件中将会为您提供有效的修改方案。
## 方式二
或许您想要使用5.0.0-RC1-SNAPSHOT版本运行examples，您需要做出如下几项操作。
### 获取源码
在您的本地目录下执行指令：
`git clone https://github.com/apache/shardingsphere.git`
或者通过压缩包的方式下载到您的本地。
### 编译源码
源码准备好以后进入到源码所在的根目录执行以下命令：<br />`mvn clean install -P release`<br />编译完成后即可使用5.0.0-RC1-SNAPSHOT版本来运行您的examples了。
# 关于ShardingSphere-example
为了更加方便您的使用，接下来将会对每一个模块进行分析，有的模块甚至会为您可能遇到的问题提出解决的方法。<br />该的结构如下：
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


