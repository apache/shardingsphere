+++
title = "开发和测试"
weight = 6
+++

本文旨在向潜在贡献者，介绍如何参与开发和贡献与 ShardingSphere 相关的 GraalVM Reachability Metadata。

## 背景信息

当贡献者发现缺少与 ShardingSphere 无关的第三方库的 GraalVM Reachability Metadata 时，
最优解是在 https://github.com/oracle/graalvm-reachability-metadata 打开新的 issue，并提交包含依赖的，第三方库缺失的 GraalVM Reachability Metadata 的 PR。

为了简化流程，ShardingSphere 在 Maven 模块 `shardingsphere-infra-reachability-metadata` 主动托管了部分第三方库的 GraalVM Reachability Metadata。

ShardingSphere 对在 GraalVM Native Image 下的可用性的验证，是通过 GraalVM Native Build Tools 的 Maven Plugin 子项目来完成的。
通过在 JVM 下运行单元测试，为单元测试打上 Junit 标签， 
此后构建为 GraalVM Native Image 进行 nativeTest 来测试在 GraalVM Native Image 下的单元测试覆盖率。

ShardingSphere 定义了，
1. `shardingsphere-test-native` 的 Maven Module，用于为 nativeTest 提供小型的单元测试子集。此单元测试子集避免了使用 nativeTest 下无法使用的第三方库。
2. `nativeTestInShardingSphere` 的 Maven Profile，用于为 `shardingsphere-test-native` 模块编译单元测试所需的 GraalVM Native Image 和执行 nativeTest。
3. `generateMetadata` 的 Maven Profile，用于在 GraalVM JIT Compiler 下携带 GraalVM Tracing Agent 执行单元测试，以采集初步的 GraalVM Reachability Metadata。

## 前提条件

贡献者必须在设备安装，

1. GraalVM CE 24.0.2，或与 GraalVM CE 24.0.2 兼容的 GraalVM 下游发行版。以 [GraalVM Native Image](/cn/user-manual/shardingsphere-jdbc/graalvm-native-image) 为准。
2. 编译 GraalVM Native Image 所需要的本地工具链。以 https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites 为准。
3. 可运行 Linux Containers 的 Docker Engine，或与 testcontainers-java 兼容的 Container Runtime。以 https://java.testcontainers.org/supported_docker_environment/ 为准。

本文不讨论 `LLVM Backend for Native Image`。下文分别讨论在 Ubuntu，Windows 与 Windows Server 下可能的所需操作。

### Ubuntu

假设贡献者处于新的 Ubuntu 22.04.5 LTS 实例下，且已配置 git。

可在 bash 通过如下命令利用 `SDKMAN!` 安装 GraalVM CE。

```shell
sudo apt install unzip zip -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 24.0.2-graalce
sdk use java 24.0.2-graalce
```

可在 bash 通过如下命令安装编译 GraalVM Native Image 所需要的本地工具链。

```shell
sudo apt-get install build-essential zlib1g-dev -y
```

可在 bash 通过如下命令安装 Rootful 模式的 Docker Engine。本文不讨论更改 `/etc/docker/daemon.json` 的默认 logging driver。

```shell
sudo apt update && sudo apt upgrade -y
sudo apt-get remove docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc
cd /tmp/
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y
sudo groupadd docker
sudo usermod -aG docker $USER
newgrp docker
```

### Windows

假设贡献者处于新的 Windows 11 Home 24H2 实例下，且已安装和配置 `git-for-windows/git` 和 `PowerShell/PowerShell`。

可在 Powershell 7 通过如下命令利用 `version-fox/vfox` 安装 GraalVM CE。

```shell
winget install --id version-fox.vfox --source winget --exact
if (-not (Test-Path -Path $PROFILE)) { New-Item -Type File -Path $PROFILE -Force }; Add-Content -Path $PROFILE -Value 'Invoke-Expression "$(vfox activate pwsh)"'
# 此时需要打开新的 Powershell 7 终端
vfox add java
vfox install java@24.0.2-graalce
vfox use --global java@24.0.2-graalce
```

当 Windows 弹出窗口，要求允许类似 `C:\users\shard\.version-fox\cache\java\v-24.0.2-graalce\java-24.0.2-graalce\bin\java.exe` 路径的应用通过 Windows 防火墙时，应当批准。
背景参考 https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c 。

可在 Powershell 7 通过如下命令安装编译 GraalVM Native Image 所需要的本地工具链。**特定情况下，开发者可能需要为 Visual Studio 的使用购买许可证。**

```shell
winget install --id Microsoft.VisualStudio.2022.Community --source winget --exact
```

打开 `Visual Studio Installer` 以修改 `Visual Studio Community 2022` 的 `工作负荷`，勾选 `桌面应用与移动应用` 的 `使用 C++ 的桌面开发` 后点击`修改`。

可在 Powershell 7 通过如下命令启用 WSL2 并设置 `Ubuntu WSL` 为默认 Linux 发行版。

