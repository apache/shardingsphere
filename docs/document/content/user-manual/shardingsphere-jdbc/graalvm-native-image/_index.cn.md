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

- GraalVM CE For JDK 22.0.2，对应于 SDKMAN! 的 `22.0.2-graalce`

用户依然可以使用 SDKMAN! 上的 `21.0.2-graalce` 等旧版本的 GraalVM CE 来构建 ShardingSphere 的 GraalVM Native Image 产物。
但这将导致集成部分第三方依赖时，构建 GraalVM Native Image 失败。
典型的例子来自 HiveServer2 JDBC Driver 相关的 `org.apache.hive:hive-jdbc:4.0.0`，HiveServer2 JDBC Driver 使用了 AWT 相关的类，
而 GraalVM CE 对 `java.beans.**` package 的支持仅位于 GraalVM CE For JDK22 及更高版本。

```shell
com.sun.beans.introspect.ClassInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.ClassInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.ClassInfo
java.beans.Introspector was unintentionally initialized at build time. To see why java.beans.Introspector got initialized use --trace-class-initialization=java.beans.Introspector
```

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
                <version>0.10.5</version>
                <extensions>true</extensions>
                <configuration>
                    <buildArgs>
                        <buildArg>-H:+AddAllCharsets</buildArg>
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
   id 'org.graalvm.buildtools.native' version '0.10.5'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.5', classifier: 'repository', ext: 'zip')
}

