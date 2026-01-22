+++
title = "Build GraalVM Native Image(Alpha)"
weight = 2
+++

## Background information

This section mainly introduces how to build the `GraalVM Native Image` of ShardingSphere Proxy through the `native-image` command line tool of `GraalVM CE`,
and the `Docker Image` containing this `GraalVM Native Image`. The `GraalVM Native Image` of ShardingSphere Proxy refers to ShardingSphere Proxy Native in this article.

All Docker Images involved in this section are not distributed through ASF official channels such as https://downloads.apache.org and https://repository.apache.org.

Docker Images are only provided in downstream channels such as `GitHub Packages` and `Docker Hub` for easy use.

ShardingSphere Proxy Native can execute DistSQL, which means that no YAML file defining the logical database is actually required.
By default, ShardingSphere Proxy Native only contains,

1. A series of JAR compiled products consistent with the default configuration of ShardingSphere Proxy
2. ShardingSphere's own and some third-party dependent GraalVM Reachability Metadata

This section assumes one of the following system environments,

1. Linux (amd64, aarch64)
2. MacOS (amd64, aarch64/M1)
3. Windows (amd64)

This section is still limited by the recorded content of [GraalVM Native Image](/en/user-manual/shardingsphere-jdbc/graalvm-native-image) on the ShardingSphere JDBC side.

If users need to use third-party JAR in ShardingSphere Proxy Native, or use UPX to compress and compile GraalVM Native Image,
then they need to modify the source code of the Maven module `org.apache.shardingsphere:shardingsphere-proxy-native-distribution`.
Refer to the `Build from source code` section below.

If you do not need to modify the default configuration of ShardingSphere Proxy Native, developers can start from the `Use through the nightly built Docker Image` section.

## Use through nightly built Docker Image

The Docker Image containing ShardingSphere Proxy Native is built nightly at https://github.com/apache/shardingsphere/pkgs/container/shardingsphere-proxy-native .

The default port of ShardingSphere Proxy Native is `3307`, and the configuration file is loaded from `/opt/shardingsphere-proxy/conf`.

The nightly built Docker Image has multiple variant Docker Image Tags of GraalVM Native Image.

### Dynamically linked GraalVM Native Image

Assuming that there is a `conf` folder containing `global.yaml` as `./custom/conf`,
developers can test ShardingSphere Proxy Native in the form of `dynamically linked GraalVM Native Image` through the following Docker Compose file.

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

As a supplement, the Docker Image Tag of `ghcr.io/apache/shardingsphere-proxy-native:latest` will point to the `dynamically linked GraalVM Native Image`.

### Mostly statically linked GraalVM Native Image

This section is limited to the Container Runtime that supports running `linux/amd64` OS/Arch Containers.

Assuming that there is a `conf` folder containing `global.yaml` as `./custom/conf`,
developers can test ShardingSphere Proxy Native in the form of `mostly statically linked GraalVM Native Image` through the following Docker Compose file.
Just add the `-mostly` suffix to the Docker Image Tag corresponding to the specific, `dynamically linked GraalVM Native Image`.

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af-mostly
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

### Fully statically linked GraalVM Native Image

This section is limited to Container Runtime that supports running `linux/amd64` OS/Arch Containers.

Assuming that there is a `conf` folder containing `global.yaml` as `./custom/conf`,
Developers can test ShardingSphere Proxy Native in the form of `fully statically linked GraalVM Native Image` through the following Docker Compose file.
Just add the `-static` suffix to the Docker Image Tag corresponding to the specific, `dynamically linked GraalVM Native Image`.

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: ghcr.io/apache/shardingsphere-proxy-native:a2661a750be0301cb221ba8f549504f04cc8a5af-static
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

## Build from source code

If you build from source code, developers have two options,

1. Build a `Linux Container` containing ShardingSphere Proxy Native products without installing a local toolchain
2. Build a ShardingSphere Proxy Native product with a local toolchain installed. For Windows, you can create a GraalVM Native Image in the form of `.exe` in this way

### Use JARs with custom SPI implementations or third-party dependent JARs

Developers may need to use JARs with custom SPI implementations or third-party dependent JARs. Before building from source code, modify the `dependencies` section of the `distribution/proxy-native/pom.xml` file.
An example of adding a MySQL JDBC Driver dependency is as follows. The relevant JAR should be pre-placed in the local Maven repository or a remote Maven repository such as Maven Central.

```xml
<dependencies>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>9.3.0</version>
    </dependency>
</dependencies>
```

### Build Linux Container

#### Prerequisites

Contributors must have installed on their devices,

1. OpenJDK 17 or higher

2. Docker Engine that can run Linux Containers

The following sections discuss possible required operations under Ubuntu and Windows respectively.

