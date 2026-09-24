+++
title = "Jakarta Transactions of Jakarta EE 9 Spec"
weight = 6
+++

## Background Information

The optional modules mentioned in this document all use Jakarta Transactions of Jakarta EE 9 Spec and are compatible with Jakarta EE 9,
Jakarta EE 9.1 or Jakarta EE 10.
The optional modules mentioned in this document are designed to solve the problem that the optional module `shardingsphere-transaction-xa` cannot be used in other Web Frameworks based on Jakarta EE 9+,
such as Spring Boot 3, Quarkus 3, Micronaut Framework 4 or Helidon 3.

The optional modules mentioned in this document are basically consistent with the agreed configuration of `shardingsphere-transaction-xa`.
However, the relevant optional modules cannot be used in the same classpath or in the same Maven module as `shardingsphere-transaction-xa`.
Java EE 8 or Jakarta EE 8 are not supported by the optional modules mentioned in this document. For similar situations, you should consider using `shardingsphere-transaction-xa`.

## JDBC

### Atomikos

#### Prerequisites

To configure Atomikos as the Jakarta Transactions implementation of the Jakarta EE 9 Spec in the ShardingSphere configuration file,
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-core</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
</dependencies>
```

#### Configuration

As with `org.apache.shardingsphere:shardingsphere-transaction-xa-core`,
you can customize Atomikos configuration items by adding `jta.properties` to the project's classpath.

In the ShardingSphere configuration file, possible configuration items are as follows,

```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```

### Narayana

To configure Narayana as the Jakarta Transactions implementation of the Jakarta EE 9 Spec in ShardingSphere's configuration file,
the possible Maven dependencies are as follows,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc</artifactId>
        <version>${shardingsphere.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-core</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-narayana</artifactId>
        <version>${shardingsphere.version}</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.narayana.jta</groupId>
        <artifactId>narayana-jta-jakarta</artifactId>
        <version>5.12.7.Final</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.narayana.jts</groupId>
        <artifactId>narayana-jts-integration-jakarta</artifactId>
        <version>5.12.7.Final</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.logging</groupId>
        <artifactId>jboss-logging</artifactId>
        <version>3.4.3.Final</version>
    </dependency>
</dependencies>
```

#### Configuration

In line with `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`,
Narayana configuration items can be customized by adding `jbossts-properties.xml` to the project's classpath.

For the minimal configuration of `jbossts-properties.xml`, ShardingSphere requires the definition of Narayana's `CoreEnvironmentBean.nodeIdentifier` property.
If Narayana's object store is not shared between different Narayana instances, you can set this value to `1`.
A possible `jbossts-properties.xml` configuration is as follows,

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
</properties>
```

In certain cases, developers may not want to use XML files,
then developers need to manually set `CoreEnvironmentBean.nodeIdentifier` in the startup class of their own Java project.
You can refer to the following method to call the Narayana Java API.

```java
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class ExampleUtils {
    public void initNarayanaInstance() {
        try {
            arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
        } catch (CoreEnvironmentBeanException e) {
            throw new RuntimeException(e);
        }
    }
}
```

In the ShardingSphere configuration file, possible configuration items are as follows:

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```

## Proxy

The following content is only a reference for the build configuration of ShardingSphere's default Maven Profile.

Introduction premise, ShardingSphere Proxy uses the Jakarta Transactions implementation of the Jakarta EE 8 Spec by default.

### Atomikos

If you need to use the Atomikos implementation of Jakarta Transactions of the Jakarta EE 9 Spec,
you need to change the source code and manually compile the binary or Linux Container.

For the following part of the `distribution/proxy/pom.xml` file,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

Need to be changed to,

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jakarta.transaction</groupId>
            <artifactId>jakarta.transaction-api</artifactId>
            <version>2.0.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-core</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-spi</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-atomikos</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-core</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

Pin only `jakarta.transaction-api` to `2.0.0` instead of overriding the whole `jakarta.jakartaee-bom` to `9.0.0`, because the whole-BOM override was verified to upgrade `jakarta.validation-api` to `3.0.0` while `bval-jsr` still uses the `javax.validation` namespace, and Proxy then fails at startup with `NoClassDefFoundError: javax/validation/ValidationException`.
Replace the legacy XA dependencies instead of adding the Jakarta dependencies alongside them.
Build the binary with an invocation that explicitly deactivates `default-dep` and does not activate `all`, `transaction-atomikos`, or `transaction-narayana`, because `default-dep` is `activeByDefault` and both `default-dep` and `all` add a legacy provider that recreates the mixed `javax.transaction`/`jakarta.transaction` classpath.

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep'
```

Build the container image with the same profile selection plus the `docker` profile.

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep,docker'
```

