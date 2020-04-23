# ShardingSphere UI

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Build Status](https://builds.apache.org/job/shardingsphere-ui-dev/badge/icon)](https://builds.apache.org/job/shardingsphere-ui-dev/)
## 概述

ShardingSphere UI是[ShardingSphere](https://shardingsphere.apache.org/)的管理后台，包含了动态配置、数据编排等功能。

### ShardingSphere UI 前端

shardingsphere-ui-frontend模块基于[vue](https://github.com/vuejs/vue)，并使用了UI工具包[element](https://github.com/ElemeFE/element)。

* [shardingsphere-ui-frontend/README_ZH.md](shardingsphere-ui-frontend/README_ZH.md)

### ShardingSphere UI 后端

shardingsphere-ui-backend模块是一个标准的spring boot项目。

## 如何构建

```bash
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere/shardingsphere-ui/
mvn clean package -Prelease
```

从 `shardingsphere-ui/shardingsphere-ui-distribution/shardingsphere-ui-bin-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-ui-bin.tar.gz`中获取软件包。

