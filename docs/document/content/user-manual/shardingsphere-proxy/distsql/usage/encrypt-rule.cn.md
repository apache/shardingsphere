+++
title = "数据加密"
weight = 3
+++

## 资源操作

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
);
```

## 规则操作

- 创建加密规则

```sql
CREATE ENCRYPT RULE t_encrypt (
    COLUMNS(
        (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),
        (NAME=order_id,PLAIN=order_plain,CIPHER =order_cipher,TYPE(NAME='RC4',PROPERTIES('rc4-key-value'='123456abc')))
));
```

- 创建加密表

```sql
CREATE TABLE `t_encrypt` (
    `id` int(11) NOT NULL,
    `user_id` varchar(45) DEFAULT NULL,
    `order_id` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- 修改加密规则

```sql
ALTER ENCRYPT RULE t_encrypt (
    COLUMNS(
        (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))
));
```

- 删除加密规则

```sql
DROP ENCRYPT RULE t_encrypt;
```

- 删除数据源

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- 删除分布式数据库

```sql
DROP DATABASE encrypt_db;
```
