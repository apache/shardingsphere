+++
title = "构建 GraalVM Native Image(Alpha)"
weight = 2
+++

## 背景信息

本节主要介绍如何通过 `GraalVM CE` 的 `native-image` 命令行工具构建 ShardingSphere Proxy 的 `GraalVM Native Image`，
以及包含此 `GraalVM Native Image` 的 `Docker Image`。ShardingSphere Proxy 的 `GraalVM Native Image` 在本文即指代 ShardingSphere Proxy Native。

本节内容涉及的所有 Docker Image 均不通过 https://downloads.apache.org ，https://repository.apache.org 等 ASF 官方渠道进行分发。
Docker Image 仅在 `GitHub Packages`，`Docker Hub` 等下游渠道提供以方便使用。

ShardingSphere Proxy Native 可执行 DistSQL，这意味着实际上不需要任何定义逻辑数据库的 YAML 文件。
默认情况下，ShardingSphere Proxy Native 中仅包含，

1. 与 ShardingSphere Proxy 默认配置一致的，一系列 JAR 的编译后产物
2. ShardingSphere 维护的自有及部分第三方依赖的 GraalVM Reachability Metadata

本节内容假定处于以下的系统环境之一，

1. Linux（amd64，aarch64）
2. MacOS（amd64，aarch64/M1）
3. Windows（amd64）

本节内容依然受到 ShardingSphere JDBC 一侧的 [GraalVM Native Image](/cn/user-manual/shardingsphere-jdbc/graalvm-native-image) 的已记录内容的限制。

如果用户需要在 ShardingSphere Proxy Native 中使用第三方 JAR，或者使用 UPX 压缩编译后的 GraalVM Native Image，
则需要修改 Maven 模块 `org.apache.shardingsphere:shardingsphere-proxy-native-distribution` 的源码。
参考下文的`从源码构建`一节。

若不需要修改 ShardingSphere Proxy Native 的默认配置，开发者可从`通过夜间构建的 Docker Image 使用`一节开始处理。

## 通过夜间构建的 Docker Image 使用

包含 ShardingSphere Proxy Native 的 Docker Image 在 https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native 存在每夜构建。
ShardingSphere Proxy Native 的默认端口为 `3307`，配置文件从 `/opt/shardingsphere-proxy/conf` 加载。

夜间构建的 Docker Image 存在多种形态的 GraalVM Native Image 的变体 Docker Image Tag。

### 动态链接的 GraalVM Native Image

假设存在包含 `global.yaml` 的 `conf` 文件夹为 `./custom/conf`，
开发者可通过如下的 Docker Compose 文件对 `动态链接的 GraalVM Native Image` 形态的 ShardingSphere Proxy Native 进行测试。

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

作为补充，`ghcr.io/apache/shardingsphere-proxy-native:latest`的 Docker Image Tag 将指向 `动态链接的 GraalVM Native Image`。 

### 大部分静态链接的 GraalVM Native Image

此节内容仅限于支持运行 `linux/amd64` OS/Arch Containers 的 Container Runtime 使用。

假设存在包含 `global.yaml` 的 `conf` 文件夹为 `./custom/conf`，
开发者可通过如下的 Docker Compose 文件对 `大部分静态链接的 GraalVM Native Image` 形态的 ShardingSphere Proxy Native 进行测试。
只需为特定的，`动态链接的 GraalVM Native Image` 对应的 Docker Image Tag 加上 `-mostly` 后缀。

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af-mostly
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

### 完全静态链接的 GraalVM Native Image

此节内容仅限于支持运行 `linux/amd64` OS/Arch Containers 的 Container Runtime 使用。

假设存在包含 `global.yaml` 的 `conf` 文件夹为 `./custom/conf`，
开发者可通过如下的 Docker Compose 文件对 `完全静态链接的 GraalVM Native Image` 形态的 ShardingSphere Proxy Native 进行测试。
只需为特定的，`动态链接的 GraalVM Native Image` 对应的 Docker Image Tag 加上 `-static` 后缀。

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af-static
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

## 从源码构建

若从源码构建，开发者有2种选择，

1. 在不安装本地工具链的情况下，构建包含 ShardingSphere Proxy Native 产物的 `Linux Container`
2. 在安装本地工具链的情况下，构建包含 ShardingSphere Proxy Native 产物。对于 Windows，可通过此途径创建`.exe`形态的 GraalVM Native Image

### 使用存在自定义 SPI 实现的 JAR 或第三方依赖的 JAR

开发者可能需要使用存在自定义 SPI 实现的 JAR 或第三方依赖的 JAR。可在从源码构建前，修改 `distribution/proxy-native/pom.xml` 文件的 `dependencies` 部分。
一个添加 MySQL JDBC Driver 依赖的示例如下，相关 JAR 应预先置入本地 Maven 仓库或 Maven Central 等远程 Maven 仓库。

```xml
<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.3.0</version>
    </dependency>
</dependencies>
```

### 构建 Linux Container

#### 前提条件