```shell
wsl --install
```

完成 WSL2 的启用后，通过如下的 PowerShell 7 命令下载和安装 `rancher-sandbox/rancher-desktop`，
并设置使用 `dockerd(moby)` 的 `Container Engine`。

```shell
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# 打开新的 PowerShell 7 终端
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
```

本文不讨论更改 Linux 发行版 `rancher-desktop` 的 `/etc/docker/daemon.json` 的默认 logging driver。

### Windows Server

对于通常的 `Windows Server 2025` 实例，操作等同于 `Windows 11 Home 24H2`。

但受 https://github.com/rancher-sandbox/rancher-desktop/issues/3999 影响，
如果开发者正在使用的 `Windows Server 2025` 实例包含可运行 Windows Containers 的 Docker Engine，
则需要使用 Microsoft 提供的脚本卸载 Docker Engine 后，再安装 Rancher Desktop。
可在 PowerShell 7 执行如下命令，

```shell
iex "& { $(irm https://raw.githubusercontent.com/microsoft/Windows-Containers/refs/heads/Main/helpful_tools/Install-DockerCE/uninstall-docker-ce.ps1) } -Force"
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# 打开新的 PowerShell 7 终端
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
```

这类操作常见于 `windows-latest` 的 GitHub Actions Runner 中。

## 处理单元测试

### 在 GraalVM JIT Compiler 下执行单元测试

若仅需在 GraalVM JIT Compiler 下，即在避开编译 GraalVM Native Image 的情况下执行 nativeTest 相关的单元测试，可执行如下命令。

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -e -T 1C clean verify
```

### 在 GraalVM Native Image 下执行单元测试

可通过如下命令为 `shardingsphere-test-native` 子模块编译单元测试所需的 GraalVM Native Image 和执行 nativeTest。

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PnativeTestInShardingSphere -e -T 1C clean verify
```

当 Windows 弹出窗口，要求允许类似 `C:\users\shard\shardingsphere\test\native\target\native-tests.exe.exe` 路径的应用通过 Windows 防火墙时，应当批准。
背景参考 https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c 。

### 生成和更改 GraalVM Reachability Metadata

如果 nativeTest 执行失败，应为单元测试生成初步的 GraalVM Reachability Metadata，
并手动调整 `shardingsphere-infra-reachability-metadata` 子模块的 classpath 中，
`META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` 文件夹下的内容，以修复 nativeTest。
如有需要，可使用 `org.junit.jupiter.api.condition.DisabledInNativeImage` 注解或 `org.graalvm.nativeimage.imagecode` 的 System Property，
以屏蔽部分单元测试在 GraalVM Native Image 下运行。

`generateMetadata` 的 Maven Profile 将在 `shardingsphere-infra-reachability-metadata` 子模块的 classpath 中，
`META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` 文件夹下，
生成或覆盖已有的 GraalVM Reachability Metadata 文件。可通过如下命令简单处理此流程。

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -e -T 1C clean verify native:metadata-copy
```

贡献者仍可能需要手动调整具体的 JSON 条目，并适时调整 Maven Profile 和 GraalVM Tracing Agent 的 Filter 链。
针对 `shardingsphere-infra-reachability-metadata` 子模块，
手动增删改动的 JSON 条目应位于 `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` 文件夹下，
而 `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` 中的条目仅应由 `generateMetadata` 的 Maven Profile 生成。

对于测试类和测试文件独立使用的 GraalVM Reachability Metadata，贡献者应该放置到 `shardingsphere-test-native` 子模块的 classpath 的
`META-INF/native-image/shardingsphere-test-native-test-metadata/` 下。

## 已知限制

### `reachability-metadata.json` 限制

受 https://github.com/apache/shardingsphere/issues/33206 影响，
开发者执行 `./mvnw -PgenerateMetadata -T 1C -e clean test native:metadata-copy` 后，
`infra/reachability-metadata/src/main/resources/META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/reachability-metadata.json` 会生成不必要的包含绝对路径的 JSON 条目，

对于 Ubuntu，类似如下，

```json
{
  "resources": [
    {
      "condition": {
        "typeReached": "org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"
      },
      "glob": "home/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/mysql/"
    },
    {
      "condition": {
        "typeReached": "org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"
      },
      "glob": "home/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/mysql//global.yaml"
    },
    {
      "condition": {
        "typeReached": "org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"
      },
      "glob": "home/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/postgresql/"
    },
    {
      "condition": {
        "typeReached": "org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader"
      },
      "glob": "home/runner/work/shardingsphere/shardingsphere/test/native/src/test/resources/test-native/yaml/proxy/databases/postgresql//global.yaml"
    }
  ]
}
```

需要为 ShardingSphere 提交 PR 的贡献者应始终手动删除这些包含绝对路径的 JSON 条目，并等待 https://github.com/oracle/graal/issues/8417 被解决。

### 单元测试库限制

对于 `shardingsphere-test-native` 的 Maven Module，
应避免使用 `io.kotest:kotest-runner-junit5-jvm:5.5.4` 等在 Junit 的 `test listener` mode 下存在 `failed to discover tests` 问题的测试库。

由于 Mockito Inline 无法在 GraalVM Native Image 下运行，应避免在此 Maven 模块的单元测试使用 Mockito。

对于 testcontainers，对 `org.testcontainers.utility.MountableFile#forClasspathResource(String)` 的使用应更改为 
`org.testcontainers.utility.MountableFile#forHostPath(java.nio.file.Path)`，
以避开 https://github.com/testcontainers/testcontainers-java/issues/7954 的影响。举例如下，

