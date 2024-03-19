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
适用于 `Oracle GraalVM`， `Liberica Native Image Kit` 和 `Mandrel` 等 `GraalVM CE` 的下游发行版。

- GraalVM CE 23.1.2 For JDK 21.0.2，对应于 SDKMAN! 的 `21.0.2-graalce`

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
                <version>0.10.1</version>
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
由于 Gradle 8.6 的限制，用户需要通过 Maven 依赖的形式引入 Metadata Repository 的 JSON 文件。
参考 https://github.com/graalvm/native-build-tools/issues/572 。

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.10.1'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.1', classifier: 'repository', ext: 'zip')
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
  dataSources:
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

5. 当使用 Seata 的 BASE 集成时，用户需要使用特定的 `io.seata:seata-all:1.8.0` 版本以避开对 ByteBuddy Java API 的使用，
并排除 `io.seata:seata-all:1.8.0` 中过时的 `org.antlr:antlr4-runtime:4.8` 的 Maven 依赖。可能的配置例子如下，

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
         <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
         <version>${shardingsphere.version}</version>
      </dependency>
      <dependency>
         <groupId>io.seata</groupId>
         <artifactId>seata-all</artifactId>
         <version>1.8.0</version>
         <exclusions>
            <exclusion>
               <groupId>org.antlr</groupId>
               <artifactId>antlr4-runtime</artifactId>
            </exclusion>
         </exclusions>
      </dependency>
    </dependencies>
</project>
```

## 贡献 GraalVM Reachability Metadata

ShardingSphere 对在 GraalVM Native Image 下的可用性的验证，是通过 GraalVM Native Build Tools 的 Maven Plugin 子项目来完成的。
通过在 JVM 下运行单元测试，为单元测试打上 `junit-platform-unique-ids*` 标签，此后构建为 GraalVM Native Image 进行 nativeTest 来测试
在 GraalVM Native Image 下的单元测试覆盖率。请贡献者不要使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 `test listener` mode 下
failed to discover tests 的测试库。

ShardingSphere 定义了 `shardingsphere-test-native` 的 Maven Module 用于为 native Test 提供小型的单元测试子集，
此单元测试子集避免了使用 Mockito 等 native Test 下无法使用的第三方库。

ShardingSphere 定义了 `nativeTestInShardingSphere` 的 Maven Profile 用于为 `shardingsphere-test-native` 模块执行 nativeTest 。

假设贡献者处于新的 Ubuntu 22.04.3 LTS 实例下，其可通过如下 bash 命令通过 SDKMAN! 管理 JDK 和工具链，
并为 `shardingsphere-test-native` 子模块执行 nativeTest。

你必须安装 Docker Engine 以执行 `testcontainers-java` 相关的单元测试。

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.2-graalce
sdk use java 21.0.2-graalce
sudo apt-get install build-essential libz-dev zlib1g-dev -y

git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PnativeTestInShardingSphere -T1C -e clean test
```

当贡献者发现缺少与 ShardingSphere 无关的第三方库的 GraalVM Reachability Metadata 时，应当在
https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue， 并提交包含依赖的第三方库缺失的 GraalVM Reachability
Metadata 的 PR。ShardingSphere 在 `shardingsphere-infra-reachability-metadata` 子模块主动托管了部分第三方库的 GraalVM Reachability Metadata。

如果 nativeTest 执行失败， 应为单元测试生成初步的 GraalVM Reachability Metadata，并手动调整以修复 nativeTest。
如有需要，请使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解或 `org.graalvm.nativeimage.imagecode` 的
System Property 屏蔽部分单元测试在 GraalVM Native Image 下运行。

ShardingSphere 定义了 `generateMetadata` 的 Maven Profile 用于在 GraalVM JIT Compiler 下携带 GraalVM Tracing Agent 执行单元测试，并在特定目录下生成或合并
已有的 GraalVM Reachability Metadata 文件。可通过如下 bash 命令简单处理此流程。贡献者仍可能需要手动调整具体的 JSON 条目，并在适当的时候
调整 Maven Profile 和 GraalVM Tracing Agent 的 Filter 链。

以下命令仅为 `shardingsphere-test-native` 生成 Conditional 形态的 GraalVM Reachability Metadata 的一个举例。生成的 GraalVM
Reachability Metadata 位于 `shardingsphere-infra-reachability-metadata` 子模块下。

对于测试类和测试文件独立使用的 GraalVM Reachability Metadata，贡献者应该放置到
`${user.dir}/test/natived/src/test/resources/META-INF/native-image/shardingsphere-test-native-test-metadata/`
文件夹下。`${}` 内为相关子模块对应的 POM 4.0 的常规系统变量，自行替换。

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```

请手动删除无任何具体条目的 JSON 文件。