贡献者必须在设备安装，

1. OpenJDK 17 或更高版本
2. 可运行 Linux Containers 的 Docker Engine

下文分别讨论在 Ubuntu 与 Windows 下可能的所需操作。

##### Ubuntu

假设贡献者处于新的 Ubuntu 22.04.5 LTS 实例下，且已配置 git。
可在 bash 通过如下命令利用 `SDKMAN!` 安装 OpenJDK 21。

```shell
sudo apt install unzip zip -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.7-ms
sdk use java 21.0.7-ms
```

可在 bash 通过如下命令安装 Rootful 模式的 Docker Engine。

```shell
sudo apt update && sudo apt upgrade --assume-yes
sudo apt-get remove docker.io docker-compose docker-compose-v2 docker-doc podman-docker containerd runc
sudo apt install ca-certificates curl --assume-yes
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

sudo tee /etc/apt/sources.list.d/docker.sources <<EOF
Types: deb
URIs: https://download.docker.com/linux/ubuntu
Suites: $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}")
Components: stable
Signed-By: /etc/apt/keyrings/docker.asc
EOF

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin --assume-yes
sudo groupadd docker
sudo usermod -aG docker $USER
newgrp docker

sudo tee /etc/docker/daemon.json <<EOF
{
  "log-driver": "local"
}
EOF

sudo systemctl restart docker.service
```

##### Windows

假设贡献者处于新的 Windows 11 Home 24H2 实例下，且已安装和配置 `git-for-windows/git` 和 `PowerShell/PowerShell`。

可在 Powershell 7 通过如下命令利用 `version-fox/vfox` 安装 OpenJDK 21。

```shell
winget install --id version-fox.vfox --source winget --exact
if (-not (Test-Path -Path $PROFILE)) { New-Item -Type File -Path $PROFILE -Force }; Add-Content -Path $PROFILE -Value 'Invoke-Expression "$(vfox activate pwsh)"'
# 此时需要打开新的 Powershell 7 终端
vfox add java
vfox install java@21.0.7-ms
vfox use --global java@21.0.7-ms
```

当 Windows 弹出窗口，要求允许类似 `C:\users\lingh\.version-fox\cache\java\v-21.0.7-ms\java-21.0.7-ms\bin\java.exe` 路径的应用通过 Windows 防火墙时，
应当批准。 
背景参考 https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c 。

可在 Powershell 7 通过如下命令启用 WSL2 并设置 `Ubuntu WSL` 为默认 Linux 发行版。

```shell
wsl --install
```

完成 WSL2 的启用后，通过如下的 PowerShell 7 命令下载和安装 `rancher-sandbox/rancher-desktop`，
并设置使用 `dockerd(moby)` 的 `Container Engine`。

```shell
[Environment]::SetEnvironmentVariable('DOCKER_API_VERSION','1.44','Machine')
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# 打开新的 PowerShell 7 终端
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false

@'
{
  "features": {
    "containerd-snapshotter": true
  },
  "log-driver": "local"
}
'@ | rdctl shell sudo tee /etc/docker/daemon.json

rdctl shutdown
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
```


#### 构建包含动态链接的 GraalVM Native Image 的 Docker Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-DskipTests" clean package
```

一个可能的 Docker Compose 示例为，

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

#### 构建包含大部分静态链接的 GraalVM Native Image 的 Docker Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-Dproxy.native.dockerfile=Dockerfile-linux-mostly" "-Dproxy.native.image.tag=5.5.3-SNAPSHOT-mostly" "-DskipTests" clean package
```

一个可能的 Docker Compose 示例为，

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT-mostly
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

#### 构建包含完全静态链接的 GraalVM Native Image 的 Docker Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-Dproxy.native.dockerfile=Dockerfile-linux-static" "-Dproxy.native.image.tag=5.5.3-SNAPSHOT-static" "-DskipTests" clean package
```

一个可能的 Docker Compose 示例为，

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT-static
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

### 仅构建产物

#### 前提条件

贡献者必须在设备安装，

1. GraalVM CE 24.0.2，或与 GraalVM CE 24.0.2 兼容的 GraalVM 下游发行版。以 [GraalVM Native Image](/cn/user-manual/shardingsphere-jdbc/graalvm-native-image) 为准。
2. 编译 GraalVM Native Image 所需要的本地工具链。以 https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites 为准。

在 Ubuntu 与 Windows 下可能的所需操作与[开发和测试](/cn/user-manual/shardingsphere-jdbc/graalvm-native-image/development)一致。
但不需要安装 Container Runtime。

##### 静态编译所需的本地工具链

开发者如需构建 `大部分静态链接的 GraalVM Native Image` 或 `完全静态链接的 GraalVM Native Image`，
则需要按 https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/ 要求，从源代码构建 musl。

#### 构建动态链接的 GraalVM Native Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" clean package
```

