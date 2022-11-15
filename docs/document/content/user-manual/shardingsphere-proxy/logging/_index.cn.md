+++
title = "日志配置"
weight = 8
+++

## 背景信息

ShardingSphere 使用 Logback 进行日志管理，内部采用 Java SPI 提供默认日志配置，用户可以使用 XML 文件来配置自定义日志输出，Proxy 将优先读取 `conf` 目录下的 `logback.xml` 提供的日志配置。

下面将介绍如何自定义日志配置。

## 操作步骤

1. 获取 ShardingSphere-Proxy 二进制发布包

在[下载页面](https://shardingsphere.apache.org/document/current/cn/downloads/)获取。

2. 新建 `conf/logback.xml`

根据需求自定义 logger 级别、pattern 等。
> 建议在配置示例的基础上进行修改

3. 查看日志

ShardingSphere-Proxy 启动后，日志将输出到 `logs` 目录下，选择目标日志文件进行查看。

### 配置示例

```xml
<?xml version="1.0"?>
<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one or more
  ~ contributor license agreements.  See the NOTICE file distributed with
  ~ this work for additional information regarding copyright ownership.
  ~ The ASF licenses this file to You under the Apache License, Version 2.0
  ~ (the "License"); you may not use this file except in compliance with
  ~ the License.  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<configuration>
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>[%-5level] %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <logger name="org.apache.shardingsphere" level="info" additivity="false">
        <appender-ref ref="console" />
    </logger>
    
    <logger name="com.zaxxer.hikari" level="error" />
    
    <logger name="com.atomikos" level="error" />
    
    <logger name="io.netty" level="error" />
    
    <root>
        <level value="info" />
        <appender-ref ref="console" />
    </root>
</configuration>
```
