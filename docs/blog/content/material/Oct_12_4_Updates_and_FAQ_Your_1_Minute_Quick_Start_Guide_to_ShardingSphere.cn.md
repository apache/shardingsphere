+++
title = "ShardingSphere 知识库更新 | 官方样例集助你快速上手"
weight = 20
chapter = true
+++

Apache ShardingSphere 作为 Apache 顶级项目，是数据库领域最受欢迎的开源项目之一。经过 5 年多的发展，ShardingSphere 已获得超 14K Stars 的关注，270+ 贡献者，建立起了活跃的社区生态。

随着项目的蓬勃发展，版本的不断更迭，Apache ShardingSphere 支持的特性逐渐增多，功能日益强大，配置规则也在不断优化。为了帮助用户更好地理解各项特性和配置规则，方便用户快速测试并运行相关功能组件，找到最佳实现，`shardingsphere-example` 项目应运而生。

`shardingsphere-example` 是一个独立的 Maven 项目，位于 `Apache ShardingSphere` 项目的 examples 目录下。项目地址：

https://github.com/apache/shardingsphere/tree/master/examples

**江龙滔**

SphereEx 中间件研发工程师，Apache ShardingSphere contributor。目前专注于 ShardingSphere 数据库中间件研发及开源社区建设。


**侯阳**

SphereEx 中间件研发工程师，目前从事 ShardingSphere 数据库中间件研发，热爱开源，希望同大家一起建设更好的社区。







## 模块详解


`shardingsphere-example` 项目包含多个模块，将为用户带来水平拆分、读写分离、分布式治理、分布式事务、数据加密、强制路由、影子库等功能的使用及配置样例，覆盖 Java API、YAML、Spring Boot、Spring Namespace 等多种业务常用的接入形态。除了 `ShardingSphere-JDBC`，`shardingsphere-example` 中还增加了 `ShardingSphere-Proxy` 和 `ShardingSphere-Parser` 的使用案例。

所有涉及到 `Apache ShardingSphere `的功能特性、接入场景以及各种灵活的配置方式，都可以在官方的 repo 里找到样例，方便用户查询和参考。下表展示了 `shardingsphere-example` 的模块分布情况：

~~~
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
  │   │   ├── governance-spring-boot-mybatis-example
  │   │   └── governance-spring-namespace-mybatis-example
  │   ├── transaction-example
  │   │   ├── transaction-2pc-xa-atomikos-raw-jdbc-example
  │   │   ├── transaction-2pc-xa-bitronix-raw-jdbc-example
  │   │   ├── transaction-2pc-xa-narayana-raw-jdbc-example
  │   │   ├── transaction-2pc-xa-spring-boot-example
  │   │   ├── transaction-2pc-xa-spring-namespace-example
  │   │   ├── transaction-base-seata-raw-jdbc-example       
  │   │   └── transaction-base-seata-spring-boot-example
  │   ├── other-feature-example
  │   │   ├── encrypt-example
  │   │   │   ├── encrypt-raw-jdbc-example
  │   │   │   ├── encrypt-spring-boot-mybatis-example
  │   │   │   └── encrypt-spring-namespace-mybatis-example
  │   │   ├── hint-example
  │   │   │   └── hint-raw-jdbc-example
  │   │   └── shadow-example
  │   │   │   ├── shadow-raw-jdbc-example
  │   │   │   ├── shadow-spring-boot-mybatis-example
  │   │   │   └── shadow-spring-namespace-mybatis-example
  │   ├── extension-example
  │   │   └── custom-sharding-algortihm-example
  ├── shardingsphere-parser-example
  ├── shardingsphere-proxy-example
  │   ├── shardingsphere-proxy-boot-mybatis-example
  │   └── shardingsphere-proxy-hint-example
  └── src/resources
        └── manual_schema.sql
~~~

**1. example-core**

example 核心模块，包含实体、接口定义和其他公用代码。

**2. shardingsphere-jdbc-example**

ShardingSphere-JDBC 示例模块，展示 ShardingSphere-JDBC 的功能特性和各种使用方式。

**（1）sharding-example**

展示如何使用  ShardingSphere-JDBC 进行数据分片，包含分库、分表、分库+分表、读写分离、读写分离+分库分表的应用场景。在 ORM 集成方面，本模块也贴心的为用户提供了 MyBatis 和 JPA 的集成样例。

**（2）governance-example**

展示 ShardingSphere-JDBC 在分布式治理方面的应用，包含了分库分表、读写分离、数据加密、影子库等特性与分布式治理相结合的应用场景。

**注意：** 分布式治理 example 依赖 Apache ZooKeeper，请自行部署。

**（3）transaction-example**

展示 ShardingSphere-JDBC 支持的多种分布式事务管理方式，用户可以根据应用场景选择适合的分布式事务管理器进行使用。鉴于分布式事务的特殊性，本模块的示例都是基于分库、分表或分库+分表的场景设计的。

**注意：** Seata 事务管理器需要自行部署。