#### 构建大部分静态链接的 GraalVM Native Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" "-DbuildArgs=-H:+UnlockExperimentalVMOptions,-H:+AddAllCharsets,-H:+IncludeAllLocales,--static-nolibc" clean package
```

#### 构建完全静态链接的 GraalVM Native Image

可执行如下命令构建。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" "-DbuildArgs=-H:+UnlockExperimentalVMOptions,-H:+AddAllCharsets,-H:+IncludeAllLocales,--static,--libc=musl" clean package
```

#### 使用 GraalVM Native Image

无论 GraalVM Native Image 属于什么变体，通过命令行启动 Native Image, 都需要带上 3 个参数，

1. 第 1 个参数为 ShardingSphere Proxy Native 使用的端口，
2. 第 2 个参数为用户编写的包含 `global.yaml` 配置文件的文件夹，
3. 第 3 个参数为要侦听的主机，如果为 `0.0.0.0` 则允许任意数据库客户端均可访问 ShardingSphere Proxy Native。

已完成构建的 GraalVM Native Image 的二进制文件仅可设置命令行参数。这意味着，

1. 用户仅可在构建 GraalVM Native Image 的过程中设置 JVM 参数
2. 用户无法针对已完成构建的 GraalVM Native Image 的二进制文件设置 JVM 参数

Ubuntu 下假设已存在包含 `global.yaml` 的 `conf` 文件夹为 `/tmp/conf`，可能的示例为，

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.3-SNAPSHOT-shardingsphere-proxy-bin/bin
./proxy-native "3307" "/tmp/conf" "0.0.0.0"
```

Windows 下假设已存在包含 `global.yaml` 的 `conf` 文件夹为 `C:\Users\shard\Downloads\conf`，可能的示例为，

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.3-SNAPSHOT-shardingsphere-proxy-bin/bin
./proxy-native.exe "3307" "C:\Users\shard\Downloads\conf" "0.0.0.0"
```

## 使用限制

### GraalVM Native Image 变体选择

一般情况下开发者仅需使用`动态链接的 GraalVM Native Image`。

当开发者仅使用可运行 `linux/amd64` OS/Arch Containers 的 Container Runtime，并希望获得更小体积的 Docker Image 时，
可考虑使用 `大部分静态链接的 GraalVM Native Image` 或 `完全静态链接的 GraalVM Native Image`。
背景参考 https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/ 和 https://github.com/oracle/graal/issues/2589 。
大部分静态链接的可执行文件，是 `golang/go` 推广的，针对静态链接 musl libc 实现的另一种替代方案。

### 可观察性

针对 ShardingSphere Proxy Native，其提供的可观察性的能力与[可观察性](/cn/user-manual/shardingsphere-proxy/observability)并不一致。

用户可以使用 https://www.graalvm.org/latest/reference-manual/tools/ 提供的一系列命令行工具或可视化工具观察 GraalVM Native Image 的内部行为，
并根据其要求在 Linux 下使用 VSCode 完成 Debug 工作。如果用户正在使用 IntelliJ IDEA 并且希望调试生成的 GraalVM Native Image，
用户可以关注 https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java 及其后继。
如果用户使用的不是 Linux，则无法对 GraalVM Native Image 进行 Debug，请关注尚未关闭的 https://github.com/oracle/graal/issues/5648 。

对于使用 `ShardingSphere Agent` 等 Java Agent 的情形， GraalVM 的 `native-image` 组件尚未完全支持在构建 Native Image 时使用 javaagent，
用户需要关注尚未关闭的 https://github.com/oracle/graal/issues/8177 。
若用户期望在 ShardingSphere Proxy Native 下使用这类 Java Agent，则需要关注 https://github.com/oracle/graal/pull/8077 涉及的变动。

### `linux/riscv64` OS/Arch 限制

当前，ShardingSphere Proxy Native 未提供在 `linux/riscv64` OS/Arch 上的可用性。如果开发者使用 `linux/riscv64` 设备，
应参考 https://medium.com/graalvm/graalvm-native-image-meets-risc-v-899be38eddd9 的处理修改 Proxy Native 的构建配置。

自 https://github.com/oracle/graal/issues/6855 开始，`LLVM backend` 需要从 GraalVM 的源码构建才可使用。
参考 https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core.graal.llvm/src/com/oracle/svm/core/graal/llvm/LLVMBackend.md 。

### Windows Containers 限制

ShardingSphere Proxy Native 可通过包含 `Microsoft.VisualStudio.2022.Community` 的本地工具链，在 Windows 上开箱即用地构建 GraalVM Native Image。

当前受 https://github.com/graalvm/container/issues/106 影响，
ShardingSphere 暂时不为通过 Windows 编译的 `动态链接的 GraalVM Native Image` 提供构建 Docker Image 所需的构建配置。

### Wasm 模块限制

尽管 `Oracle GraalVM Early Access Builds For JDK 26 EA 3` 已支持构建 `Wasm 模块`形态的 GraalVM Native Image，
但 ShardingSphere 尚未准备好在 OpenJDK 26 下测试 CI。

当前，ShardingSphere Proxy Native 未提供编译为 `Wasm 模块` 所需的构建配置。
