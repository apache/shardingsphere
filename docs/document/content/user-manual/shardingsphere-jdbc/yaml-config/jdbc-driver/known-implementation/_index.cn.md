+++
title = "已知实现"
weight = 4
chapter = true
+++

## 背景信息

对于 `org.apache.shardingsphere.driver.ShardingSphereDriver` 的驱动类，
通过实现 `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider` 的 SPI，
可允许从多种来源和 File System 获取并解析为 ShardingSphere 的 YAML 配置文件。

在解析并加载 YAML 文件为 ShardingSphere 的元数据后，
会再次通过[模式配置](../../../java-api/mode)的相关配置决定下一步行为。讨论两种情况：
1. 元数据持久化仓库中不存在 ShardingSphere 的元数据，本地元数据将被存储到元数据持久化仓库。
2. 元数据持久化仓库中已存在 ShardingSphere 的元数据，无论是否与本地元数据相同，本地元数据将被元数据持久化仓库的元数据覆盖。

对元数据持久化仓库的配置需参考[元数据持久化仓库](../../../../common-config/builtin-algorithm/metadata-repository)。

## URL 配置

### 从类路径中加载配置文件
加载 classpath 中 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:classpath:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)一致。

用例：
- `jdbc:shardingsphere:classpath:config.yaml`

### 从绝对路径中加载配置文件
加载绝对路径中 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:absolutepath:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)一致。

用例：
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`

### 从类路径中加载包含环境变量的配置文件

加载 classpath 中包含环境变量的 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:classpath-environment:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)基本一致。
在涉及的 YAML 文件中，允许通过环境变量设置特定YAML属性的值，并配置可选的默认值。这常用于 Docker Image 的部署场景。
环境变量的名称和其可选的默认值通过`::`分割，在最外层通过`$${`和`}`包裹。

讨论两种情况。
1. 当对应的环境变量不存在时，此 YAML 属性的值将被设置为`::`右侧的默认值。
2. 当对应的环境变量和`::`右侧的默认值均不存在时，此属性将被设置为空。

假设存在以下一组环境变量，
1. 存在环境变量`FIXTURE_JDBC_URL`为`jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`。
2. 存在环境变量`FIXTURE_USERNAME`为`sa`。

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
- `jdbc:shardingsphere:classpath-environment:config.yaml`

### 从绝对路径中加载包含环境变量的配置文件

加载绝对路径中包含环境变量的 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:absolutepath-environment:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与`jdbc:shardingsphere:classpath-environment:`一致。
与 `jdbc:shardingsphere:classpath-environment:` 的区别仅在于 YAML 文件的加载位置。

用例：
- `jdbc:shardingsphere:absolutepath-environment:/path/to/config.yaml`

### 从类路径中加载包含系统属性的配置文件

加载类路径中包含系统属性的 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:classpath-system-props:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与`jdbc:shardingsphere:classpath-environment:`一致。
与 `jdbc:shardingsphere:classpath-environment:` 的区别仅在于读取属性值的位置。

假设存在以下一组系统属性，

1. 存在系统属性`fixture.config.driver.jdbc-url`为`jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`。
2. 存在系统属性`fixture.config.driver.username`为`sa`。

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

在实际情况下，系统变量通常是动态定义的。
假设如上系统变量均未定义，存在包含如上YAML截取片段的YAML文件`config.yaml`，
可参考如下方法创建 DataSource 实例。

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public DataSource createDataSource() {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
    config.setJdbcUrl("jdbc:shardingsphere:classpath-system-props:config.yaml");
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
```

用例：
- `jdbc:shardingsphere:classpath-system-props:config.yaml`

### 其他实现
具体可参考 https://github.com/apache/shardingsphere-plugin 。
