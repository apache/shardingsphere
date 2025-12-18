+++
title = "GraalVM Native Image"
weight = 8
chapter = true
+++

## 背景信息

ShardingSphere JDBC 已在 GraalVM Native Image 下完成可用性验证。

构建包含 `org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}` 的 Maven 依赖的 GraalVM Native 
Image，你需要借助于 GraalVM Native Build Tools。GraalVM Native Build Tools 提供了 Maven Plugin 和 Gradle Plugin 来简化 GraalVM
CE 的 `native-image` 命令行工具的长篇大论的 shell 命令。

ShardingSphere JDBC 要求在如下或更高版本的 `GraalVM CE` 完成构建 GraalVM Native Image。使用者可通过 SDKMAN! 快速切换 JDK。这同理
适用于 https://sdkman.io/jdks#graal ， https://sdkman.io/jdks#nik 和 https://sdkman.io/jdks#mandrel 等 `GraalVM CE` 的下游发行版。

- GraalVM CE For JDK 24.0.2，对应于 SDKMAN! 的 `24.0.2-graalce`

用户依然可以使用 SDKMAN! 上的 `21.0.8-graal` 等旧版本的 Oracle GraalVM 来构建 ShardingSphere 的 GraalVM Native Image 产物。
但这将导致集成部分第三方依赖时，构建 GraalVM Native Image 失败。
分类讨论，

1. 开发者正在使用 HiveServer2 JDBC Driver 相关的 `org.apache.hive:hive-jdbc:4.0.1`。由于 HiveServer2 JDBC Driver 使用了 AWT 相关的类，
   且 `GraalVM CE` 对 AWT 相关类的支持仅位于 GraalVM CE For JDK22 及更高版本，这将破环 GraalVM Native Image 的构建。

```shell
com.sun.beans.introspect.ClassInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.ClassInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.ClassInfo
java.beans.Introspector was unintentionally initialized at build time. To see why java.beans.Introspector got initialized use --trace-class-initialization=java.beans.Introspector
```

2. 开发者使用的旧版本的 `GraalVM CE` 或 `GraalVM CE` 的下游发行版未包含 https://github.com/graalvm/graalvm-community-jdk21u/pull/23 的向后移植补丁。
   此情况下开发者需要自行编写更多的可被旧版本 `GraalVM CE` 识别的，GraalVM Reachability Metadata 相关的 JSON。

### Maven 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Maven Profiles，以 GraalVM Native Build Tools 的文档为准。

```xml
<project>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc</artifactId>
            <version>${shardingsphere.version}</version>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.graalvm.buildtools</groupId>
                <artifactId>native-maven-plugin</artifactId>
                <version>0.11.3</version>
                <extensions>true</extensions>
                <configuration>
                    <buildArgs>
                        <buildArg>-H:+UnlockExperimentalVMOptions</buildArg>
                        <buildArg>-H:+AddAllCharsets</buildArg>
                        <buildArg>-H:+IncludeAllLocales</buildArg>
                    </buildArgs>
                </configuration>
                <executions>
                    <execution>
                        <id>build-native</id>
                        <goals>
                            <goal>compile-no-fork</goal>
                        </goals>
                        <phase>package</phase>
                    </execution>
                    <execution>
                        <id>test-native</id>
                        <goals>
                            <goal>test</goal>
                        </goals>
                        <phase>test</phase>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Gradle 生态

使用者需要主动使用 GraalVM Reachability Metadata 中央仓库。
如下配置可供参考，以配置项目额外的 Gradle Tasks，以 GraalVM Native Build Tools 的文档为准。
由于 https://github.com/gradle/gradle/issues/17559 的限制，用户需要通过 Maven 依赖的形式引入 Metadata Repository 的 JSON 文件。
参考 https://github.com/graalvm/native-build-tools/issues/572 。

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.11.3'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.11.3', classifier: 'repository', ext: 'zip')
}

graalvmNative {
   binaries {
      main {
         buildArgs.add('-H:+UnlockExperimentalVMOptions')
         buildArgs.add('-H:+AddAllCharsets')
         buildArgs.add('-H:+IncludeAllLocales')
      }
      test {
         buildArgs.add('-H:+UnlockExperimentalVMOptions')
         buildArgs.add('-H:+AddAllCharsets')
         buildArgs.add('-H:+IncludeAllLocales')
      }
   }
   metadataRepository {
        enabled.set(false)
   }
}
```

### 对于 sbt 等不被 GraalVM Native Build Tools 支持的构建工具

此类需求需要在 https://github.com/graalvm/native-build-tools 打开额外的 issue 并提供对应构建工具的 Plugin 实现。

