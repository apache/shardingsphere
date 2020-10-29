+++
title = "Build"
weight = 1
+++

## Binary Run

1. `git clone https://github.com/apache/shardingsphere-ui.git`;
1. Run `mvn clean install -Prelease`;
1. Get the package in `/shardingsphere-ui/shardingsphere-ui-distribution/target/apache-shardingsphere-${latest.release.version}-shardingsphere-ui-bin.tar.gz`;
1. After the decompression, run `bin/start.sh`;
1. visit `http://localhost:8088/`.

## Source Code Debug

ShardingSphere-UI use frontend and backend separately mode.

### backend

1. Main class is `org.apache.shardingsphere.ui.Bootstrap`;
1. visit `http://localhost:8088/`.

### frontend

1. `cd shardingsphere-ui-frontend/`;
1. run `npm install`;
1. run `npm run dev`;
1. visit `http://localhost:8080/`.

## Configuration

Configuration file of ShardingSphere-UI is conf/application.properties in distribution package. It is constituted by two parts.

1. Listening port;
1. authentication.

```properties
server.port=8088

user.admin.username=admin
user.admin.password=admin
```

## Notices

1. If you run the frontend project locally after a build with maven, you may fail to run it due to inconsistent version of node. 
You can clean up `node_modules/` directory and run it again. The error log is: 

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
