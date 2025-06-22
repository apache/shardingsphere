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
A typical example is related to the `org.apache.hive:hive-jdbc:4.0.1` HiveServer2 JDBC Driver, which uses AWT-related classes. 
GraalVM CE only supports AWT for GraalVM CE For JDK22 and higher versions.

```shell
com.sun.beans.introspect.ClassInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.ClassInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.ClassInfo
java.beans.Introspector was unintentionally initialized at build time. To see why java.beans.Introspector got initialized use --trace-class-initialization=java.beans.Introspector
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
                 <version>0.10.6</version>
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
Due to the limitations of https://github.com/gradle/gradle/issues/17559 , 
users need to introduce the JSON file of Metadata Repository through Maven dependency. 
Reference https://github.com/graalvm/native-build-tools/issues/572 .

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.10.6'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.10.6', classifier: 'repository', ext: 'zip')
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
   "allPublicMethods": true,
   "methods": [{"name":"<init>","parameterTypes":[] }]
}
]
```

6. When using the ClickHouse dialect through ShardingSphere JDBC, 
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

7. ShardingSphere's unit test only uses the Maven module `io.github.linghengqian:hive-server2-jdbc-driver-thin` to verify the availability of HiveServer2 integration under GraalVM Native Image. 
If developers use `org.apache.hive:hive-jdbc` directly, they should handle dependency conflicts and provide additional GraalVM Reachability Metadata by themselves.

8. Due to https://github.com/oracle/graal/issues/7979 , 
the Oracle JDBC Driver corresponding to the `com.oracle.database.jdbc:ojdbc8` Maven module cannot be used under GraalVM Native Image.

9. Due to https://github.com/apache/doris/issues/9426, when connecting to Apache Doris FE via Shardinghere JDBC,
   users need to provide GraalVM Reachability Metadata related to the `apache/doris` integration module.