## 使用限制

1. 如下的算法类由于涉及到 https://github.com/oracle/graal/issues/5522 ， 暂未可在 GraalVM Native Image 下使用。
    - `org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`
    - `org.apache.shardingsphere.sharding.algorithm.sharding.inline.ComplexInlineShardingAlgorithm`
    - `org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`

对于常规案例，使用者可通过 CLASS_BASE 算法自行模拟 GroovyShell 的行为。例如对于如下配置。

```yaml
rules:
- !SHARDING
  defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: inline
  shardingAlgorithms:
    inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
        allow-range-query-with-inline-sharding: false
```

可首先定义 CLASS_BASE 的实现类。

```java
package org.example.test;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

public final class TestShardingAlgorithmFixture implements StandardShardingAlgorithm<Integer> {
    
    @Override
    public String doSharding(final Collection<String> availableTargetNames, final PreciseShardingValue<Integer> shardingValue) {
        String resultDatabaseName = "ds_" + shardingValue.getValue() % 2;
        for (String each : availableTargetNames) {
            if (each.equals(resultDatabaseName)) {
                return each;
            }
        }
        return null;
    }
    
    @Override
    public Collection<String> doSharding(final Collection<String> availableTargetNames, final RangeShardingValue<Integer> shardingValue) {
        throw new RuntimeException("This algorithm class does not support range queries.");
    }
}
```

修改相关 YAML 配置如下。

```yaml
rules:
- !SHARDING
  defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: inline
  shardingAlgorithms:
    inline:
        type: CLASS_BASED
        props:
          strategy: STANDARD
          algorithmClassName: org.example.test.TestShardingAlgorithmFixture
```

在 `src/main/resources/META-INF/native-image/exmaple-test-metadata/reachability-metadata.json` 加入如下内容即可在正常在 GraalVM Native 
Image 下使用。

```json
{
   "reflection": [
      {
         "type":"org.example.test.TestShardingAlgorithmFixture",
         "methods":[{"name":"<init>","parameterTypes":[] }]
      }
   ]
}
```

2. 对于 `读写分离` 的功能，你需要使用 `行表达式` SPI 的其他实现，以在配置 `logic database name`，`writeDataSourceName` 和 `readDataSourceNames` 
时绕开对 GroovyShell 的调用。一个可能的配置是使用 `LITERAL` 的 `行表达式` SPI 的实现。
```yaml
rules:
- !READWRITE_SPLITTING
   dataSourceGroups:
    <LITERAL>readwrite_ds:
      writeDataSourceName: <LITERAL>ds_0
      readDataSourceNames:
        - <LITERAL>ds_1
        - <LITERAL>ds_2
```

对于 `数据分片` 的功能的 `actualDataNodes` 同理。

```yaml
- !SHARDING
   tables:
      t_order:
         actualDataNodes: <LITERAL>ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1
         keyGenerateStrategy:
            column: order_id
            keyGeneratorName: snowflake
```

3. 使用者依然需要在 `src/main/resources/META-INF/native-image` 文件夹或 `src/test/resources/META-INF/native-image` 文件夹配置独立
文件的 GraalVM Reachability Metadata。使用者可通过 GraalVM Native Build Tools 的 GraalVM Tracing Agent 来快速采集 GraalVM 
Reachability Metadata。

4. 以 MS SQL Server 的 JDBC Driver 为代表的 `com.microsoft.sqlserver:mssql-jdbc` 等 Maven 模块会根据数据库中使用的编码动态加载不同的字符集，这是不可预测的行为。
当遇到如下 Error，使用者需要添加 `-H:+AddAllCharsets` 的 `buildArg` 到 GraalVM Native Build Tools 的配置中。

```shell
Caused by: java.io.UnsupportedEncodingException: Codepage Cp1252 is not supported by the Java environment.
 com.microsoft.sqlserver.jdbc.Encoding.checkSupported(SQLCollation.java:572)
 com.microsoft.sqlserver.jdbc.SQLCollation$SortOrder.getEncoding(SQLCollation.java:473)
 com.microsoft.sqlserver.jdbc.SQLCollation.encodingFromSortId(SQLCollation.java:501)
 [...]
```

5. 讨论在 ShardingSphere JDBC 的 GraalVM Native Image 下使用 XA 分布式事务的所需步骤，则需要引入额外的已知前提，
   - `org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper#loadXADataSource(String)` 会通过 `java.lang.Class#getDeclaredConstructors` 实例化各数据库驱动的 `javax.sql.XADataSource` 实现类。
   - 各数据库驱动的 `javax.sql.XADataSource` 实现类的全类名通过实现 `org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption` 的 SPI，来存入 ShardingSphere 的元数据。

