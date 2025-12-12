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

- GraalVM CE For JDK 24.0.2, corresponding to `24.0.2-graalce` of SDKMAN!

Users can still use old versions of Oracle GraalVM such as `21.0.8-graal` on SDKMAN! to build ShardingSphere's GraalVM Native Image product.
But this will cause the failure of building GraalVM Native Image when integrating some third-party dependencies.
Classification discussion,
1. Developers are using `org.apache.hive:hive-jdbc:4.0.1` related to HiveServer2 JDBC Driver. Since HiveServer2 JDBC Driver uses AWT-related classes,
   and `GraalVM CE`'s support for AWT-related classes is only in GraalVM CE For JDK22 and higher, this will destroy the construction of GraalVM Native Image.

```shell
com.sun.beans.introspect.ClassInfo was unintentionally initialized at build time. To see why com.sun.beans.introspect.ClassInfo got initialized use --trace-class-initialization=com.sun.beans.introspect.ClassInfo
java.beans.Introspector was unintentionally initialized at build time. To see why java.beans.Introspector got initialized use --trace-class-initialization=java.beans.Introspector
```

2. The developer is using an old version of `GraalVM CE` or a downstream distribution of `GraalVM CE` that does not include the backported patch of https://github.com/graalvm/graalvm-community-jdk21u/pull/23 .
   In this case, the developer needs to write more JSON related to GraalVM Reachability Metadata that can be recognized by the old version of `GraalVM CE`.

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
                 <version>0.11.0</version>
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

### Gradle Ecosystem

Users need to actively use the GraalVM Reachability Metadata central repository.
The following configuration is for reference to configure additional Gradle Tasks for the project,
and the documentation of GraalVM Native Build Tools shall prevail.
Due to the limitations of https://github.com/gradle/gradle/issues/17559 , 
users need to introduce the JSON file of Metadata Repository through Maven dependency. 
Reference https://github.com/graalvm/native-build-tools/issues/572 .

```groovy
plugins {
   id 'org.graalvm.buildtools.native' version '0.11.0'
}

dependencies {
   implementation 'org.apache.shardingsphere:shardingsphere-jdbc:${shardingsphere.version}'
   implementation(group: 'org.graalvm.buildtools', name: 'graalvm-reachability-metadata', version: '0.11.0', classifier: 'repository', ext: 'zip')
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

Add the following content to `src/main/resources/META-INF/native-image/exmaple-test-metadata/reachability-metadata.json` to used 
normally under GraalVM Native Image.

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
   - The full class name of the `javax.sql.XADataSource` implementation class of each database driver is stored in the metadata of ShardingSphere by implementing the SPI of `org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption`.

In the GraalVM Native Image, this actually requires the definition of the GraalVM Reachability Metadata of the third-party dependencies,
while ShardingSphere itself only provides the corresponding GraalVM Reachability Metadata for `com.h2database:h2`.

GraalVM Reachability Metadata of other database drivers such as `com.mysql:mysql-connector-j` should be defined by themselves,
or the corresponding JSON should be submitted to https://github.com/oracle/graalvm-reachability-metadata .

For example, the `com.mysql.cj.jdbc.MysqlXADataSource` class in `com.mysql:mysql-connector-j:9.0.0` implements the `javax.sql.XADataSource` class of the MySQL JDBC Driver.
Users need to define the following JSON in the `reachability-metadata.json` file in the `/META-INF/native-image/com.mysql/mysql-connector-j/9.0.0/` folder of their project's buildpath.

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
          <artifactId>shardingsphere-jdbc-dialect-clickhouse</artifactId>
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

9. Including but not limited to `com.mysql.cj.LocalizedErrorMessages`,
   `com.microsoft.sqlserver.jdbc.SQLServerResource`,
   `org.postgresql.translation.messages`,
   `org.opengauss.translation.messages` from third-party dependencies. By default, L10N resources are loaded according to the system's default locale,
   and localized information for a specific locale is displayed.
   It is tedious to exhaustively enumerate `Resource Bundles` through the JSON definition of GraalVM Reachability Metadata.
   This sometimes causes GraalVM Native Image to throw a warning log similar to the following at runtime.
   The usual operation is to set the `buildArg` of `-H:+IncludeAllLocales`.

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

10. Due to the use of `janino-compiler/janino` by `apache/calcite`, 
    ShardingSphere's `SQL Federation` feature is unavailable in the GraalVM Native Image.
    This also prevents ShardingSphere Proxy Native from integrating with OpenGauss.

11. Due to the issue at https://github.com/oracle/graal/issues/11280, 
    Etcd's Cluster mode integration cannot be used on GraalVM Native Images compiled via Windows 11,
    and Etcd's Cluster mode will conflict with the GraalVM Tracing Agent.
    If developers need to use Etcd's Cluster mode on GraalVM Native Images compiled via Linux,
    they need to provide additional GraalVM Reachability Metadata related JSON themselves.