+++
title = "配置中心"
weight = 1
+++

## 实现动机

- 配置集中化：越来越多的运行时实例，使得散落的配置难于管理，配置不同步导致的问题十分严重。将配置集中于配置中心，可以更加有效进行管理。

- 配置动态化：配置修改后的分发，是配置中心可以提供的另一个重要能力。它可支持数据源和规则的动态切换。

## 配置中心数据结构

配置中心在定义的命名空间的 `config` 节点下，以 YAML 格式存储，包括数据源信息，规则信息、权限配置和属性配置，可通过修改节点来实现对于配置的动态管理。

```
config
    ├──authentication                            # 权限配置
    ├──props                                     # 属性配置
    ├──schema                                    # Schema 配置
    ├      ├──schema_1                           # Schema 名称1
    ├      ├      ├──datasource                  # 数据源配置
    ├      ├      ├──rule                        # 规则配置
    ├      ├──schema_2                           # Schema 名称2
    ├      ├      ├──datasource                  # 数据源配置
    ├      ├      ├──rule                        # 规则配置
```

### config/authentication

权限配置，可配置访问 ShardingSphere-Proxy 的用户名和密码。

```yaml
username: root
password: root
```

### config/props

属性配置，详情请参见[配置手册](/cn/user-manual/shardingsphere-jdbc/configuration/)。

```yaml
executor.size: 20
sql.show: true
```

### config/schema/schemeName/datasource

多个数据库连接池的集合，不同数据库连接池属性自适配（例如：DBCP，C3P0，Druid, HikariCP）。

```yaml
ds_0: !!org.apache.shardingsphere.governance.core.common.yaml.config.YamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
ds_1: !!org.apache.shardingsphere.governance.core.common.yaml.configYamlDataSourceConfiguration
  dataSourceClassName: com.zaxxer.hikari.HikariDataSource
  props:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    password: null
    maxPoolSize: 50
    maintenanceIntervalMilliseconds: 30000
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    minPoolSize: 1
    username: root
    maxLifetimeMilliseconds: 1800000
```

### config/schema/schemeName/rule

规则配置，可包括数据分片、读写分离、数据加密、影子库压测、多副本等配置。

```yaml
rules:
- !SHARDING
  xxx
  
- !MASTERSLAVE
  xxx
  
- !ENCRYPT
  xxx
```

## 动态生效

在配置中心上修改、删除、新增相关配置，会动态推送到生产环境并立即生效。