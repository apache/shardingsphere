+++
toc = true
title = "编排治理"
weight = 2
+++

## 实现动机

通过注册中心，提供熔断数据库访问程序对数据库的访问和禁用从库的访问的能力。数据治理仍然有大量未完成的功能。

## 注册中心数据结构

注册中心在定义的命名空间的state下，创建数据库访问对象运行节点，用于区分不同数据库访问实例。包括instances和datasources节点。

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....
datasources
    ├──ds_0
    ├──ds_1
    ├──....
```

### state/instances

数据库访问对象运行实例信息，子节点是当前运行实例的标识。运行实例标识由运行服务器的IP地址和PID构成。运行实例标识均为临时节点，当实例上线时注册，下线时自动清理。注册中心监控这些节点的变化来治理运行中实例对数据库的访问等。

### state/datasources

可以治理读写分离从库，可动态添加删除以及禁用。
