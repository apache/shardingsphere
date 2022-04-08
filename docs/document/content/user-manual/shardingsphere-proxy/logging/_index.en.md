+++
title = "Logging"
weight = 4
+++

This chapter will introduce the detailed syntax of Logging which is used when users need to distinguish schemas or users in the log. To achieve a specific goal, following configurations can be added to logback.xml:

## To distinguish schemas in the same log
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

## To distinguish schemas and users in the same log
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

## To split into different log files
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