graalvmNative {
   binaries {
      main {
         buildArgs.add('-H:+AddAllCharsets')
      }
      test {
         buildArgs.add('-H:+AddAllCharsets')
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

在 `src/main/resources/META-INF/native-image/exmaple-test-metadata/reflect-config.json` 加入如下内容即可在正常在 GraalVM Native 
Image 下使用。

```json
[
{
  "name":"org.example.test.TestShardingAlgorithmFixture",
  "methods":[{"name":"<init>","parameterTypes":[] }]
}
]
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
   - 各数据库驱动的 `javax.sql.XADataSource` 实现类的全类名通过实现 `org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition` 的 SPI，来存入 ShardingSphere 的元数据。

在 GraalVM Native Image 内部，这实际上要求定义第三方依赖的 GraalVM Reachability Metadata，而 ShardingSphere 自身仅为 `com.h2database:h2` 提供对应的 GraalVM Reachability Metadata。
`com.mysql:mysql-connector-j` 等其他数据库驱动的 GraalVM Reachability Metadata 应自行定义，
或将对应 JSON 提交到 https://github.com/oracle/graalvm-reachability-metadata 一侧。

以 `com.mysql:mysql-connector-j:9.0.0` 的 `com.mysql.cj.jdbc.MysqlXADataSource` 类为例，这是 MySQL JDBC Driver 的 `javax.sql.XADataSource` 的实现。
用户需要在自有项目的 claapath 的 `/META-INF/native-image/com.mysql/mysql-connector-j/9.0.0/` 文件夹的 `reflect-config.json`文件内定义如下 JSON，
以在 GraalVM Native Image 内部定义 `com.mysql.cj.jdbc.MysqlXADataSource` 的构造函数。

```json
[
{
   "condition":{"typeReachable":"com.mysql.cj.jdbc.MysqlXADataSource"},
   "name":"com.mysql.cj.jdbc.MysqlXADataSource",
   "allPublicMethods": true,
   "methods": [{"name":"<init>","parameterTypes":[] }]
}
]
```

6. 当需要通过 ShardingSphere JDBC 使用 ClickHouse 方言时，
用户需要手动引入相关的可选模块和 classifier 为 `http` 的 ClickHouse JDBC 驱动。
原则上，ShardingSphere 的 GraalVM Native Image 集成不希望使用 classifier 为 `all` 的 `com.clickhouse:clickhouse-jdbc`，
因为 Uber Jar 会导致采集重复的 GraalVM Reachability Metadata。
可能的配置例子如下，

```xml
<project>
    <dependencies>
      <dependency>
         <groupId>org.apache.shardingsphere</groupId>
         <artifactId>shardingsphere-jdbc</artifactId>
         <version>${shardingsphere.version}</version>
      </dependency>
       <dependency>
          <groupId>org.apache.shardingsphere</groupId>
          <artifactId>shardingsphere-parser-sql-clickhouse</artifactId>
          <version>${shardingsphere.version}</version>
      </dependency>
       <dependency>
          <groupId>com.clickhouse</groupId>
          <artifactId>clickhouse-jdbc</artifactId>
          <version>0.6.3</version>
          <classifier>http</classifier>
       </dependency>
    </dependencies>
</project>
```

7. 受 https://github.com/grpc/grpc-java/issues/10601 影响，用户如果在项目中引入了 `org.apache.hive:hive-jdbc`，
则需要在项目的 classpath 的 `META-INF/native-image/io.grpc/grpc-netty-shaded` 文件夹下创建包含如下内容的文件 `native-image.properties`，

```properties
Args=--initialize-at-run-time=\
    io.grpc.netty.shaded.io.netty.channel.ChannelHandlerMask,\
    io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel,\
    io.grpc.netty.shaded.io.netty.channel.socket.nio.SelectorProviderUtil,\
    io.grpc.netty.shaded.io.netty.util.concurrent.DefaultPromise,\
    io.grpc.netty.shaded.io.netty.util.internal.MacAddressUtil,\
    io.grpc.netty.shaded.io.netty.util.internal.SystemPropertyUtil,\
    io.grpc.netty.shaded.io.netty.util.NetUtilInitializations,\
    io.grpc.netty.shaded.io.netty.channel.AbstractChannel,\
    io.grpc.netty.shaded.io.netty.util.NetUtil,\
    io.grpc.netty.shaded.io.netty.util.internal.PlatformDependent,\
    io.grpc.netty.shaded.io.netty.util.internal.PlatformDependent0,\
    io.grpc.netty.shaded.io.netty.channel.DefaultChannelPipeline,\
    io.grpc.netty.shaded.io.netty.channel.DefaultChannelId,\
    io.grpc.netty.shaded.io.netty.util.ResourceLeakDetector,\
    io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext,\
    io.grpc.netty.shaded.io.netty.channel.ChannelOutboundBuffer,\
    io.grpc.netty.shaded.io.netty.util.internal.InternalThreadLocalMap,\
    io.grpc.netty.shaded.io.netty.util.internal.CleanerJava9,\
    io.grpc.netty.shaded.io.netty.util.internal.StringUtil,\
    io.grpc.netty.shaded.io.netty.util.internal.CleanerJava6,\
    io.grpc.netty.shaded.io.netty.buffer.ByteBufUtil$HexUtil,\
    io.grpc.netty.shaded.io.netty.buffer.AbstractByteBufAllocator,\
    io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalThread,\
    io.grpc.netty.shaded.io.netty.buffer.PoolArena,\
    io.grpc.netty.shaded.io.netty.buffer.EmptyByteBuf,\
    io.grpc.netty.shaded.io.netty.buffer.PoolThreadCache,\
    io.grpc.netty.shaded.io.netty.util.AttributeKey
```

ShardingSphere 的单元测试仅使用 Maven 模块 `io.github.linghengqian:hive-server2-jdbc-driver-thin` 来在 GraalVM Native Image 下验证可用性。

8. 由于 https://github.com/oracle/graal/issues/7979 的影响，
对应 `com.oracle.database.jdbc:ojdbc8` Maven 模块的 Oracle JDBC Driver 无法在 GraalVM Native Image 下使用。

9. 由于 https://github.com/apache/doris/issues/9426 的影响，当通过 Shardinghere JDBC 连接至 Apache Doris FE，
用户需自行提供 `apache/doris` 集成模块相关的 GraalVM Reachability Metadata。

10. 由于 https://github.com/prestodb/presto/issues/23226 的影响，当通过 Shardinghere JDBC 连接至 Presto Server，
用户需自行提供 `com.facebook.presto:presto-jdbc` 和 `prestodb/presto` 集成模块相关的 GraalVM Reachability Metadata。

## 贡献 GraalVM Reachability Metadata

ShardingSphere 对在 GraalVM Native Image 下的可用性的验证，是通过 GraalVM Native Build Tools 的 Maven Plugin 子项目来完成的。
通过在 JVM 下运行单元测试，为单元测试打上 `junit-platform-unique-ids*` 标签，此后构建为 GraalVM Native Image 进行 nativeTest 来测试
在 GraalVM Native Image 下的单元测试覆盖率。请贡献者不要使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 `test listener` mode 下
failed to discover tests 的测试库。

ShardingSphere 定义了 `shardingsphere-test-native` 的 Maven Module 用于为 native Test 提供小型的单元测试子集，
此单元测试子集避免了使用 Mockito 等 native Test 下无法使用的第三方库。

ShardingSphere 定义了 `nativeTestInShardingSphere` 的 Maven Profile 用于为 `shardingsphere-test-native` 模块执行 nativeTest 。

贡献者必须安装 Docker Engine 以执行 `testcontainers-java` 相关的单元测试，以 https://java.testcontainers.org/supported_docker_environment/ 为准。

假设贡献者处于新的 Ubuntu 22.04.4 LTS 实例下，其可通过如下 bash 命令通过 SDKMAN! 管理 JDK 和工具链，
并为 `shardingsphere-test-native` 子模块执行 nativeTest。

```bash
sudo apt install unzip zip -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.0.2-graalce
sdk use java 22.0.2-graalce
sudo apt-get install build-essential zlib1g-dev -y

git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PnativeTestInShardingSphere -e -T 1C clean test
```

当贡献者发现缺少与 ShardingSphere 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability
Metadata 的 PR。ShardingSphere 在 `shardingsphere-infra-reachability-metadata` 子模块主动托管了部分第三方库的 GraalVM Reachability Metadata。

如果 nativeTest 执行失败， 应为单元测试生成初步的 GraalVM Reachability Metadata，
并手动调整 `shardingsphere-infra-reachability-metadata` 子模块的 classpath 的 `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` 文件夹下的内容以修复 nativeTest。
如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解或 `org.graalvm.nativeimage.imagecode` 的
System Property 屏蔽部分单元测试在 GraalVM Native Image 下运行。

ShardingSphere 定义了 `generateMetadata` 的 Maven Profile 用于在 GraalVM JIT Compiler 下携带 GraalVM Tracing Agent 执行单元测试，
并在 `shardingsphere-infra-reachability-metadata` 子模块的 classpath 的 `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` 文件夹下，
生成或覆盖已有的 GraalVM Reachability Metadata 文件。可通过如下 bash 命令简单处理此流程。
贡献者仍可能需要手动调整具体的 JSON 条目，并适时调整 Maven Profile 和 GraalVM Tracing Agent 的 Filter 链。
针对 `shardingsphere-infra-reachability-metadata` 子模块，
手动增删改动的 JSON 条目应位于 `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` 文件夹下，
而 `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` 中的条目仅应由 `generateMetadata` 的 Maven Profile 生成。

以下命令仅为 `shardingsphere-test-native` 生成 Conditional 形态的 GraalVM Reachability Metadata 的一个举例。生成的 GraalVM
Reachability Metadata 位于 `shardingsphere-infra-reachability-metadata` 子模块下。

对于测试类和测试文件独立使用的 GraalVM Reachability Metadata，贡献者应该放置到 `shardingsphere-test-native` 子模块的 classpath 的
`META-INF/native-image/shardingsphere-test-native-test-metadata/` 下。

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -e -T 1C clean test native:metadata-copy
```

受 https://github.com/apache/shardingsphere/issues/33206 影响，
贡献者执行 `./mvnw -PgenerateMetadata -T 1C -e clean test native:metadata-copy` 后，
`infra/reachability-metadata/src/main/resources/META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/resource-config.json` 会生成不必要的包含绝对路径的 JSON 条目，
类似如下，

```json
{
   "resources":{
      "includes":[{
         "condition":{"typeReachable":"org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"},
         "pattern":"\\Qhome/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/postgresql//global.yaml\\E"
      }, {
         "condition":{"typeReachable":"org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"},
         "pattern":"\\Qhome/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/postgresql/\\E"
      }, {
         "condition":{"typeReachable":"org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"},
         "pattern":"\\Qhome/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/features/sharding//global.yaml\\E"
      }, {
         "condition":{"typeReachable":"org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"},
         "pattern":"\\Qhome/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/features/sharding/\\E"
      }]},
   "bundles":[]
}
```

贡献者应始终手动删除这些包含绝对路径的 JSON 条目，并等待 https://github.com/oracle/graal/issues/8417 被解决。
