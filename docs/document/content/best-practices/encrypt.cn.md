+++
pre = "<b>7.5. </b>"
title = "数据加密"
weight = 5
chapter = true
+++

## 适用场景
不论是新业务需要使用数据加密的功能，还是需要对已有业务进行改造满足加密需求，Apache ShardingSphere 都提供了一套完整的数据加密解决方案。用户可以通过如下实践进行学习了解。
## 前提条件
假设新业务上线，现在期望对用户的密码字段进行加密存储于数据库中，但同时系统要能够获取明文数据。
## 数据规划
由于用户期望对密码字段进行加密存储，但是系统又要能够获取明文字段，那么我们可以采用可逆算法 AES 对数据进行加密。因此我们可以采用如配置示例中所示的配置，对`t_user` 表进行加密，逻辑列名称为 `pwd`，也就是业务 SQL 中采用的字段名称，该字段映射到数据库中为 `pwd_cipher` 字段，对应的加密算法采用 AES。
## 操作步骤
1. 下载 ShardingSphere-proxy
2. 采用如配置示例所示的加密配置
3. 连接 proxy 后，创建 `t_user` 表
``` sql
## 可以通过 PREVIEW 语法查看真实创建表语法
encrypt_db=> PREVIEW CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd VARCHAR(200) NOT NULL, PRIMARY KEY (user_id));
 data_source_name |                                                         actual_sql
------------------+----------------------------------------------------------------------------------------------------------------------------
 ds_0             | CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd_cipher VARCHAR(200) NOT NULL, PRIMARY KEY (user_id))
(1 row)
CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd VARCHAR(200) NOT NULL, PRIMARY KEY (user_id));
``` 
4. 向 `t_user` 表中插入数据
``` sql
encrypt_db=> PREVIEW INSERT INTO t_user values (1,'ZHANGSAN','123456');
 data_source_name |                                              actual_sql
------------------+------------------------------------------------------------------------------------------------------
 ds_0             | INSERT INTO t_user(user_id, username, pwd_cipher) values (1, 'ZHANGSAN', 'MyOShk4kjRnds7CZfU5NCw==')
(1 row)
INSERT INTO t_user values (1,'ZHANGSAN','123456');
```
5. 查询 `t_user` 表
``` sql
encrypt_db=> PREVIEW SELECT * FROM t_user WHERE pwd = '123456';
 data_source_name |                                                                actual_sql
------------------+------------------------------------------------------------------------------------------------------------------------------------------
 ds_0             | SELECT "t_user"."user_id", "t_user"."username", "t_user"."pwd_cipher" AS "pwd" FROM t_user WHERE pwd_cipher = 'MyOShk4kjRnds7CZfU5NCw=='
(1 row)
encrypt_db=> SELECT * FROM t_user WHERE pwd = '123456';
 user_id | username |  pwd
---------+----------+--------
       1 | ZHANGSAN | 123456
(1 row)
```

## 配置示例
config-encrypt.yaml
``` yaml
rules:
- !ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
  tables:
    t_user:
      columns:
        pwd:
          cipherColumn: pwd_cipher
          encryptorName: aes_encryptor
```
## 相关参考
[YAML 配置：数据加密](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)