+++
title = "已知实现"
weight = 4
chapter = true
+++

## 背景信息

对于 `org.apache.shardingsphere.driver.ShardingSphereDriver` 的驱动类，
通过实现 `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader` 的 SPI，
可允许从多种来源和 File System 获取并解析为 ShardingSphere 的 YAML 配置文件。
如无特定声明，以下实现均采用 YAML 1.1 作为 YAML 的编写规范，
这并不阻止 `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader` 的自定义实现从 XML 或 JSON 等文件手动转化为 YAML。

在解析并加载 YAML 文件为 ShardingSphere 的元数据后，
会再次通过[模式配置](/cn/user-manual/shardingsphere-jdbc/java-api/mode)的相关配置决定下一步行为。讨论两种情况，

1. 元数据持久化仓库中不存在 ShardingSphere 的元数据，本地元数据将被存储到元数据持久化仓库。
2. 元数据持久化仓库中已存在 ShardingSphere 的元数据，无论是否与本地元数据相同，本地元数据将被元数据持久化仓库的元数据覆盖。

对元数据持久化仓库的配置需参考[元数据持久化仓库](/cn/user-manual/common-config/builtin-algorithm/metadata-repository/)。

## 加载配置文件的方式

### 从类路径中加载配置文件

配置文件为 `xxx.yaml`，当`placeholder-type`为`none`或不标明时，配置文件格式与 [YAML 配置](../../../yaml-config) 一致。
当`placeholder-type`存在且不为`none`时，配置文件格式的定义参考本文的`JDBC URL 参数`一节。

用例：

- `jdbc:shardingsphere:classpath:config.yaml`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props`

### 从绝对路径中加载配置文件

配置文件为 `xxx.yaml`，当`placeholder-type`为`none`或不标明时， 配置文件格式与 [YAML 配置](../../../yaml-config) 一致。
当`placeholder-type`存在且不为`none`时，配置文件格式的定义参考本文的`JDBC URL 参数`一节。

用例：

- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=system_props`

## JDBC URL 参数

对于 `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader` 的实现，并非所有的 JDBC URL 参数都必须被解析，
这涉及到如何实现 `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader.load()`。

### placeholder-type

存在 `placeholder-type` 属性用于可选的加载包含动态占位符的配置文件，`placeholder-type` 存在默认值为`none`。
当 `placeholder-type` 设置为非 `none` 时， 在涉及的 YAML 文件中允许通过动态占位符设置特定 YAML 属性的值，并配置可选的默认值。
动态占位符的名称和其可选的默认值通过`::`分割， 在最外层通过`$${`和`}`包裹。

讨论两种情况，

1. 当对应的动态占位符的值不存在时，此 YAML 属性的值将被设置为`::`右侧的默认值。
2. 当对应的动态占位符的值和`::`右侧的默认值均不存在时，此属性将被设置为空。

#### 单个动态占位符

##### none

配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config) 一致。

用例：

- `jdbc:shardingsphere:classpath:config.yaml`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=none`

##### environment

加载包含环境变量的配置文件时，需将 `placeholder-type`置为`environment`，这常用于 Docker Image 的部署场景。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config) 基本一致。

假设存在以下一组环境变量，

1. 存在环境变量 `FIXTURE_JDBC_URL` 为 `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`。
2. 存在环境变量 `FIXTURE_USERNAME` 为 `sa`。

则对于以下 YAML 文件的截取片段，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::org.h2.Driver}
  jdbcUrl: $${FIXTURE_JDBC_URL::jdbc:h2:mem:foo_ds_do_not_use}
  username: $${FIXTURE_USERNAME::}
  password: $${FIXTURE_PASSWORD::}
```

此 YAML 截取片段将被解析为，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: org.h2.Driver
  jdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
  username: sa
  password:
```

用例：

- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=environment`

##### system_props

加载包含系统属性的配置文件时，需将 `placeholder-type`置为`system_props`。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config) 基本一致。

假设存在以下一组系统属性，

1. 存在系统属性 `fixture.config.driver.jdbc-url` 为 `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`。
2. 存在系统属性 `fixture.config.driver.username` 为 `sa`。

则对于以下 YAML 文件的截取片段，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${fixture.config.driver.driver-class-name::org.h2.Driver}
  jdbcUrl: $${fixture.config.driver.jdbc-url::jdbc:h2:mem:foo_ds_do_not_use}
  username: $${fixture.config.driver.username::}
  password: $${fixture.config.driver.password::}
```

此 YAML 截取片段将被解析为，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: org.h2.Driver
  jdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
  username: sa
  password:
```

在实际情况下，系统变量通常是动态定义的。假设如上系统变量均未定义，存在包含如上 YAML 截取片段的 YAML 文件 `config.yaml`，
可参考如下方法，使用 HikariCP Java API 创建 DataSource 实例。

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class ExampleUtils {
    public DataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props");
        try {
            assert null == System.getProperty("fixture.config.driver.jdbc-url");
            assert null == System.getProperty("fixture.config.driver.username");
            System.setProperty("fixture.config.driver.jdbc-url", "jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
            System.setProperty("fixture.config.driver.username", "sa");
            return new HikariDataSource(config);
        } finally {
            System.clearProperty("fixture.config.driver.jdbc-url");
            System.clearProperty("fixture.config.driver.username");
        }
    }
}
```

用例：

- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=system_props`

#### 多个动态占位符

在单个动态占位符的基础上，用户可以在单行 YAML 使用多个动态占位符。
在配置 YAML 属性值的时候，如果 YAML 属性值的部分需要动态替换，可以通过配置多个动态占位符的方式来实现

假设存在以下一组环境变量或系统属性，

1. 存在环境变量或系统属性 `FIXTURE_HOST` 为 `127.0.0.1`。
2. 存在环境变量或系统属性 `FIXTURE_PORT` 为 `3306`。
3. 存在环境变量或系统属性 `FIXTURE_DATABASE` 为 `test`。
4. 存在环境变量或系统属性 `FIXTURE_USERNAME` 为 `sa`。

则对于以下 YAML 文件的截取片段，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::com.mysql.cj.jdbc.Driver}
  jdbcUrl: jdbc:mysql://$${FIXTURE_HOST::}:$${FIXTURE_PORT::}/$${FIXTURE_DATABASE::}?sslMode=REQUIRED
  username: $${FIXTURE_USERNAME::}
  password: $${FIXTURE_PASSWORD::}
```

此 YAML 截取片段将被解析为，

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.cj.jdbc.Driver
  jdbcUrl: jdbc:mysql://127.0.0.1:3306/test?sslMode=REQUIRED
  username: sa
  password:
```

## 其他实现

具体可参考 https://github.com/apache/shardingsphere-plugin 。