##### Ubuntu

It is assumed that the contributor is on a fresh Ubuntu 22.04.5 LTS instance with git configured.

OpenJDK 21 can be installed using `SDKMAN!` in bash using the following command.

```shell
sudo apt install unzip zip -y
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 21.0.7-ms
sdk use java 21.0.7-ms
```

You can install Docker Engine in rootful mode by running the following command in bash.

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

Assuming the contributor is on a fresh Windows 11 Home 24H2 instance with `git-for-windows/git` and `PowerShell/PowerShell` installed and configured.

OpenJDK 21 can be installed using `version-fox/vfox` in Powershell 7 using the following command.

```shell
winget install --id version-fox.vfox --source winget --exact
if (-not (Test-Path -Path $PROFILE)) { New-Item -Type File -Path $PROFILE -Force }; Add-Content -Path $PROFILE -Value 'Invoke-Expression "$(vfox activate pwsh)"'
# At this time, you need to open a new Powershell 7 terminal
vfox add java
vfox install java@21.0.7-ms
vfox use --global java@21.0.7-ms
```

When Windows pops up a window asking you to allow an application with a path like `C:\users\lingh\.version-fox\cache\java\v-21.0.7-ms\java-21.0.7-ms\bin\java.exe` to pass through Windows Firewall,
you should approve it.
Background reference https://support.microsoft.com/en-us/windows/risks-of-allowing-apps-through-windows-firewall-654559af-3f54-3dcf-349f-71ccd90bcc5c .

You can enable WSL2 and set `Ubuntu WSL` as the default Linux distribution in Powershell 7 with the following command.

```shell
wsl --install
```

After enabling WSL2, download and install `rancher-sandbox/rancher-desktop` using the following PowerShell 7 command, 
and configure `dockerd(moby)` to use the `Container Engine`.

```shell
[Environment]::SetEnvironmentVariable('DOCKER_API_VERSION','1.44','Machine')
winget install --id SUSE.RancherDesktop --source winget --skip-dependencies
# Open a new PowerShell 7 terminal
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

#### Build a Docker Image with a dynamically linked GraalVM Native Image

You can execute the following command to build.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-DskipTests" clean package
```

A possible Docker Compose example is,

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

#### Build a Docker Image containing most of the statically linked GraalVM Native Image

You can execute the following command to build.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-Dproxy.native.dockerfile=Dockerfile-linux-mostly" "-Dproxy.native.image.tag=5.5.3-SNAPSHOT-mostly" "-DskipTests" clean package
```

A possible Docker Compose example is,

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT-mostly
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

#### Build a Docker Image containing a fully statically linked GraalVM Native Image

You can execute the following command to build.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C "-Pdocker.build.native.linux" "-Dproxy.native.dockerfile=Dockerfile-linux-static" "-Dproxy.native.image.tag=5.5.3-SNAPSHOT-static" "-DskipTests" clean package
```

A possible Docker Compose example is,

```yaml
services:
  apache-shardingsphere-proxy-native:
    image: apache/shardingsphere-proxy-native:5.5.3-SNAPSHOT-static
    volumes:
      - ./custom/conf:/opt/shardingsphere-proxy/conf
    ports:
      - "3307:3307"
```

### Build only

#### Prerequisites

Contributors must have installed on their devices,

1. GraalVM CE 24.0.2, or a GraalVM downstream distribution compatible with GraalVM CE 24.0.2. Refer to [GraalVM Native Image](/en/user-manual/shardingsphere-jdbc/graalvm-native-image).
2. The native toolchain required to compile GraalVM Native Image. Refer to https://www.graalvm.org/latest/reference-manual/native-image/#prerequisites .

The possible required operations under Ubuntu and Windows are consistent with [Development and test](/en/user-manual/shardingsphere-jdbc/graalvm-native-image/development).
However, it is not necessary to install Container Runtime.

##### Native toolchain for static compilation

Developers who want to build a `mostly statically linked GraalVM Native Image` or a `fully statically linked GraalVM Native Image`,
will need to build musl from source as described in https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/ .

#### Build a dynamically linked GraalVM Native Image

You can execute the following command to build it.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" clean package
```

#### Build most statically linked GraalVM Native Images

You can execute the following command to build.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" "-DbuildArgs=-H:+UnlockExperimentalVMOptions,-H:+AddAllCharsets,-H:+IncludeAllLocales,--static-nolibc" clean package
```

#### Build a fully statically linked GraalVM Native Image

You can execute the following command to build.

```shell
git clone git@github.com:apache/shardingsphere.git
cd ./shardingsphere/
./mvnw -am -pl distribution/proxy-native -T1C -DskipTests "-Prelease.native" "-DbuildArgs=-H:+UnlockExperimentalVMOptions,-H:+AddAllCharsets,-H:+IncludeAllLocales,--static,--libc=musl" clean package
```