**（4）other-feature-example**

ShardingSphere-JDBC 其他功能特性的示例，目前包含了 encrypt（数据加密）、hint（强制路由）、shadow（影子库）几种类型。

① encrypt-example

数据加密功能示例，同样包含了 Java API、YAML、Spring Boot、Spring Namespace 等几种接入方式的样例。

② hint-example

强制路由功能示例，目前只提供了 YAML 配置方式的案例，更多场景欢迎补充。

③ shadow-example

影子库功能示例，包含了影子库特性与数据加密、分库分表、读写分离等特性结合的应用样例。

**（5）extension-example**

本模块展示 ShardingSphere-JDBC 的自定义扩展能力，用户可以通过 SPI 或 ShardingSphere 提供的其他方式进行功能扩展。

① custom-sharding-algortihm-example

展示了如何通过 'CLASS_BASED' 方式进行自定义分片算法的扩展。

**3. shardingsphere-parser-example**

`SQLParserEngine `是 Apache ShardingSphere 定制的 SQL 解析引擎，也是 ShardingSphere-JDBC 和 ShardingSphere-Proxy 的能力基础。用户输入的 SQL 文本通过 `SQLParserEngine`  解析成可以识别的语法对象，之后才能进行路由、改写等增强操作。

从 5.0.0-alpha 版本开始，Apache ShardingSphere 将 SQL 解析这一核心能力开放给用户，用户可以通过 API 调用 `SQLParserEngine`，在自己的应用系统中进行高效的 SQL 解析，满足更多个性化的业务需要。

本模块展示了 `SQLParserEngine API` 的使用方式，覆盖了 MySQL、PostgreSQL、Oracle、SQLServer 以及 SQL92 等各种语法形式。

**4. shardingsphere-proxy-example**

ShardingSphere-Proxy 示例模块，包含了分库分表、读写分离和强制路由等常用场景的配置样例。由于 ShardingSphere-Proxy 与 ShardingSphere-JDBC 在功能特性的支持度上大体相同，未列举的示例也可以对照 `shardingsphere-jdbc-example` 进行参考。

**（1）shardingsphere-proxy-boot-mybatis-example**

展示了通过 Proxy 配置数据分片，并使用 SpringBoot + MyBatis 的方式进行数据访问的场景示例。

**（2）shardingsphere-proxy-hint-example**

展示了通过 Proxy 配置强制路由，并使用 Java 客户端进行数据访问的场景示例。

## 近期优化

在 `Apache ShardingSphere 5.0.0-beta` 版本发布之际，社区贡献者对 `shardingsphere-example` 也进行了升级和优化，主要包括：

* JDK 版本升级
* 组件版本升级
* 类命名优化
* 配置文件优化
* SQL 脚本优化

以下是升级相关的详细内容：

**JDK 版本升级**

在以 Java 作为主要语言的专业开发者中，Java 8 LTS（长期支持版本）仍然是最受欢迎的版本。


