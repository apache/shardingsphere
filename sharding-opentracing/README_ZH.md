# Sharding-JDBC OpenTracing插件

## 介绍

该插件会将[OpenTracing](http://opentracing.io/)组件添加到Sharding-JDBC中。这个插件使
Sharding-JDBC的开发人员很容易整合支持OpenTracing的Tracer。

## 使用方法

### 添加依赖

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-opentracing</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 设置Tracer

有两种方法可以整合插件与`io.opentracing.Tracer`对象（例如 [Zipkin](https://zipkin.io)，[Skywalking](http://skywalking.org)）。

#### 1. 使用代码

 1. 获得 `io.opentracing.Tracer`对象，该对象一般由Tracer提供.
 1. 使用Sharding-JDBC组件之前，调用 `io.shardingjdbc.opentracing.ShardingJDBCTracer#init(io.opentracing.Tracer)`.

#### 2. 使用jvm属性配置

 1. 设置 `-Dshardingjdbc.opentracing.tracer.class=OPENTRACING_TRACER_CLASS_NAME` 启动应用. `OPENTRACING_TRACER_CLASS_NAME` 必须实现
`io.opentracing.Tracer
 1. 使用Sharding-JDBC组件之前，调用 `io.shardingjdbc.opentracing.ShardingJDBCTracer#init()`.
