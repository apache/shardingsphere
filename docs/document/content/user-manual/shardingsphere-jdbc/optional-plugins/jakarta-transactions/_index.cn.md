+++
title = "Jakarta EE 9 Spec 的 Jakarta Transactions"
weight = 6
+++

## 背景信息

本页文档所涉及的可选模块均采用 Jakarta EE 9 Spec 的 Jakarta Transactions，兼容 Jakarta EE 9，Jakarta EE 9.1 或 Jakarta EE 10。
本页文档所涉及的可选模块，旨在解决可选模块 `shardingsphere-transaction-xa` 无法在 Spring Boot 3，Quarkus 3，
Micronaut Framework 4 或 Helidon 3 等其他基于 Jakarta EE 9+ 的 Web Framework 下使用的问题。

本页文档所涉及的可选模块与 `shardingsphere-transaction-xa` 的约定配置基本一致。
但相关可选模块无法与 `shardingsphere-transaction-xa` 在同一 classpath，或在同一 Maven 模块使用。
Java EE 8 或 Jakarta EE 8 不属于本页文档所涉及的可选模块的支持范围，对于类似的情况应考虑使用 `shardingsphere-transaction-xa`。

## JDBC

### Atomikos

#### 前提条件

要在 ShardingSphere 的配置文件中配置 Atomikos 作为 Jakarta EE 9 Spec 的 Jakarta Transactions 实现，
可能的 Maven 依赖关系如下，

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

#### 配置

与 `org.apache.shardingsphere:shardingsphere-transaction-xa-core` 一致，可以通过在项目的 classpath 中添加 `jta.properties` 来定制化 Atomikos 配置项。

在 ShardingSphere 的配置文件中，可能的配置项如下，

```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```

### Narayana

要在 ShardingSphere 的配置文件中配置 Narayana 作为 Jakarta EE 9 Spec 的 Jakarta Transactions 实现，
可能的 Maven 依赖关系如下，

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

#### 配置

与 `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana` 一致，可以通过在项目的 classpath 中添加 `jbossts-properties.xml` 来定制化 Narayana 配置项。

对于 `jbossts-properties.xml` 的最小配置，ShardingSphere 要求定义 Narayana 的 `CoreEnvironmentBean.nodeIdentifier` 属性。
如果 Narayana 的 object store 并非在不同的 Narayana 实例之间共享，你可以将此值设置为 `1`。一个可能的 `jbossts-properties.xml` 配置如下，

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
    <entry key="CoreEnvironmentBean.nodeIdentifier">1</entry>
</properties>
```

在特定情况下，开发者可能不会希望使用 XML 文件，那开发者需要在自有 Java 项目的启动类手动设置 `CoreEnvironmentBean.nodeIdentifier`。 可参考如下方法调用 Narayana Java API。

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

在 ShardingSphere 的配置文件中，可能的配置项如下，

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```

## Proxy

以下内容仅作为 ShardingSphere 默认 Maven Profile 的构建配置的参考。

引入前提，ShardingSphere Proxy 默认情况下使用 Jakarta EE 8 Spec 的 Jakarta Transactions 实现。

### Atomikos

如果需要使用 Jakarta EE 9 Spec 的 Jakarta Transactions 的 Atomikos 实现，
需要更改源代码，并手动编译 binary 或 Linux Container。

对于 `distribution/proxy/pom.xml` 文件的如下部分，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

需更改为，

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

仅将 `jakarta.transaction-api` 固定为 `2.0.0`，不要把整个 `jakarta.jakartaee-bom` 覆盖为 `9.0.0`，因为已验证整 BOM 覆盖会把 `jakarta.validation-api` 升级到 `3.0.0`，而 `bval-jsr` 仍使用 `javax.validation` 命名空间，Proxy 启动随即以 `NoClassDefFoundError: javax/validation/ValidationException` 失败。
必须替换旧的 XA 依赖，而不是在保留旧依赖的同时追加 Jakarta 依赖。
使用显式停用 `default-dep` 且不激活 `all`、`transaction-atomikos`、`transaction-narayana` 的命令构建 binary，因为 `default-dep` 为 `activeByDefault`，而 `default-dep` 与 `all` 都会带入旧 Provider，重新制造混合 `javax.transaction`/`jakarta.transaction` 类路径。

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep'
```

使用相同的 Profile 选择再加上 `docker` Profile 构建容器镜像。

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep,docker'
```

