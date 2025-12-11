+++
title = "Development and test"
weight = 6
+++

This article aims to introduce potential developers how to participate in the development and contribute to GraalVM
Reachability Metadata related to ShardingSphere.

## Background Information

When developer find that GraalVM Reachability Metadata of a third-party library not related to ShardingSphere is missing,
the best solution is to open a new issue at https://github.com/oracle/graalvm-reachability-metadata and submit a PR containing the GraalVM Reachability Metadata of the dependent, 
missing third-party library.

To simplify the process, ShardingSphere proactively hosts GraalVM Reachability Metadata of some third-party libraries in the Maven module `shardingsphere-infra-reachability-metadata`.

ShardingSphere verifies the availability under GraalVM Native Image through the Maven Plugin subproject of GraalVM Native Build Tools.
By running unit tests under JVM and tagging the unit tests with Junit,
then build it as GraalVM Native Image for nativeTest to test the unit test coverage under GraalVM Native Image.

ShardingSphere defines,
1. `shardingsphere-test-native` Maven Module, which is used to provide a small unit test subset for nativeTest. This unit test subset avoids the use of third-party libraries that cannot be used under nativeTest.
2. `nativeTestInShardingSphere` Maven Profile, which is used to compile the GraalVM Native Image required for unit testing for the `shardingsphere-test-native` module and execute nativeTest.
3. `generateMetadata` Maven Profile, which is used to carry GraalVM Tracing Agent to execute unit tests under GraalVM JIT Compiler to collect preliminary GraalVM Reachability Metadata.

## Prerequisites

Developer must have installed on their devices,

1. GraalVM CE 24.0.2, or a GraalVM downstream distribution compatible with GraalVM CE 24.0.2. Refer to [GraalVM Native Image](/en/user-manual/shardingsphere-jdbc/graalvm-native-image).
2. The native toolchain required to compile the GraalVM Native Image. Refer to https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites .
3. Docker Engine that can run Linux Containers, or a Container Runtime compatible with testcontainers-java. Refer to https://java.testcontainers.org/supported_docker_environment/ .

This article does not discuss `LLVM Backend for Native Image`. The following sections discuss the possible required operations under Ubuntu, Windows, and Windows Server.

### Ubuntu

It is assumed that the developer is on a fresh Ubuntu 22.04.5 LTS instance with git configured.

GraalVM CE can be installed using `SDKMAN!` in bash using the following command.

```shell
sudo apt install unzip zip -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 24.0.2-graalce
sdk use java 24.0.2-graalce
```

Developer can use the following command in bash to install the local toolchain required to compile GraalVM Native Image.

```shell
sudo apt-get install build-essential zlib1g-dev -y
```

Developer can install Docker Engine in rootful mode by running the following command in bash. 
This article does not discuss changing the default logging driver in `/etc/docker/daemon.json`.

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

It is assumed that the developer is on a fresh Windows 11 Home 24H2 instance with `git-for-windows/git` and `PowerShell/PowerShell` installed and configured.

GraalVM CE can be installed using `version-fox/vfox` in Powershell 7 using the following command.

```shell
winget install --id version-fox.vfox --source winget --exact
if (-not (Test-Path -Path $PROFILE)) { New-Item -Type File -Path $PROFILE -Force }; Add-Content -Path $PROFILE -Value 'Invoke-Expression "$(vfox activate pwsh)"'
# At this time, developer need to open a new Powershell 7 terminal
vfox add java
vfox install java@24.0.2-graalce
vfox use --global java@24.0.2-graalce
```

When Windows pops up a window asking developer to allow an application with a path like `C:\users\shard\.version-fox\cache\java\v-24.0.2-graalce\java-24.0.2-graalce\bin\java.exe` to pass through Windows Firewall,
developer should approve it.
Background reference https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c .

Developer can install the local toolchain required to compile GraalVM Native Image using the following command in Powershell 7. 
**In certain cases, developer may need to purchase a license for the use of Visual Studio.**

```shell
winget install --id Microsoft.VisualStudio.2022.Community --source winget --exact
```

Open `Visual Studio Installer` to modify `Workloads` of `Visual Studio Community 2022`, 
check `Desktop development with C++` of `Desktop apps and mobile apps` and click `Change`.

Developer can enable WSL2 and set `Ubuntu WSL` as the default Linux distribution in Powershell 7 with the following command.

```shell
wsl --install
```

After enabling WSL2, download and install `rancher-sandbox/rancher-desktop` using the following PowerShell 7 command,
and configure it to use the `dockerd(moby)` `Container Engine`.

```shell
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# Open a new PowerShell 7 terminal
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
```

This article does not discuss changing the default logging driver in `/etc/docker/daemon.json` of the Linux distribution's `rancher-desktop`.

### Windows Server

For standard `Windows Server 2025` instances, the operation is equivalent to that for `Windows 11 Home 24H2`.

However, due to https://github.com/rancher-sandbox/rancher-desktop/issues/3999 ,
if developers are using `Windows Server 2025` instances that include Docker Engine, which can run Windows Containers,
they will need to uninstall Docker Engine using the script provided by Microsoft before installing Rancher Desktop.
You can execute the following command in PowerShell 7:

