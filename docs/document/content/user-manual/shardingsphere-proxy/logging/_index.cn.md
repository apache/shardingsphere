+++
title = "Logging"
weight = 4
+++

本章将介绍日志记录的详细语法，当使用者需要在日志中区分 schema 或用户时，可以在 logback.xml 中添加以下配置。

## 区分同一日志中的 schema

```
<appender name="schemaConsole" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{schema}] %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="ShardingSphere-SQL" level="info" additivity="false">
    <appender-ref ref="schemaConsole" />
</logger>
```

## 区分同一日志中的 schema 和用户

```
<appender name="schemaConsole" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{schema}] [%X{user}] %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="ShardingSphere-SQL" level="info" additivity="false">
    <appender-ref ref="schemaConsole" />
</logger>
```

## 拆分为不同的日志文件

```
<appender name="SiftingFile" class="ch.qos.logback.classic.sift.SiftingAppender">
    <discriminator>
        <key>schema</key>
        <defaultValue>none</defaultValue>
    </discriminator>
    <sift>
        <appender name="File-${taskId}" class="ch.qos.logback.core.FileAppender">
            <file>logs/${schema}.log</file>
            <append>true</append>
            <encoder charset="UTF-8">
                <pattern>[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{user}] %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
    </sift>
</appender>

<logger name="ShardingSphere-SQL" level="info" additivity="false">
    <appender-ref ref="SiftingFile" />
</logger>
```
