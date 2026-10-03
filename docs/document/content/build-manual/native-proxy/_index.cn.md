+++
title = "Native Proxy"
weight = 3
pre = "<b>10.3. </b>"
+++

在源码仓库根目录执行以下命令。

## 打包

```bash
./mvnw -B -pl distribution/proxy-native -am \
  '-P<profiles>' \
  -Dmaven.test.skip=true package
```

## Profile 选择

将 `<profiles>` 替换为所需 profile，多个值用逗号分隔。

| Profile                     | 生成的制品      | 说明                                                                                          |
|-----------------------------|------------|---------------------------------------------------------------------------------------------|
| `release.native`            | 原生发行包      | 需要 GraalVM 及原生编译工具链；依赖 profile 参见 [Proxy 的 Profile 选择](/cn/build-manual/proxy/#profile-选择)。 |
| `docker.build.native.linux` | Linux 原生镜像 | 需要 Docker；外层依赖 profile 不会传入容器，定制依赖需调整所用 Dockerfile 中的 Maven 命令。                             |

环境要求、构建命令、静态链接选项和平台限制见 [构建 GraalVM Native Image](/cn/user-manual/shardingsphere-proxy/startup/graalvm-native-image/)。

## 制品

原生发行包路径：

`distribution/proxy-native/target/apache-shardingsphere-<version>-shardingsphere-proxy-bin.tar.gz`

`<version>` 为所构建源码的项目版本。

发行包包含原生程序及配置。