```shell
iex "& { $(irm https://raw.githubusercontent.com/microsoft/Windows-Containers/refs/heads/Main/helpful_tools/Install-DockerCE/uninstall-docker-ce.ps1) } -Force"
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# Open a new PowerShell 7 terminal
rdctl start --application.start-in-background --container-engine.name=moby --kubernetes.enabled=false
```

This type of operation is commonly found in the GitHub Actions Runner for `windows-latest`.

## Handling unit tests

### Execute unit tests under GraalVM JIT Compiler

If developer only need to execute nativeTest related unit tests under GraalVM JIT Compiler, 
that is, avoid compiling GraalVM Native Image, developer can execute the following command.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -e -T 1C clean verify
```

### Execute unit tests under GraalVM Native Image

Developer can use the following commands to compile the GraalVM Native Image required for unit testing for the `shardingsphere-test-native` submodule and execute nativeTest.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PnativeTestInShardingSphere -e -T 1C clean verify
```

When Windows pops up a window asking developer to allow an app with a path like `C:\users\shard\shardingsphere\test\native\target\native-tests.exe.exe` to pass through Windows Firewall, 
developer should approve it.
Background reference https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c .

### Generate and modify GraalVM Reachability Metadata

If nativeTest fails, generate preliminary GraalVM Reachability Metadata for the unit tests, 
and manually adjust the contents of the `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/`
folder in the classpath of the `shardingsphere-infra-reachability-metadata` submodule to fix nativeTest. 
If necessary, 
use the `org.junit.jupiter.api.condition.DisabledInNativeImage` annotation or the `org.graalvm.nativeimage.imagecode` System Property to block some unit tests from running under the GraalVM Native Image.

The `generateMetadata` Maven Profile will generate or overwrite the existing GraalVM Reachability Metadata file in the `shardingsphere-infra-reachability-metadata` submodule's classpath, 
under the `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` folder. 
This process can be easily handled by the following command.

```bash
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -PgenerateMetadata -e -T 1C clean verify native:metadata-copy
```

Developer may still need to manually adjust specific JSON entries and adjust the filter chain of Maven Profile and GraalVM Tracing Agent as appropriate.
For the `shardingsphere-infra-reachability-metadata` submodule, manually added, deleted, 
and modified JSON entries should be located in the `META-INF/native-image/org.apache.shardingsphere/shardingsphere-infra-reachability-metadata/` folder,
while the entries in `META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/` should only be generated by the Maven Profile of `generateMetadata`.

For GraalVM Reachability Metadata used independently by test classes and test files,
developer should place it in the classpath of the `shardingsphere-test-native` submodule under `META-INF/native-image/shardingsphere-test-native-test-metadata/`.

## Known limitations

### `reachability-metadata.json` limitations

Affected by https://github.com/apache/shardingsphere/issues/33206,
after developers execute `./mvnw -PgenerateMetadata -T 1C -e clean test native:metadata-copy`,
`infra/reachability-metadata/src/main/resources/META-INF/native-image/org.apache.shardingsphere/generated-reachability-metadata/reachability-metadata.json` will generate unnecessary JSON entries containing absolute paths.

For Ubuntu, it is similar to the following,

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

Contributors who need to submit PRs for ShardingSphere should always manually remove these JSON entries containing absolute paths
and wait for https://github.com/oracle/graal/issues/8417 to be resolved.

### Unit test library limitations

For the Maven Module of `shardingsphere-test-native`,
Avoid using test libraries such as `io.kotest:kotest-runner-junit5-jvm:5.5.4` which have the `failed to discover tests` issue in Junit's `test listener` mode.

Since Mockito Inline cannot run under GraalVM Native Image, avoid using Mockito in unit tests of this Maven module.

For testcontainers, the use of `org.testcontainers.utility.MountableFile#forClasspathResource(String)` should be changed to
`org.testcontainers.utility.MountableFile#forHostPath(java.nio.file.Path)`,
to avoid the impact of https://github.com/testcontainers/testcontainers-java/issues/7954. For example,

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

Should be changed to,

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

### Known issues with unit testing

Affected by https://github.com/apache/shardingsphere/issues/35052 , 
the unit test of `org.apache.shardingsphere.test.natived.jdbc.modes.cluster.EtcdTest` cannot be run under GraalVM Native Image compiled by `Windows 11 Home 24H2`.

Due to https://github.com/apache/incubator-seata/issues/7523 , 
`org.apache.shardingsphere.test.natived.proxy.transactions.base.SeataTest` has been disabled.
This is because executing this unit test in the Github Actions Runner will cause JDBC connection leaks in other unit tests.

### `CodeCachePoolMXBean` limitation

Currently executing `./mvnw -PnativeTestInShardingSphere -e -T 1C clean verify` will involve warning logs for `com.oracle.svm.core.code.CodeCachePoolMXBean`.

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

The relevant warning cannot be avoided on `GraalVM CE For JDK 24.0.2`.
Because the no-argument constructor of `com.oracle.svm.core.code.CodeCachePoolMXBean` is marked as an element that is only visible during Native Image generation and cannot be used at Runtime, 
regardless of the actual Platform,
through the Java class `org.graalvm.nativeimage.Platform.HOSTED_ONLY`.