Do not add a parallel reactor flag such as `-T1C`, because no Maven reactor dependency edge connects `shardingsphere-proxy-distribution` to `shardingsphere-agent-distribution` whose output directory the Proxy assembly consumes, and parallel builds race on that directory.

Keep the `dev` profile of `shardingsphere-proxy-bootstrap` active, because it carries the dialect, authority, mode repository, and DistSQL handler dependencies and contains no legacy transaction dependency.
Verify that the built `lib` directory contains `shardingsphere-transaction-xa-jakarta-core`, `shardingsphere-transaction-xa-jakarta-spi`, `shardingsphere-transaction-xa-jakarta-atomikos`, and `jakarta.transaction-api-2.0.0.jar`, and that it contains neither `shardingsphere-transaction-xa-core`, `shardingsphere-transaction-xa-spi`, `shardingsphere-transaction-xa-atomikos`, nor `shardingsphere-transaction-xa-narayana`.
Verify that Proxy starts successfully with the configured provider before using it.

For Proxy's `global.yaml`, possible configuration items are as follows,

```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```

### Narayana

If you need to use the Narayana implementation of Jakarta Transactions with the Jakarta EE 9 Spec,
you need to modify the source code and manually compile the binary or Linux Container.

For the following part of the `distribution/proxy/pom.xml` file,

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

Need to be changed to,

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jakarta.transaction</groupId>
            <artifactId>jakarta.transaction-api</artifactId>
            <version>2.0.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-core</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-spi</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-atomikos</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-transaction-xa-narayana</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-core</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-transaction-xa-jakarta-narayana</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>jakarta.transaction</groupId>
        <artifactId>jakarta.transaction-api</artifactId>
    </dependency>
    <dependency>
        <groupId>org.jboss.narayana.jta</groupId>
        <artifactId>narayana-jta-jakarta</artifactId>
        <version>5.12.7.Final</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.narayana.jts</groupId>
        <artifactId>narayana-jts-integration-jakarta</artifactId>
        <version>5.12.7.Final</version>
    </dependency>
    <dependency>
        <groupId>org.jboss.logging</groupId>
        <artifactId>jboss-logging</artifactId>
        <version>3.4.3.Final</version>
    </dependency>
</dependencies>
```

Pin only `jakarta.transaction-api` to `2.0.0` instead of overriding the whole `jakarta.jakartaee-bom` to `9.0.0`, because the whole-BOM override was verified to upgrade `jakarta.validation-api` to `3.0.0` while `bval-jsr` still uses the `javax.validation` namespace, and Proxy then fails at startup with `NoClassDefFoundError: javax/validation/ValidationException`.
Declare explicit versions for the Narayana Jakarta artifacts, because they are `provided` scope in `shardingsphere-transaction-xa-jakarta-narayana` and therefore do not arrive transitively, and no repository BOM manages them for this module.
Replace the legacy XA dependencies instead of adding the Jakarta dependencies alongside them.
Build the binary with an invocation that explicitly deactivates `default-dep` and does not activate `all`, `transaction-atomikos`, or `transaction-narayana`, because `default-dep` is `activeByDefault` and both `default-dep` and `all` add a legacy provider that recreates the mixed `javax.transaction`/`jakarta.transaction` classpath.

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep'
```

Build the container image with the same profile selection plus the `docker` profile.

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep,docker'
```

Do not add a parallel reactor flag such as `-T1C`, because no Maven reactor dependency edge connects `shardingsphere-proxy-distribution` to `shardingsphere-agent-distribution` whose output directory the Proxy assembly consumes, and parallel builds race on that directory.

Keep the `dev` profile of `shardingsphere-proxy-bootstrap` active, because it carries the dialect, authority, mode repository, and DistSQL handler dependencies and contains no legacy transaction dependency.
Verify that the built `lib` directory contains `shardingsphere-transaction-xa-jakarta-narayana` and `narayana-jta-jakarta-5.12.7.Final.jar` in addition to the Jakarta core, SPI, and transaction API jars, and that it contains neither `shardingsphere-transaction-xa-core`, `shardingsphere-transaction-xa-spi`, `shardingsphere-transaction-xa-atomikos`, nor `shardingsphere-transaction-xa-narayana`.
Verify that Proxy starts successfully with the configured provider before using it.

For Proxy's `global.yaml`, possible configuration items are as follows,

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```

For a minimal configuration,
you still need to configure Narayana's `CoreEnvironmentBean.nodeIdentifier` property via `jbossts-properties.xml` in the same directory as `global.yaml`.

## Proxy Native

The processing method is the same as ShardingSphere Proxy.
