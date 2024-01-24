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
会再次通过[模式配置](../../../java-api/mode.cn.md)的相关配置决定下一步行为。讨论两种情况：
1. 元数据持久化仓库中不存在 ShardingSphere 的元数据，本地元数据将被存储到元数据持久化仓库。
2. 元数据持久化仓库中已存在与本地元数据不同的 ShardingSphere 的元数据，本地元数据将被元数据持久化仓库的元数据覆盖。

对元数据持久化仓库的配置需参考[元数据持久化仓库](../../../../common-config/builtin-algorithm/metadata-repository.cn.md)。

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

### 其他实现
具体可参考 https://github.com/apache/shardingsphere-plugin 。
