+++
title = "Known Implementation"
weight = 4
chapter = true
+++

## Background Information

For the driver class of `org.apache.shardingsphere.driver.ShardingSphereDriver`,
by implementing the SPI of `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader`,
allows YAML configuration files to be fetched from multiple sources and File Systems and parsed into ShardingSphere.
If there is no specific statement, the following implementations all use YAML 1.1 as the YAML writing specification.
This does not prevent custom implementations of `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader` from being manually converted to YAML from files such as XML or JSON.

After parsing and loading the YAML file into ShardingSphere's metadata, 
the next behavior will be determined again through the relevant configuration of [Mode Configuration](/en/user-manual/shardingsphere-jdbc/java-api/mode). 
Discuss two situations,

1. ShardingSphere’s metadata does not exist in the Metadata Repository, and local metadata will be stored in the Metadata Repository.
2. The metadata of ShardingSphere already exists in the Metadata Repository. 
   Regardless of whether it is the same as the local metadata, 
   the local metadata will be overwritten by the metadata of the Metadata Repository.

For the configuration of the Metadata Repository, 
please refer to [Metadata Repository](/en/user-manual/common-config/builtin-algorithm/metadata-repository/).

## How to load configuration files

### Load configuration files from classpath

The configuration file is `xxx.yaml`. When `placeholder-type` is `none` or is not specified, 
the configuration file format is consistent with [YAML configuration](../../../yaml-config).
When `placeholder-type` exists and is not `none`, 
the configuration file format is defined in the `JDBC URL Parameters` section of this article.

Example:

- `jdbc:shardingsphere:classpath:config.yaml`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props`

### Load configuration file from absolute path

The configuration file is `xxx.yaml`. When `placeholder-type` is `none` or is not specified, 
the configuration file format is consistent with [YAML configuration](../../../yaml-config).
When `placeholder-type` exists and is not `none`, 
the configuration file format is defined in the `JDBC URL Parameters`section of this article.

Example:

- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=system_props`

## JDBC URL parameters

For implementations of `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader`, 
not all JDBC URL parameters must be parsed,
this involves how to implement `org.apache.shardingsphere.infra.url.ShardingSphereURLLoader.load()`.

### placeholder-type

There is a `placeholder-type` attribute for optional loading of configuration files containing dynamic placeholders. 
The default value of `placeholder-type` is `none`.
When `placeholder-type` is set to something other than `none`, allows setting the value of specific YAML properties via
dynamic placeholders in the involved YAML file, and configuring optional default values.
The name of a dynamic placeholder and its optional default value are separated by `::` and wrapped in the outermost
layer by `$${` and `}`.

Discuss two situations,

1. When the corresponding dynamic placeholder value does not exist, 
   the value of this YAML attribute will be set to the default value on the right side of `::`.
2. When neither the corresponding dynamic placeholder value nor the default value on the right side of `::` exists, 
   this attribute will be set to empty.

#### Single dynamic placeholder

##### none

The configuration file is `xxx.yaml`, and the configuration file format is consistent
with [YAML configuration](../../../yaml-config).

Example：

- `jdbc:shardingsphere:classpath:config.yaml`
- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=none`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=none`

##### environment

When loading a configuration file containing environment variables,
users need to set `placeholder-type` to `environment`, which is commonly used in Docker Image deployment scenarios.
The configuration file is `xxx.yaml`, and the configuration file format is basically the same as [YAML configuration](../../../yaml-config).

Assume that the following set of environment variables exists,

1. The existing environment variable `FIXTURE_JDBC_URL` is `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`.
2. The existing environment variable `FIXTURE_USERNAME` is `sa`.

Then for the intercepted fragment of the following YAML file,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::org.h2.Driver}
  standardJdbcUrl: $${FIXTURE_JDBC_URL::jdbc:h2:mem:foo_ds_do_not_use}
  username: $${FIXTURE_USERNAME::}
  password: $${FIXTURE_PASSWORD::}
```

This YAML snippet will be parsed as,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: org.h2.Driver
  standardJdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
  username: sa
  password:
```

Example:

- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=environment`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=environment`

##### system_props

When loading a configuration file containing system properties, users need to set `placeholder-type` to `system_props`.
The configuration file is `xxx.yaml`, and the configuration file format is basically the same as [YAML configuration](../../../yaml-config).

Assume the following set of system properties exists,

1. The existing system property `fixture.config.driver.jdbc-url` is `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`.
2. The existing system property `fixture.config.driver.username` is `sa`.

Then for the intercepted fragment of the following YAML file,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${fixture.config.driver.driver-class-name::org.h2.Driver}
  standardJdbcUrl: $${fixture.config.driver.jdbc-url::jdbc:h2:mem:foo_ds_do_not_use}
  username: $${fixture.config.driver.username::}
  password: $${fixture.config.driver.password::}
```

This YAML snippet will be parsed as,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: org.h2.Driver
  standardJdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
  username: sa
  password:
```

In real situations, system variables are usually defined dynamically. 
Assume that none of the above system variables are defined,
and there is a YAML file `config.yaml` containing the above YAML interception fragment,
users can refer to the following method to create a DataSource instance using the HikariCP Java API.

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

Example:

- `jdbc:shardingsphere:classpath:config.yaml?placeholder-type=system_props`
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml?placeholder-type=system_props`

#### multiple dynamic placeholders

On top of a single dynamic placeholder, users can use multiple dynamic placeholders in a single line of YAML.
When configuring the value of a YAML attribute, if part of the value of the YAML attribute needs to be replaced dynamically, you can implement this by configuring multiple dynamic placeholders.

Assume the following set of environment variables or system properties exists,

1. The existing environment variable or system property `FIXTURE_HOST` is `127.0.0.1`。
2. The existing environment variable or system property `FIXTURE_PORT` is `3306`。
3. The existing environment variable or system property `FIXTURE_DATABASE` is `test`。
4. The existing environment variable or system property `FIXTURE_USERNAME` is `sa`。

Then for the intercepted fragment of the following YAML file,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::com.mysql.cj.jdbc.Driver}
  standardJdbcUrl: jdbc:mysql://$${FIXTURE_HOST::}:$${FIXTURE_PORT::}/$${FIXTURE_DATABASE::}?sslMode=REQUIRED
  username: $${FIXTURE_USERNAME::}
  password: $${FIXTURE_PASSWORD::}
```

This YAML snippet will be parsed as,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: com.mysql.cj.jdbc.Driver
  standardJdbcUrl: jdbc:mysql://127.0.0.1:3306/test?sslMode=REQUIRED
  username: sa
  password:
```

## Other implementations

For details, please refer to https://github.com/apache/shardingsphere-plugin.
