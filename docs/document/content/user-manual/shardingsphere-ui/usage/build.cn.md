+++
title = "部署运行"
weight = 1
+++

## 二进制运行

1. `git clone https://github.com/apache/shardingsphere.git`；
1. 运行 `mvn clean install -Prelease`；
1. 获取安装包 `/shardingsphere-ui/shardingsphere-ui-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-ui-bin.tar.gz`；
1. 解压缩后运行`bin/start.sh`；
1. 访问`http://localhost:8088/`。

## 源码调试模式

ShardingSphere-UI 采用前后端分离的方式。

### 后端

1. 后端程序执行入口为 `org.apache.shardingsphere.ui.Bootstrap`；
1. 访问 `http://localhost:8088/`。

### 前端

1. 进入 `shardingsphere-ui-frontend/` 目录；
1. 执行 `npm install`；
1. 执行 `npm run dev`；
1. 访问 `http://localhost:8080/`。

## 配置

ShardingSphere-UI 的配置文件为 `conf/application.properties`, 它由两部分组成。

1. 程序监听端口；
1. 登录身份验证信息。

```properties
server.port=8088

user.admin.username=admin
user.admin.password=admin
```

## 注意事项

1. 若使用 maven 构建后，再进行本地运行前端项目时，可能因为 node 版本不一致导致运行失败，可以清空 `node_modules/` 目录后重新运行。
错误日志如下：

```
ERROR  Failed to compile with 17 errors
error  in ./src/views/orchestration/module/instance.vue?vue&type=style&index=0&id=9e59b740&lang=scss&scoped=true&
Module build failed (from ./node_modules/sass-loader/dist/cjs.js):
Error: Missing binding /shardingsphere/shardingsphere-ui/shardingsphere-ui-frontend/node_modules/node-sass/vendor/darwin-x64-57/binding.node
Node Sass could not find a binding for your current environment: OS X 64-bit with Node.js 8.x
Found bindings for the following environments:
  - OS X 64-bit with Node.js 6.x
This usually happens because your environment has changed since running `npm install`.
Run `npm rebuild node-sass` to download the binding for your current environment.
```
