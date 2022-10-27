+++
title = "Encrypt"
weight = 3
+++

## Resource Operation

```sql
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="ds_0",
    USER="root",
    PASSWORD="root"
);
```

## Rule Operation

- Create encrypt rule

```sql
CREATE ENCRYPT RULE t_encrypt (
    COLUMNS(
        (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc'))),
        (NAME=order_id,PLAIN=order_plain,CIPHER =order_cipher,TYPE(NAME='RC4',PROPERTIES('rc4-key-value'='123456abc')))
));
```

- Create encrypt table

```sql
CREATE TABLE `t_encrypt` (
    `id` int(11) NOT NULL,
    `user_id` varchar(45) DEFAULT NULL,
    `order_id` varchar(45) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- Alter encrypt rule

```sql
ALTER ENCRYPT RULE t_encrypt (
    COLUMNS(
        (NAME=user_id,PLAIN=user_plain,CIPHER=user_cipher,TYPE(NAME='AES',PROPERTIES('aes-key-value'='123456abc')))
));
```

- Drop encrypt rule

```sql
DROP ENCRYPT RULE t_encrypt;
```

- Unregister storage unit

```sql
UNREGISTER STORAGE UNIT ds_0;
```

- Drop distributed database

```sql
DROP DATABASE encrypt_db;
```
