+++
title = "GraalVM Native Image"
weight = 8
chapter = true
+++

## Background Information

ShardingSphere JDBC has been validated for availability under GraalVM Native Image.

Build GraalVM Native containing Maven dependencies of `org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}`
Image, you need to resort to GraalVM Native Build Tools. GraalVM Native Build Tools provides Maven Plugin and Gradle Plugin 
to simplify long list of shell commands for GraalVM CE's `native-image` command line tool.

ShardingSphere JDBC requires GraalVM Native Image to be built with GraalVM CE as follows or higher. Users can quickly switch
JDK through `SDKMAN!`. Same reason applicable to downstream distributions of `GraalVM CE` such as https://sdkman.io/jdks#graal ,
https://sdkman.io/jdks#nik and https://sdkman.io/jdks#mandrel .

- GraalVM CE For JDK 22.0.2, corresponding to `22.0.2-graalce` of SDKMAN!

Users can still use the old versions of GraalVM CE such as `21.0.2-graalce` on SDKMAN! to build the GraalVM Native Image product of ShardingSphere. 
However, this will cause the failure of building the GraalVM Native Image when integrating some third-party dependencies. 
A typical example is related to the `org.apache.hive:hive-jdbc:4.0.0` HiveServer2 JDBC Driver, which uses AWT-related classes. 
GraalVM CE only supports AWT for GraalVM CE For JDK22 and higher versions.

```shell
com.sun.beans.introspect.ClassInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.ClassInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.ClassInfo
java.beans.Introspector was unintentionally initialized at build time. To see why java.beans.Introspector got initialized use --trace-class-initialization=java.beans.Introspector
com.sun.beans.util.Cache$Kind$2 was unintentionally initialized at build time. To see why com.sun.beans.util.Cache$Kind$2 got initialized use --trace-class-initialization=com.sun.beans.util.Cache$Kind$2
com.sun.beans.TypeResolver was unintentionally initialized at build time. To see why com.sun.beans.TypeResolver got initialized use --trace-class-initialization=com.sun.beans.TypeResolver
java.beans.ThreadGroupContext was unintentionally initialized at build time. To see why java.beans.ThreadGroupContext got initialized use --trace-class-initialization=java.beans.ThreadGroupContext
com.sun.beans.util.Cache$Kind was unintentionally initialized at build time. To see why com.sun.beans.util.Cache$Kind got initialized use --trace-class-initialization=com.sun.beans.util.Cache$Kind
com.sun.beans.introspect.MethodInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.MethodInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.MethodInfo
com.sun.beans.util.Cache$Kind$1 was unintentionally initialized at build time. To see why com.sun.beans.util.Cache$Kind$1 got initialized use --trace-class-initialization=com.sun.beans.util.Cache$Kind$1
com.sun.beans.util.Cache$Kind$3 was unintentionally initialized at build time. To see why com.sun.beans.util.Cache$Kind$3 got initialized use --trace-class-initialization=com.sun.beans.util.Cache$Kind$3
```

### Maven Ecology

Users need to actively use the GraalVM Reachability Metadata central repository. 
The following configuration is for reference to configure additional Maven Profiles for the project, 
and the documentation of GraalVM Native Build Tools shall prevail.

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
                 <version>0.10.2</version>
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

### Gradle Ecosystem

Users need to actively use the GraalVM Reachability Metadata central repository.
The following configuration is for reference to configure additional Gradle Tasks for the project,
and the documentation of GraalVM Native Build Tools shall prevail.
Due to the limitations of Gradle 8.6, 
users need to introduce the JSON file of Metadata Repository through Maven dependency. 
Reference https://github.com/graalvm/native-build-tools/issues/572 .

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.10.2'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.2', classifier: 'repository', ext: 'zip')
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

### For build tools such as sbt that are not supported by GraalVM Native Build Tools

Such requirements require opening additional issues at https://github.com/graalvm/native-build-tools 
and providing the Plugin implementation of the corresponding build tool.

## Usage restrictions

1. The following algorithm classes are not available under GraalVM Native Image due to the involvement of https://github.com/oracle/graal/issues/5522.
    - `org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`
    - `org.apache.shardingsphere.sharding.algorithm.sharding.inline.ComplexInlineShardingAlgorithm`
    - `org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`

For general cases, users can simulate the behavior of GroovyShell by themselves through the `CLASS_BASE` algorithm. For example, 
take the following configuration.

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

