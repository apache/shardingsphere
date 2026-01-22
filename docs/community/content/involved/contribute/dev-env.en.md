+++
title = "How to Set Up Your DEV Environment"
weight = 2
chapter = true
+++

## Git Installation

Use the latest version.

You could download Git from [Git Downloads]( https://git-scm.com/downloads ).

If you're running macOS or Linux, you could install Git from software repository.

### Git Settings

If you're running Windows, following settings should be done before cloning ShardingSphere code.

Execute following command in PowerShell or cmd to prevent from `filename too long` error on cloning ShardingSphere code:
```shell
git config --global core.longpaths true
```

## JDK Installation

Use JDK 17 or higher.

You could download JDK from [OpenJDK Downloads]( https://adoptium.net/temurin/releases ).

You could search JDK installation guide for your platform on Google.

## Maven

Optional. You could use `mvn` command if Maven is ready, else use `mvnw` instead (download and install Maven automatically, use version defined in `.mvn/wrapper/maven-wrapper.properties`).

Use 3.6+.

You could download Maven from [Downloading Apache Maven]( https://maven.apache.org/download.html ).

Please refer to [Installing Apache Maven]( https://maven.apache.org/install.html ) for more details.

## Terminal

### macOS

You could use any of them:
- Terminal : Preinstalled on macOS
- iTerm2 : Could be installed by yourself

### Windows

You could use any of them:
- PowerShell : Preinstalled on Windows
- Git Bash : Could be installed in official Git client
- Windows Subsystem for Linux (WSL) : Could be installed by yourself

### Linux

Different terminal on different distribution.

## Environment Variables

Set up JAVA_HOME, MAVEN_HOME and PATH system environment variables.

You could find how to do it on Google.

## IDE

You could use any of them:
- IntelliJ IDEA
- Eclipse
- NetBeans

## IDE Plugins

These plugins might be useful for you:
- Lombok : Required. It's already preinstalled in the latest IntelliJ IDEA.
- CheckStyle : Optional. It provides both real-time and on-demand scanning of Java files. **Notice**: Not all [Code of Conduct](/en/involved/conduct/code/) could be checked.
- ANTLR v4 : Optional. It's recommended if you contribute to SQL extension.

### CheckStyle Settings

1. Add `Configuration File` : use `src/resources/checkstyle.xml`
2. Select `Checkstyle version` : use `12.3.0`