在 GraalVM Native Image 内部，这实际上要求定义第三方依赖的 GraalVM Reachability Metadata，而 ShardingSphere 自身仅为 `com.h2database:h2` 提供对应的 GraalVM Reachability Metadata。
`com.mysql:mysql-connector-j` 等其他数据库驱动的 GraalVM Reachability Metadata 应自行定义，
或将对应 JSON 提交到 https://github.com/oracle/graalvm-reachability-metadata 一侧。

以 `com.mysql:mysql-connector-j:9.0.0` 的 `com.mysql.cj.jdbc.MysqlXADataSource` 类为例，这是 MySQL JDBC Driver 的 `javax.sql.XADataSource` 的实现。
用户需要在自有项目的 claapath 的 `/META-INF/native-image/com.mysql/mysql-connector-j/9.0.0/` 文件夹的 `reachability-metadata.json`文件内定义如下 JSON。

```json
{
   "reflection": [
      {
         "condition": {
            "typeReached": "com.mysql.cj.jdbc.Driver"
         },
         "type": "com.mysql.cj.jdbc.MysqlXADataSource",
         "allPublicMethods": true,
         "methods": [
            {
               "name": "<init>",
               "parameterTypes": []
            }
         ]
      }
   ]
}
```

6. ShardingSphere 的单元测试仅使用 Maven 模块 `io.github.linghengqian:hive-server2-jdbc-driver-thin` 来在 GraalVM Native Image 下验证 HiveServer2 集成的可用性。
如果开发者直接使用 `org.apache.hive:hive-jdbc`，则应自行处理依赖冲突和提供额外的 GraalVM Reachability Metadata 。

7. 由于 https://github.com/oracle/graal/issues/7979 的影响，
对应 `com.oracle.database.jdbc:ojdbc8` Maven 模块的 Oracle JDBC Driver 无法在 GraalVM Native Image 下使用。

8. 包括但不限于来自第三方依赖的 `com.mysql.cj.LocalizedErrorMessages`,
   `com.microsoft.sqlserver.jdbc.SQLServerResource`,
   `org.postgresql.translation.messages`,
   `org.opengauss.translation.messages` 等 `Resource Bundles` 在默认情况下会根据系统的默认语言环境加载 L10N 资源，
   并显示针对特定语言环境的本地化信息。
   通过 GraalVM Reachability Metadata 的 JSON 定义来穷举 `Resource Bundles` 是一件繁琐的事情。
   这有时会导致 GraalVM Native Image 在运行时中抛出类似如下的警告日志。 
   通常的操作是设置 `-H:+IncludeAllLocales` 的 `buildArg`。

```shell
com.oracle.svm.core.jdk.resources.MissingResourceRegistrationError: The program tried to access the resource at path

   com/mysql/cj/LocalizedErrorMessages_zh_Hans_CN.properties

without it being registered as reachable. Add it to the resource metadata to solve this problem. See https://www.graalvm.org/latest/reference-manual/native-image/metadata/#resources-and-resource-bundles for help
  java.base@24.0.2/java.util.ResourceBundle.getBundle(ResourceBundle.java:1261)
  com.mysql.cj.Messages.<clinit>(Messages.java:56)
  com.mysql.cj.Constants.<clinit>(Constants.java:50)
  com.mysql.cj.util.Util.<clinit>(Util.java:69)
  com.mysql.cj.conf.ConnectionUrl$Type.getImplementingInstance(ConnectionUrl.java:251)
  com.mysql.cj.conf.ConnectionUrl$Type.getConnectionUrlInstance(ConnectionUrl.java:221)
  com.mysql.cj.conf.ConnectionUrl.getConnectionUrlInstance(ConnectionUrl.java:291)
  com.mysql.cj.jdbc.NonRegisteringDriver.connect(NonRegisteringDriver.java:186)
```

9. 受 `apache/calcite` 使用的 `janino-compiler/janino` 的影响，
    ShardingSphere 的 `SQL Federation` 功能在 GraalVM Native Image 下不可用。
    这同样导致 ShardingSphere Proxy Native 无法使用 OpenGauss 集成。

10. 受 https://github.com/oracle/graal/issues/11280 影响，
    Etcd 的 Cluster 模式集成无法在通过 Windows 11 编译的 GraalVM Native Image 下使用，
    且 Etcd 的 Cluster 模式会与 GraalVM Tracing Agent 产生冲突。
    若开发者需要在通过 Linux 编译的 GraalVM Native Image 下使用 Etcd 的 Cluster 模式，
    需要自行提供额外的 GraalVM Reachability Metadata 相关的 JSON。