You can first define the implementation class of `CLASS_BASE`.

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

Modify the relevant YAML configuration as follows.

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

Add the following content to `src/main/resources/META-INF/native-image/exmaple-test-metadata/reflect-config.json` to used 
normally under GraalVM Native Image.

```json
[
{
   "name":"org.example.test.TestShardingAlgorithmFixture",
   "methods":[{"name":"<init>","parameterTypes":[] }]
}
]
```

2. For the `ReadWrite Splitting` feature, you need to use other implementations of `Row Value Expressions` SPI to configure 
`logic database name`, `writeDataSourceName` and `readDataSourceNames` when bypassing calls to GroovyShell. 
One possible configuration is to use the `Row Value Expressions` SPI implementation of `LITERAL`.

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

The same applies to `actualDataNodes` for the `Sharding` feature.

```yaml
- !SHARDING
   tables:
      t_order:
         actualDataNodes: <LITERAL>ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1
         keyGenerateStrategy:
            column: order_id
            keyGeneratorName: snowflake
```

3. Users still need to configure GraalVM Reachability Metadata for independent files in the `src/main/resources/META-INF/native-image` 
folder or `src/test/resources/META-INF/native-image` folder. Users can quickly collect GraalVM Reachability Metadata through 
the GraalVM Tracing Agent of GraalVM Native Build Tools. 

4. Maven modules such as `com.microsoft.sqlserver:mssql-jdbc`, represented by the JDBC Driver of MS SQL Server,
will dynamically load different character sets based on the encoding used in the database, which is unpredictable behavior. 
When encountering the following Error, users need to add the `buildArg` of `-H:+AddAllCharsets` to the configuration of GraalVM Native Build Tools.

```shell
Caused by: java.io.UnsupportedEncodingException: Codepage Cp1252 is not supported by the Java environment.
 com.microsoft.sqlserver.jdbc.Encoding.checkSupported(SQLCollation.java:572)
 com.microsoft.sqlserver.jdbc.SQLCollation$SortOrder.getEncoding(SQLCollation.java:473)
 com.microsoft.sqlserver.jdbc.SQLCollation.encodingFromSortId(SQLCollation.java:501)
 [...]
```

5. To discuss the steps required to use XA distributed transactions under the GraalVM Native Image of ShardingSphere JDBC, 
additional known prerequisites need to be introduced,
   - `org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper#loadXADataSource(String)` will instantiate the `javax.sql.XADataSource` implementation class of each database driver through `java.lang.Class#getDeclaredConstructors`.
   - The full class name of the `javax.sql.XADataSource` implementation class of each database driver is stored in the metadata of ShardingSphere by implementing the SPI of `org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition`.

In the GraalVM Native Image, this actually requires the definition of the GraalVM Reachability Metadata of the third-party dependencies,
while ShardingSphere itself only provides the corresponding GraalVM Reachability Metadata for `com.h2database:h2`.

GraalVM Reachability Metadata of other database drivers such as `com.mysql:mysql-connector-j` should be defined by themselves,
or the corresponding JSON should be submitted to https://github.com/oracle/graalvm-reachability-metadata .

Take the `com.mysql.cj.jdbc.MysqlXADataSource` class of `com.mysql:mysql-connector-j:9.0.0` as an example,
which is the implementation of `javax.sql.XADataSource` of MySQL JDBC Driver.
Users need to define the following JSON in the `reflect-config.json` file in the `/META-INF/native-image/com.mysql/mysql-connector-j/9.0.0/` folder of their own project's claapath,
to define the constructor of `com.mysql.cj.jdbc.MysqlXADataSource` inside the GraalVM Native Image.

```json
[
{
"condition":{"typeReachable":"com.mysql.cj.jdbc.MysqlXADataSource"},
"name":"com.mysql.cj.jdbc.MysqlXADataSource",
"allDeclaredConstructors": true
}
]
```

6. When using Seata's BASE integration, 
users need to use a specific `io.seata:seata-all:1.8.0` version to avoid using the ByteBuddy Java API,
and exclude the outdated Maven dependency of `org.antlr:antlr4-runtime:4.8` in `io.seata:seata-all:1.8.0`.
Possible configuration examples are as follows,

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
            <exclusion>
               <groupId>commons-lang</groupId>
               <artifactId>commons-lang</artifactId>
            </exclusion>
            <exclusion>
               <groupId>org.apache.commons</groupId>
               <artifactId>commons-pool2</artifactId>
            </exclusion>
         </exclusions>
      </dependency>
    </dependencies>
