+++
title = "开发环境指南"
weight = 2
chapter = true
+++

## 安装 Git

使用最新版。

可以从 [Git 下载页面]( https://git-scm.com/downloads ) 下载最新安装包。

如果你运行的是 macOS 或者 Linux 系统，那也可以从相关软件仓库直接安装。

### Git 配置

如果系统是 Windows，请在克隆 ShardingSphere 代码之前完成以下配置。

在 PowerShell 或者 cmd 执行以下命令，预防克隆 ShardingSphere 代码的时候出现 `filename too long` 错误：
```shell
git config --global core.longpaths true
```

## 安装 JDK

使用 JDK 17 或以上版本。

可以从 [OpenJDK 下载页面]( https://adoptium.net/temurin/releases ) 下载。

可以根据自己的系统自行搜索 JDK 安装指南。

## 安装 Maven

可选。Maven 安装并配置好的情况下可以使用 `mvn` 命令，否则可以使用 `mvnw` 命令（自动下载并安装需要的 Maven，使用在 `.mvn/wrapper/maven-wrapper.properties` 配置的版本）。

使用 Maven 3.6 或以上版本。

可以从 [Maven 下载页面]( https://maven.apache.org/download.html ) 下载。

请参考 [Maven 官方教程]( https://maven.apache.org/install.html ) 完成安装。

## 选择终端

### MacOS

以下终端任选其一：
- Terminal：MacOS 自带。
- iTerm2：可自行安装。

### Windows

以下终端任选其一：
- PowerShell：Windows 自带。
- Git Bash：可以通过 Git 官方客户端安装。
- 适用于 Linux 的 Windows 子系统 (WSL) ：可自行安装。

### Linux

不同发行版有不同的终端，可自行选择。

## 设置系统环境变量

设置系统环境变量，包括：JAVA_HOME，MAVEN_HOME 和 PATH。

根据使用的系统自行查询设置方法并完成设置。

## 选择 IDE

以下 IDE 任选其一：
- IntelliJ IDEA
- Eclipse
- NetBeans

## 安装 IDE 插件

以下是一些常用插件：
- Lombok：必须。最新版 IntelliJ IDEA 已自带。
- CheckStyle：可选。可以实时或根据需要扫描 Java 文件，找出不符合代码规范的地方并提示。**注意**：不是所有不符合 [代码规范](/cn/involved/conduct/code/) 的地方都可以被检查出来。
- ANTLR v4：可选。做 SQL 语法扩展任务的时候可能有用。

### CheckStyle 插件配置

1. 添加配置文件 ：使用 `src/resources/checkstyle.xml`；
2. 选择 Checkstyle 版本 ：使用 `12.3.0`。