![](https://shardingsphere.apache.org/blog/img/Blog_20_img_1_a_Photo.png)

来源《JetBrains 公司 2020 关于 Java 的报告》：
https://blog.jetbrains.com/zh-hans/idea/2020/10/java-2020/

shardingsphere-example 升级以后要求 Java 8 作为最低版本。如果您当前使用的是 Java 7 或更早版本，则需要先升级 JDK。

**Spring 依赖升级**

shardingsphere-example 对 Spring 相关组件进行升级。

* spring-boot version 由 1.5.17 升级到 2.0.9.RELEASE
* springframework version 由 4.3.20.RELEASE 升级到 5.0.13.RELEASE
* mybatis-spring-boot-start version 由 1.3.0 升级到 2.0.1
* mybatis-spring version 由 1.3.0 升级到 2.0.1

**持久层框架升级**

shardingsphere-example 对持久层框架 MyBatis 和 Hibernate 进行了升级。

* mybatis version 由 3.4.2 升级到 3.5.1
* hibernate version 由 4.3.11.Final 升级到 5.2.18.Final

**数据库连接池升级**

shardingsphere-example 对数据库连接池 HikariCP 进行了升级。

* HikariCP artifactId 由 HikariCP-java7 升级到 HikariCP
* HikariCP version 由 2.4.11 升级到 3.4.2

**数据库驱动升级**

shardingsphere-example 对 MySQL 和 PostgreSQL 连接驱动进行了升级。

* mysql-connector-java version 由 5.1.42 升级到 5.1.47
* postgresql version 由 42.2.5.jre7 升级到 42.2.5

## Example 运行示例

从这里开始，我们将通过几个典型场景来说明如何配置和运行 example。

由于 shardingsphere-example 项目模块众多，本次挑选几个关注度较高的 ShardingSphere-JDBC 应用场景来举例说明。

**一、前置准备**

1.shardingsphere-example 使用 Maven 作为构建工具，请提前准备 Maven 环境；

2.准备 Apache ShardingSphere，如果你的设备中尚未安装 Apache ShardingSphere，可以按照如下方式进行下载和编译：

~~~
## 克隆 Apache ShardingSphere 项目
git clone https://github.com/apache/shardingsphere.git
## 编译源代码
cd shardingsphere
mvn clean install -Prelease,default-dep
~~~

3.将 shardingsphere-example 项目导入自己的 IDE 中；

4.准备一个可管理的数据库环境，例如本地的 MySQL 实例；

5.如需运行读写分离测试，请确保数据库的主从同步机制工作正常；

6.执行数据库初始化脚本：examples/src/resources/manual_schema.sql

**二、场景示例**

* **sharding-spring-boot-mybatis-example「分库分表场景」**

1.模块路径

examples/shardingsphere-jdbc-example/sharding-example/sharding-spring-boot-mybatis-example

2.场景目标

本示例展示 ShardingSphere-JDBC 结合 SpringBoot 和 MyBatis 进行分库分表的应用场景。此次分片的目标是 2 库 4 表，即将一张逻辑表拆分为 4 个分片，均匀保存在 2 个不同的数据库中。

3.运行准备

（1）配置 application.properties

* 将 `spring.profiles.active `设置为 `sharding-databases-tables`

（2）配置 application-sharding-databases-tables.properties

* 将 `jdbc-url` 修改为自己的数据库地址，并配置正确的用户名密码等信息
* 将 `spring.shardingsphere.props.sql-show` 属性设置为 true

详细配置说明请阅读配置手册：
https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/configuration/spring-boot-starter/sharding/

4.启动运行

运行启动类： **ShardingSpringBootMybatisExample.java**

此时即可通过日志中的「Logic SQL」和「Actual SQL」观察每一条 SQL 语句的路由情况，理解分库分表的运行机制。

* **sharding-raw-jdbc-example「读写分离场景」**

1.模块路径

examples/shardingsphere-jdbc-example/sharding-example/sharding-raw-jdbc-example

2.场景目标

本示例展示如何使用 YAML 配置 ShardingSphere-JDBC 的读写分离功能。此次演示的场景是一个写库 + 两个读库的分离配置。

3.运行准备

配置 META-INF/readwrite-splitting.yaml

* 将 `jdbc-url` 修改为自己的数据库地址，并配置正确的用户名密码等信息

* 将 `props.sql-show` 属性设置为 `true`

详细配置说明请阅读配置手册：
https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/configuration/yaml/readwrite-splitting-/

4.启动运行

打开启动类：`ShardingRawYamlConfigurationExample.java`，将 `shardingType` 设置为 `ShardingType.READWRITE_SPLITTING`，并启动运行。

此时即可通过日志中的「Logic SQL」和「Actual SQL」观察每一条 SQL 语句的路由情况，理解读写分离的运行机制。

**注意：** 如果主从数据库无法正常同步，将会发生查询异常。

* **custom-sharding-algortihm-example「自定义算法场景」**

1.模块路径：

examples/shardingsphere-jdbc-example/extension-example/custom-sharding-algortihm-example/class-based-sharding-algorithm-example

2.场景目标

本示例展示如何使用 `CLASS_BASED` 方式进行自定义算法扩展，让 ShardingSphere-JDBC 在进行分片路由时，使用用户提供的算法来计算分片结果。此次演示的场景是使用自定义分片算法进行分库。

3.运行准备

（1）准备一个自定义的分片算法，该算法应根据应用需要，实现`StandardShardingAlgorithm`、`ComplexKeysShardingAlgorithm` 或 `HintShardingAlgorithm` 其中一个接口，如示例中提供的 `ClassBasedStandardShardingAlgorithmFixture`

（2）配置 META-INF/sharding-databases.yaml

* 将 jdbc-url 修改为自己的数据库地址，并配置正确的用户名密码等信息
* 将 props.sql-show 属性设置为 true
* 注意 shardingAlgorithms 配置项，当 type 为 CLASS_BASED 时，可以通过 props 指定自定义算法的类别和全路径，这样就可以完成自定义算法的配置。

详细配置说明请阅读配置手册：
https://shardingsphere.apache.org/document/current/cn/user-manual/common-config/builtin-algorithm/sharding/

4.启动运行

运行启动类：

`YamlClassBasedShardingAlgorithmExample.java`

此时即可通过日志观察分库运行情况，并可以通过 DEBUG 等方式检查自定义算法的输入输出是否符合预期。

## 结语

以上就是本次知识库分享的全部内容。有关 ShardingSphere-JDBC、ShardingSphere-Proxy 和 ShardingSphere-Parser 的示例将在未来继续为大家分享。如果读者有感兴趣的场景需求，或是发现了新的问题、新的提升点，欢迎在 GitHub issue 列表提出建议，也可提交 Pull Request 参与到开源社区，为世界级的项目贡献力量。

**GitHub issue：**
https://github.com/apache/shardingsphere/issues

**贡献指南：**
https://shardingsphere.apache.org/community/cn/involved/
