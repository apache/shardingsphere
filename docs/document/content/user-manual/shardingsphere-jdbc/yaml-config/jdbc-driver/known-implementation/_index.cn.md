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

加载类路径中 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:classpath:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)一致。

用例：
- `jdbc:shardingsphere:classpath:config.yaml`

### 从绝对路径中加载配置文件

加载绝对路径中 config.yaml 配置文件的 JDBC URL，通过 `jdbc:shardingsphere:absolutepath:` 前缀识别。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)一致。

用例：
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`

### 加载包含环境变量的配置文件

加载路径中包含环境变量的 `config.yaml` 配置文件的 JDBC URL，通过追加 `placeholder-type=xxx` 参数识别。
`placeholder-type` 的取值范围包括 `none`（默认值）， `environment` 和 `system_props`。
配置文件为 `xxx.yaml`，配置文件格式与 [YAML 配置](../../../yaml-config)基本一致。
在涉及的 YAML 文件中，允许通过环境变量设置特定 YAML 属性的值，并配置可选的默认值，这常用于 Docker 镜像的部署场景。
环境变量的名称和其可选的默认值通过`::`分割，在最外层通过`$${`和`}`包裹。

讨论两种情况。
1. 当对应的环境变量不存在时，此 YAML 属性的值将被设置为`::`右侧的默认值。
2. 当对应的环境变量和`::`右侧的默认值均不存在时，此属性将被设置为空。

假设存在以下一组环境变量，
1. 存在环境变量`FIXTURE_JDBC_URL`为`jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`。
2. 存在环境变量`FIXTURE_USERNAME`为`sa`。

则对于以下 YAML 文件的截取片段：

```yaml
ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::org.h2.Driver}
    jdbcUrl: $${FIXTURE_JDBC_URL::jdbc:h2:mem:foo_ds_do_not_use}
    username: $${FIXTURE_USERNAME::}
    password: $${FIXTURE_PASSWORD::}
```
此 YAML 截取片段将被解析为：

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

在实际情况下，系统变量通常是动态定义的。
假设如上系统变量均未定义，存在包含如上 YAML 截取片段的 YAML 文件 `config.yaml`，
可参考如下方法创建 DataSource 实例。

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public DataSource createDataSource() {
    HikariConfig config = new HikariConfig();
    config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
    config.setJdbcUrl("jdbc:shardingsphere:classpath:config.yaml??placeholder-type=system_props");
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
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props`

### 其他实现

具体可参考 https://github.com/apache/shardingsphere-plugin 。