</project>
```

7. When using the ClickHouse dialect through ShardingSphere JDBC, 
users need to manually introduce the relevant optional modules and the ClickHouse JDBC driver with the classifier `http`.
In principle, ShardingSphere's GraalVM Native Image integration does not want to use `com.clickhouse:clickhouse-jdbc` with classifier `all`, 
because Uber Jar will cause the collection of duplicate GraalVM Reachability Metadata.
Possible configuration examples are as follows,
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
ClickHouse does not support local transactions, XA transactions, and Seata AT mode transactions at the ShardingSphere integration level. 
More discussion is at https://github.com/ClickHouse/clickhouse-docs/issues/2300 .

## Contribute GraalVM Reachability Metadata

The verification of ShardingSphere's availability under GraalVM Native Image is completed through the Maven Plugin subproject 
of GraalVM Native Build Tools. By running the unit test under the JVM, label the unit test with `junit-platform-unique-ids*`, 
and then build it as GraalVM Native Image for nativeTest to test Unit Test Coverage under GraalVM Native Image. 
Please do not use `io.kotest:kotest-runner-junit5-jvm:5.5.4` and some third-party test libraries, they are in `test listener` 
mode failed to discover tests.

ShardingSphere defines the Maven Module of `shardingsphere-test-native` to provide a small subset of unit tests for native Test.
This subset of unit tests avoids the use of third-party libraries such as Mockito that are not available under native Test.

ShardingSphere defines the Maven Profile of `nativeTestInShardingSphere` for executing nativeTest for the `shardingsphere-test-native` module.

Assuming that the contributor is under a new Ubuntu 22.04.3 LTS instance, Contributors can manage the JDK and tool chain through 
`SDKMAN!` through the following bash command, and execute nativeTest for the `shardingsphere-test-native` submodule.

You must install Docker Engine to execute `testcontainers-java` related unit tests.

```bash
sudo apt install unzip zip curl sed -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.0.2-graalce
sdk use java 22.0.2-graalce
sudo apt-get install build-essential zlib1g-dev -y

git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PnativeTestInShardingSphere -T1C -e clean test
```

When contributors discover that GraalVM Reachability Metadata is missing for a third-party library not related to ShardingSphere, 
they should open a new issue and submit PR containing the missing third-party library GraalVM Reachability Metadata that depends 
on https://github.com/oracle/graalvm-reachability-metadata . ShardingSphere actively hosts the GraalVM Reachability Metadata of 
some third-party libraries in the `shardingsphere-infra-reachability-metadata` submodule.

If nativeTest execution fails, preliminary GraalVM Reachability Metadata should be generated for unit tests,
and manually adjust the contents of the `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata` folder on the classpath of the `shardingsphere-infra-reachability-metadata` submodule to fix nativeTest.
If necessary, 
use the `org.junit.jupiter.api.condition.DisabledInNativeImage` annotation or the `org.graalvm.nativeimage.imagecode` System Property blocks some unit tests from running under GraalVM Native Image.

ShardingSphere defines the Maven Profile of `generateMetadata` to carry the GraalVM Tracing Agent under the GraalVM JIT Compiler to perform unit testing,
and generate or overwrite existing GraalVM Reachability Metadata files  in the `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` folder of the classpath of the `shardingsphere-infra-reachability-metadata` submodule. 
This process can be easily handled with the following bash command.
Contributors may still need to manually adjust specific JSON entries and adjust the Filter chain of Maven Profile and GraalVM Tracing Agent as appropriate.
For the `shardingsphere-infra-reachability-metadata` submodule,
manually added, deleted, and changed JSON entries should be located in the `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` folder,
entries in `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` should only be generated by the Maven Profile of `generateMetadata`.

The following command is only an example of using `shardingsphere-test-native` to generate GraalVM Reachability Metadata in Conditional form. 
Generated GraalVM Reachability Metadata is located under the `shardingsphere-infra-reachability-metadata` submodule.

For GraalVM Reachability Metadata to be used independently by test classes and test files, 
contributors should place it on the classpath of the `shardingsphere-test-native` submodule `META-INF/native-image/shardingsphere-test-native-test-metadata/`.

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -DskipNativeTests -e -T1C clean test native:metadata-copy
```