#### Use GraalVM Native Image

No matter what variant the GraalVM Native Image is, you need to bring 3 parameters to start the Native Image through the command line.

1. The first parameter is the port used by ShardingSphere Proxy Native,
2. The second parameter is the folder containing the `global.yaml` configuration file written by the user,
3. The third parameter is the host to listen to. If it is `0.0.0.0`, any database client can access ShardingSphere Proxy Native.

The binary file of the built GraalVM Native Image can only set command line parameters. This means that,

1. Users can only set JVM parameters during the process of building GraalVM Native Image
2. Users cannot set JVM parameters for the binary file of the built GraalVM Native Image

On Ubuntu, assuming that the `conf` folder containing `global.yaml` is `/tmp/conf`, possible example is,

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.3-SNAPSHOT-shardingsphere-proxy-bin/bin
./proxy-native "3307" "/tmp/conf" "0.0.0.0"
```

On Windows, assuming that a `conf` folder containing `global.yaml` already exists at `C:\Users\shard\Downloads\conf`, a possible example is,

```bash
cd ./shardingsphere/
cd ./distribution/proxy-native/target/apache-shardingsphere-5.5.3-SNAPSHOT-shardingsphere-proxy-bin/bin
./proxy-native.exe "3307" "C:\Users\shard\Downloads\conf" "0.0.0.0"
```

## Usage restrictions

### GraalVM Native Image variant selection

In general, developers only need to use `dynamically linked GraalVM Native Image`.

When developers only use the Container Runtime that can run `linux/amd64` OS/Arch Containers and want to get a smaller Docker Image,
consider using `mostly statically linked GraalVM Native Image` or `fully statically linked GraalVM Native Image`.

For background, see https://www.graalvm.org/latest/reference-manual/native-image/guides/build-static-executables/ and https://github.com/oracle/graal/issues/2589.
Mostly statically linked executables are an alternative to statically linked musl libc implementations promoted by `golang/go`.

### Observability

For ShardingSphere Proxy Native, the observability capabilities it provides are not consistent with [Observability](/en/user-manual/shardingsphere-proxy/observability).

Users can use a series of command-line tools or visualization tools provided by https://www.graalvm.org/latest/reference-manual/tools/ to observe the internal behavior of GraalVM Native Image,
and use VSCode under Linux to complete the debugging work according to their requirements. If the user is using IntelliJ IDEA and wants to debug the generated GraalVM Native Image,
the user can follow https://blog.jetbrains.com/idea/2022/06/intellij-idea-2022-2-eap-5/#Experimental_GraalVM_Native_Debugger_for_Java and its successors.
If the user is not using Linux, the GraalVM Native Image cannot be debugged. Please pay attention to https://github.com/oracle/graal/issues/5648 which has not been closed.

For the use of Java Agents such as `ShardingSphere Agent`, the `native-image` component of GraalVM does not fully support the use of javaagent when building Native Image.
Users need to pay attention to https://github.com/oracle/graal/issues/8177 which has not been closed.
If users expect to use such Java Agents under ShardingSphere Proxy Native, they need to pay attention to the changes involved in https://github.com/oracle/graal/pull/8077.

### `linux/riscv64` OS/Arch limitation

Currently, ShardingSphere Proxy Native does not provide availability on `linux/riscv64` OS/Arch. If developers use the `linux/riscv64` device,
they should refer to https://medium.com/graalvm/graalvm-native-image-meets-risc-v-899be38eddd9 to modify the build configuration of Proxy Native.

Since https://github.com/oracle/graal/issues/6855, `LLVM backend` needs to be built from the source code of GraalVM to be used.

See https://github.com/oracle/graal/blob/master/substratevm/src/com.oracle.svm.core.graal.llvm/src/com/oracle/svm/core/graal/llvm/LLVMBackend.md .

### Windows Containers Limitations

ShardingSphere Proxy Native can build GraalVM Native Image on Windows out of the box with a local toolchain containing `Microsoft.VisualStudio.2022.Community`.

Currently affected by https://github.com/graalvm/container/issues/106,
ShardingSphere does not provide the build configuration required to build Docker Image for `Dynamically Linked GraalVM Native Image` compiled through Windows.

### Wasm Module Limitations

Although `Oracle GraalVM Early Access Builds For JDK 26 EA 3` already supports building GraalVM Native Image in the form of `Wasm Module`,
ShardingSphere is not yet ready to test CI under OpenJDK 26.

Currently, ShardingSphere Proxy Native does not provide the build configuration required to compile to `Wasm Module`.