不要追加 `-T1C` 之类的并行 Reactor 参数，因为 `shardingsphere-proxy-distribution` 与产出其装配所需目录的 `shardingsphere-agent-distribution` 之间没有 Maven Reactor 依赖边，并行构建会在该目录上竞态。

保持 `shardingsphere-proxy-bootstrap` 的 `dev` Profile 处于激活状态，因为它携带方言、权限、模式仓储与 DistSQL 处理器依赖，且不包含旧事务依赖。
确认构建产物的 `lib` 目录包含 `shardingsphere-transaction-xa-jakarta-core`、`shardingsphere-transaction-xa-jakarta-spi`、`shardingsphere-transaction-xa-jakarta-atomikos` 与 `jakarta.transaction-api-2.0.0.jar`，且不包含 `shardingsphere-transaction-xa-core`、`shardingsphere-transaction-xa-spi`、`shardingsphere-transaction-xa-atomikos`、`shardingsphere-transaction-xa-narayana`。
正式使用前需验证 Proxy 能够使用所配置的 Provider 正常启动。

对于 Proxy 的 `global.yaml`, 可能的配置项如下，

```yaml
transaction:
  defaultType: XA
  providerType: Atomikos
```

### Narayana

如果需要使用 Jakarta EE 9 Spec 的 Jakarta Transactions 的 Narayana 实现，需要更改源代码，并手动编译 binary 或 Linux Container。

对于 `distribution/proxy/pom.xml` 文件的如下部分，

```xml
<dependencies>
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-proxy-bootstrap</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

需更改为，

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

仅将 `jakarta.transaction-api` 固定为 `2.0.0`，不要把整个 `jakarta.jakartaee-bom` 覆盖为 `9.0.0`，因为已验证整 BOM 覆盖会把 `jakarta.validation-api` 升级到 `3.0.0`，而 `bval-jsr` 仍使用 `javax.validation` 命名空间，Proxy 启动随即以 `NoClassDefFoundError: javax/validation/ValidationException` 失败。
Narayana Jakarta 产物必须声明显式版本，因为它们在 `shardingsphere-transaction-xa-jakarta-narayana` 中为 `provided` 范围，不会传递引入，且本模块没有管理其版本的 BOM。
必须替换旧的 XA 依赖，而不是在保留旧依赖的同时追加 Jakarta 依赖。
使用显式停用 `default-dep` 且不激活 `all`、`transaction-atomikos`、`transaction-narayana` 的命令构建 binary，因为 `default-dep` 为 `activeByDefault`，而 `default-dep` 与 `all` 都会带入旧 Provider，重新制造混合 `javax.transaction`/`jakarta.transaction` 类路径。

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep'
```

使用相同的 Profile 选择再加上 `docker` Profile 构建容器镜像。

```shell
./mvnw clean package -pl distribution/proxy,distribution/agent -am -DskipTests -P 'release,!default-dep,docker'
```

不要追加 `-T1C` 之类的并行 Reactor 参数，因为 `shardingsphere-proxy-distribution` 与产出其装配所需目录的 `shardingsphere-agent-distribution` 之间没有 Maven Reactor 依赖边，并行构建会在该目录上竞态。

保持 `shardingsphere-proxy-bootstrap` 的 `dev` Profile 处于激活状态，因为它携带方言、权限、模式仓储与 DistSQL 处理器依赖，且不包含旧事务依赖。
确认构建产物的 `lib` 目录在 Jakarta core、SPI 与事务 API 之外还包含 `shardingsphere-transaction-xa-jakarta-narayana` 与 `narayana-jta-jakarta-5.12.7.Final.jar`，且不包含 `shardingsphere-transaction-xa-core`、`shardingsphere-transaction-xa-spi`、`shardingsphere-transaction-xa-atomikos`、`shardingsphere-transaction-xa-narayana`。
正式使用前需验证 Proxy 能够使用所配置的 Provider 正常启动。

对于 Proxy 的 `global.yaml`, 可能的配置项如下，

```yaml
transaction:
  defaultType: XA
  providerType: Narayana
```

对于最小配置，仍需通过与 `global.yaml` 处于同一目录的 `jbossts-properties.xml` 配置 Narayana 的 `CoreEnvironmentBean.nodeIdentifier` 属性。

## Proxy Native

处理方式与 ShardingSphere Proxy 一致。