```java
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;
class SolutionTest {
    @Test
    void test() {
        try (PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17.5-bookworm").withCopyFileToContainer(
                MountableFile.forClasspathResource("test-native/sh/postgres.sh"), 
                "/docker-entrypoint-initdb.d/postgres.sh")) {
            container.start();
        }
    }
}
```

应更改为，

```java
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;
import java.nio.file.Paths;
class SolutionTest {
    @Test
    void test() {
        try (PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:17.5-bookworm").withCopyFileToContainer(
                MountableFile.forHostPath(Paths.get("src/test/resources/test-native/sh/postgres.sh").toAbsolutePath()),
                "/docker-entrypoint-initdb.d/postgres.sh")) {
            container.start();
        }
    }
}
```

### 单元测试的已知问题

受 https://github.com/apache/shardingsphere/issues/35052 影响，
`org.apache.shardingsphere.test.natived.jdbc.modes.cluster.EtcdTest` 的单元测试无法在通过 `Windows 11 Home 24H2` 编译的 GraalVM Native Image 下运行。

受 https://github.com/apache/incubator-seata/issues/7523 影响，
`org.apache.shardingsphere.test.natived.proxy.transactions.base.SeataTest` 已被禁用，
因为在 Github Actions Runner 执行此单元测试将导致其他单元测试出现 JDBC 连接泄露。

### `CodeCachePoolMXBean` 限制

当前执行 `./mvnw -PnativeTestInShardingSphere -e -T 1C clean verify` 将涉及到针对 `com.oracle.svm.core.code.CodeCachePoolMXBean` 的警告日志，

```shell
org.graalvm.nativeimage.MissingReflectionRegistrationError: The program tried to reflectively access

   com.oracle.svm.core.code.CodeCachePoolMXBean$CodeAndDataPool.getConstructors()

without it being registered for runtime reflection. Add com.oracle.svm.core.code.CodeCachePoolMXBean$CodeAndDataPool.getConstructors() to the reflection metadata to solve this problem. See https://www.graalvm.org/latest/reference-manual/native-image/metadata/#reflection for help.
  java.base@24.0.2/java.lang.Class.getConstructors(DynamicHub.java:1128)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.findConstructors(MBeanIntrospector.java:459)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.getClassMBeanInfo(MBeanIntrospector.java:430)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.getMBeanInfo(MBeanIntrospector.java:389)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanSupport.<init>(MBeanSupport.java:137)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MXBeanSupport.<init>(MXBeanSupport.java:66)
  java.management@24.0.2/javax.management.StandardMBean.construct(StandardMBean.java:174)
  java.management@24.0.2/javax.management.StandardMBean.<init>(StandardMBean.java:268)
org.graalvm.nativeimage.MissingReflectionRegistrationError: The program tried to reflectively access

   com.oracle.svm.core.code.CodeCachePoolMXBean$NativeMetadataPool.getConstructors()

without it being registered for runtime reflection. Add com.oracle.svm.core.code.CodeCachePoolMXBean$NativeMetadataPool.getConstructors() to the reflection metadata to solve this problem. See https://www.graalvm.org/latest/reference-manual/native-image/metadata/#reflection for help.
  java.base@24.0.2/java.lang.Class.getConstructors(DynamicHub.java:1128)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.findConstructors(MBeanIntrospector.java:459)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.getClassMBeanInfo(MBeanIntrospector.java:430)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanIntrospector.getMBeanInfo(MBeanIntrospector.java:389)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MBeanSupport.<init>(MBeanSupport.java:137)
  java.management@24.0.2/com.sun.jmx.mbeanserver.MXBeanSupport.<init>(MXBeanSupport.java:66)
  java.management@24.0.2/javax.management.StandardMBean.construct(StandardMBean.java:174)
  java.management@24.0.2/javax.management.StandardMBean.<init>(StandardMBean.java:268)
```

相关警告在 `GraalVM CE For JDK 24.0.2` 上无法避免。
因为 `com.oracle.svm.core.code.CodeCachePoolMXBean` 的无参构造函数通过 Java 类 `org.graalvm.nativeimage.Platform.HOSTED_ONLY` 被标记为无论实际的 Platform 是什么，
仅在 Native Image 生成期间可见，且无法在 Runtime 使用的元素。
