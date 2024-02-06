+++
title = "Known Implementation"
weight = 4
chapter = true
+++

## Background Information

For the driver class of `org.apache.shardingsphere.driver.ShardingSphereDriver`,
by implementing the SPI of `org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereURLProvider`,
allows YAML configuration files to be fetched from multiple sources and File Systems and parsed into ShardingSphere.

After parsing and loading the YAML file into ShardingSphere's metadata,
The next behavior will be determined again through the relevant configuration of [Mode Configuration](../../../java-api/mode). Discuss two situations:
1. ShardingSphere’s metadata does not exist in the metadata repository, and local metadata will be stored in the metadata repository.
2. ShardingSphere’s metadata already exists in the metadata repository, regardless of whether it is the same as the local metadata, 
the local metadata will be overwritten by the metadata of the metadata repository.

For the configuration of the metadata repository, please refer to [Metadata Repository](../../../../common-config/builtin-algorithm/metadata-repository).

## URL configuration

### Load configuration files from classpath
Load the JDBC URL of the config.yaml configuration file in classpath, identified by the `jdbc:shardingsphere:classpath:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is consistent with [YAML configuration](../../../yaml-config).

Example:
- `jdbc:shardingsphere:classpath:config.yaml`

### Load configuration file from absolute path
JDBC URL to load the config.yaml configuration file in an absolute path, identified by the `jdbc:shardingsphere:absolutepath:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is consistent with [YAML configuration](../../../yaml-config).

Example:
- `jdbc:shardingsphere:absolutepath:/path/to/config.yaml`

### Load configuration file containing environment variables from classpath

JDBC URL to load the config.yaml configuration file that contains environment variables in classpath, identified by the `jdbc:shardingsphere:classpath-environment:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is basically the same as [YAML configuration](../../../yaml-config).
Allows setting the value of specific YAML properties via environment variables and configuring optional default values in the involved YAML files. 
This is commonly used in Docker Image deployment scenarios.
The name of an environment variable and its optional default value are separated by `::` and wrapped in the outermost layer by `$${` and `}`.

Discuss two situations.
1. When the corresponding environment variable does not exist, the value of this YAML attribute will be set to the default value on the right side of `::`.
2. When the corresponding environment variable and the default value on the right side of `::` do not exist, this property will be set to empty.

Assume that the following set of environment variables exists,
1. The existing environment variable `FIXTURE_JDBC_URL` is `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`.
2. The existing environment variable `FIXTURE_USERNAME` is `sa`.

Then for the intercepted fragment of the following YAML file,
```yaml
ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: $${FIXTURE_DRIVER_CLASS_NAME::org.h2.Driver}
    jdbcUrl: $${FIXTURE_JDBC_URL::jdbc:h2:mem:foo_ds_do_not_use}
    username: $${FIXTURE_USERNAME::}
    password: $${FIXTURE_PASSWORD::}
```
This YAML snippet will be parsed as,
```yaml
ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: org.h2.Driver
    jdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
    username: sa
    password:
```

Example:
- `jdbc:shardingsphere:classpath-environment:config.yaml`

### Load configuration file containing environment variables from absolute path

JDBC URL to load the config.yaml configuration file that contains environment variables in absolute path, 
identified by the `jdbc:shardingsphere:absolutepath-environment:` prefix.
The configuration file is `xxx.yaml`, and the configuration file format is consistent with `jdbc:shardingsphere:classpath-environment:`.
The difference from `jdbc:shardingsphere:classpath-environment:` is only where the YAML file is loaded.

Example:
- `jdbc:shardingsphere:absolutepath-environment:/path/to/config.yaml`

### Load configuration file containing system properties from classpath

JDBC URL to load the config.yaml configuration file containing system properties in the classpath, 
identified by the `jdbc:shardingsphere:classpath-system-props:` prefix.
The configuration file is `xxx.yaml`, 
and the configuration file format is consistent with `jdbc:shardingsphere:classpath-environment:`.
The difference from `jdbc:shardingsphere:classpath-environment:` is only where the property value is read.

Assume the following set of system properties exists,

1. The system property `fixture.config.driver.jdbc-url` exists as `jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL`.
2. The system property `fixture.config.driver.username` exists as `sa`.

Then for the intercepted fragment of the following YAML file,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: $${fixture.config.driver.driver-class-name::org.h2.Driver}
  jdbcUrl: $${fixture.config.driver.jdbc-url::jdbc:h2:mem:foo_ds_do_not_use}
  username: $${fixture.config.driver.username::}
  password: $${fixture.config.driver.password::}
```

This YAML snippet will be parsed as,

```yaml
ds_1:
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  driverClassName: org.h2.Driver
  jdbcUrl: jdbc:h2:mem:foo_ds_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL
  username: sa
  password:
```

In real situations, system variables are usually defined dynamically.
Assume that none of the above system variables are defined, 
and there is a YAML file `config.yaml` containing the above YAML interception fragment,
Users can refer to the following methods to create a DataSource instance.

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

Example:
- `jdbc:shardingsphere:classpath-system-props:config.yaml`

### Other implementations
For details, please refer to https://github.com/apache/shardingsphere-plugin .
