+++
title = "Dynamic Data Masking | ShardingSphere 5.3.1 Deep Dive"
weight = 88
chapter = true 
+++

With increased focus placed on user data protection and the introduction of privacy laws or data protection regulations such as [GDPR](https://gdpr-info.eu/), being compliant in terms of digital personal privacy is now of paramount importance.

Traditional applications generally lack personal data protection. Data masking enables data returned by production databases to be specially masked, encrypted, hidden, and audited according to the users' role, responsibility, and other defined rules without having to make any changes to the data.

This allows you to ensure that employees, external users, operations, part-time employees, partners, data analysts, developers, testers, and consultants have the appropriate degree of access to sensitive data in production environments.

Based on industry experience, there are two major types of data masking. One is static data masking and the other is dynamic data masking.

Static data masking refers to masking, encrypting, or replacing sensitive data with a masking algorithm for database systems through the task. It saves the masked data to the target location.

Dynamic data masking is more flexible. Dynamic data masking happens at runtime based on each query. This way, there doesn't need to be a second data source to store the masked data dynamically.

[ShardingSphere 5.3.1 provides a dynamic data masking function](https://shardingsphere.apache.org/document/4.1.1/en/features/orchestration/encrypt/). Users query data through ShardingSphere, while ShardingSphere processes data with a masking algorithm before returning results.

The whole process is based on masking rules configured by users. The masked data is then delivered to users.

# Methods

## Masking & Microkernel

On account of the ShardingSphere microkernel and pluggable architecture, the data masking function only needs to implement SPI, the resulting merger, to achieve the flexible extension of functions.

As shown in the figure below, the ShardingSphere microkernelalready includes core logic like `SQL Parser`, `SQL router`, `SQL executor`. The dynamic data masking function provided by ShardingSphere 5.3.1 is only an enhancement of the query results of the other ShardingSphere components.

This means that users only need to implement `ResultDecoratorEngine` and `ResultDecorator` in the merger to achieve the function.

![img](https://shardingsphere.apache.org/blog/img/2023_02_17_Dynamic_Data_Masking_ShardingSphere_5.3.1_Deep_Dive1.png)

In order to implement the data masking function, the `shardingsphere-mask` module is added to the features. This module contains `shardingsphere-mask-api`, `shardingsphere-mask-core` and `shardingsphere-mask-distsql`. The functions of each module are as follows:

- `shardingsphere-mask-api`：data masking API module, including `Rule` configuration with data masking function and masking algorithm SPI interface.
- `shardingsphere-mask-core`：the core data masking module, including `Rule` initialization logic, masking algorithm implementation, and resulting merger decorator implementation logic.
- `shardingsphere-mask-distsql`：DistSQL module for data masking allowing users to dynamically modify masking rules through DistSQL.

In addition to the kernel process, data masking's position in the ShardingSphere kernel also deserves mention.

ShardingSphere's powerful pluggable architecture allows us to arbitrarily assemble kernel functions, including the newly added data masking function.

Users can use the function alone, or with sharding, encryption, and other functions to achieve a more complete distributed database solution.

The following figure shows the relationship between the current functions of the ShardingSphere kernel, which can be generally divided into three levels: Column Level, Table Level, and Database Level.

![img](https://shardingsphere.apache.org/blog/img/2023_02_17_Dynamic_Data_Masking_ShardingSphere_5.3.1_Deep_Dive2.png)

Column Level includes data encryption and data masking for improving columns. Table Level includes data sharding and built-in single table management.

Functions at the Database Level represent the bulk of the ecosystem, including the dual write function provided by [SphereEx](https://www.sphere-ex.com/), as well as read/write splitting, high-availability database discovery, and shadow database provided by the open-source ShardingSphere. They're all related to database traffic governance.

ShardingSphere processes according to the three levels levels, and within each level it processes according to the Order.

For example: if you use both data encryption and data masking, the system preferentially processes the encryption logic, decrypts the ciphertext stored in the database, and then uses the masking algorithm to process data masking.

## Masking YAML API & DistSQL

After introducing the relationship between masking and microkernel, let's draw our eyes to the masking API and DistSQL. Users can configure masking rules based on YAML or using DistSQL.

First, let's take a look at how the YAML API is configured. The user just needs to configure the data masking column and algorithm in tables under `- !MASK`. The name of the algorithm defined by `maskAlgorithm` must be the same as the name in it.

- `maskAlgorithm`: specifies the masking algorithm, and the mask feature performs data processing according to the masking algorithm.

The YAML API configuration of the mask feature is as follows:

```yaml
databaseName: mask_db

dataSources:
  ds_0:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false
    username: root
    password: 123456
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_1:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1?serverTimezone=UTC&useSSL=false
    username: root
    password: 123456
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
        
# MASK rules configuration
rules:
- !MASK
  tables:
    t_user:
      columns:
        password:
          maskAlgorithm: md5_mask
        email:
          maskAlgorithm: mask_before_special_chars_mask
        telephone:
          maskAlgorithm: keep_first_n_last_m_mask

  maskAlgorithms:
    md5_mask:
      type: MD5
    mask_before_special_chars_mask:
      type: MASK_BEFORE_SPECIAL_CHARS
      props:
        special-chars: '@'
        replace-char: '*'
    keep_first_n_last_m_mask:
      type: KEEP_FIRST_N_LAST_M
      props:
        first-n: 3
        last-m: 4
        replace-char: '*'
```

In addition, ShardingSphere 5.3.1 also provides DistSQL related masking rules to meet users' needs for dynamically updating masking rules during the running stage.

The data masking DistSQL syntax is as follows, containing DistSQL statements that are commonly used to create, alter, drop, and show masking rules.

```sql
-- Create masking rules
CREATE MASK RULE t_user (
    COLUMNS(
        (NAME=password, TYPE(NAME='MD5')),
        (NAME=email, TYPE(NAME='MASK_BEFORE_SPECIAL_CHARS', PROPERTIES("special-chars"="@", "replace-char"="*"))),
        (NAME=telephone, TYPE(NAME='KEEP_FIRST_N_LAST_M', PROPERTIES("first-n"=3, "last-m"=4, "replace-char"="*")))
    )
);

-- Alter masking rules
ALTER MASK RULE t_user (
    COLUMNS(
        (NAME=password, TYPE(NAME='MD5', PROPERTIES("salt"="123abc"))),
        (NAME=email, TYPE(NAME='MASK_BEFORE_SPECIAL_CHARS', PROPERTIES("special-chars"="@", "replace-char"="*"))),
        (NAME=telephone, TYPE(NAME='TELEPHONE_RANDOM_REPLACE', PROPERTIES("network-numbers"="123,180")))
    )
);

-- Drop masking rule
DROP MASK RULE t_user;

-- Show masking rule
SHOW MASK RULES FROM mask_db;
```

For detailed DistSQL syntax, see [DistSQL Documentation — Data Masking](https://shardingsphere.apache.org/document/5.3.1/en/user-manual/shardingsphere-proxy/distsql/syntax/rdl/rule-definition/mask/create-mask-rule/).

## Built-in Masking Algorithm

The ShardingSphere 5.3.1 release also includes a large number of built-in masking algorithms, which are implemented based on the `MaskAlgorithm` SPI interface, and users can flexibly extend the algorithm according to their business requirements.

```java
/**
 * Mask algorithm.
 * 
 * @param <I> type of plain value
 * @param <O> type of mask value
 */
public interface MaskAlgorithm<I, O> {
    
    /**
     * Mask.
     *
     * @param plainValue plain value
     * @return mask value
     */
    O mask(I plainValue);
}
```

Built-in masking algorithms can be mainly divided into three types, hash mask, cover mask, and replace mask. The specific algorithms are listed as follows：

![img](https://shardingsphere.apache.org/blog/img/2023_02_17_Dynamic_Data_Masking_ShardingSphere_5.3.1_Deep_Dive3.png)

The data masking algorithm is still in continuous improvement. For more about the algorithm parameters, please refer to [Data Masking Algorithm](https://shardingsphere.apache.org/document/5.3.1/en/user-manual/common-config/builtin-algorithm/mask/).

We welcome everyone to participate and contribute to improving the data masking algorithm together.

# Data Masking in Practice

In the last section, we will take a practical look at data masking.

For a company's internal sensitive data, data masking and data encryption are used together. The database stores data with data encryption to avoid security problems caused by loss of data.

During the data query, data decryption and data masking are run according to the rules to avoid the direct display of sensitive data.

This post showcases a scenario where data masking and data encryption are performed together and uses DistSQL for a dynamic update to show the actual effect of data masking.

First, download ShardingSphere Proxy 5.3.1, and configure `server.yaml` for the first operation, and connect to the Proxy with `mysql -u root -h 127.0.0.1 -P 3307 -p c-A`, and run `CREATE DATABASE mask_db` to create a data masking logic database.

```sql
-- Create a data masking logic database
CREATE DATABASE mask_db;
-- Switch to mask_db
USE mask_db;
```

After creating the logic database, DistSQL can be used to register the storage resource and initialize the masking and encryption rules.

```sql
-- Register storage resource
REGISTER STORAGE UNIT ds_0 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="demo_ds_0",
    USER="root",
    PASSWORD="123456",
    PROPERTIES("maximumPoolSize"=10)
), ds_1 (
    HOST="127.0.0.1",
    PORT=3306,
    DB="demo_ds_1",
    USER="root",
    PASSWORD="123456",
    PROPERTIES("maximumPoolSize"=10)
);

-- Create masking rules
CREATE MASK RULE t_user (
    COLUMNS(
        (NAME=password, TYPE(NAME='MD5')),
        (NAME=email, TYPE(NAME='MASK_BEFORE_SPECIAL_CHARS', PROPERTIES("special-chars"="@", "replace-char"="*"))),
        (NAME=telephone, TYPE(NAME='KEEP_FIRST_N_LAST_M', PROPERTIES("first-n"=3, "last-m"=4, "replace-char"="*")))
    )
);

-- Create encryption rules
CREATE ENCRYPT RULE t_user (
    COLUMNS(
        (NAME=user_name, CIPHER=user_name_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='123456abc')))),
        (NAME=password, CIPHER =password_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='123456abc')))),
        (NAME=email, CIPHER =email_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='123456abc')))),
        (NAME=telephone, CIPHER =telephone_cipher, ENCRYPT_ALGORITHM(TYPE(NAME='AES', PROPERTIES('aes-key-value'='123456abc'))))
    )
);
```

After the masking and encryption rules are created, run the `SHOW` statement in DistSQL to view the masking and encryption rules:

```mysql
-- Show masking rules
mysql> SHOW MASK RULES FROM mask_db;
+--------+-----------+---------------------------+-----------------------------------+
| table  | column    | algorithm_type            | algorithm_props                   |
+--------+-----------+---------------------------+-----------------------------------+
| t_user | password  | MD5                       |                                   |
| t_user | email     | MASK_BEFORE_SPECIAL_CHARS | replace-char=*,special-chars=@    |
| t_user | telephone | KEEP_FIRST_N_LAST_M       | first-n=3,last-m=4,replace-char=* |
+--------+-----------+---------------------------+-----------------------------------+
3 rows in set (0.01 sec)

-- Show encryption rules
mysql> SHOW ENCRYPT RULES FROM mask_db;
+--------+--------------+------------------+--------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| table  | logic_column | cipher_column    | assisted_query_column | like_query_column | encryptor_type | encryptor_props         | assisted_query_type | assisted_query_props | like_query_type | like_query_props |
+--------+--------------+------------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
| t_user | user_name    | user_name_cipher |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
| t_user | password     | password_cipher  |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
| t_user | email        | email_cipher     |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
| t_user | telephone    | telephone_cipher |                       |                   | AES            | aes-key-value=123456abc |                     |                      |                 |                  |
+--------+--------------+------------------+-----------------------+-------------------+----------------+-------------------------+---------------------+----------------------+-----------------+------------------+
4 rows in set (0.01 sec)
```

After creating the rules, we create the following `t_user` table and perform data initialization:

```sql
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (user_id INT PRIMARY KEY, user_name VARCHAR(50) NOT NULL, password VARCHAR(50) NOT NULL, email VARCHAR(50) NOT NULL, telephone CHAR(50) NOT NULL, creation_date DATE NOT NULL);

INSERT INTO t_user(user_id, user_name, password, email, telephone, creation_date) values(10, 'zhangsan', '111111', 'zhangsan@gmail.com', '12345678900', '2017-08-08'),
(11, 'lisi', '222222', 'lisi@gmail.com', '12345678901', '2017-08-08'),
(12, 'wangwu', '333333', 'wangwu@gmail.com', '12345678902', '2017-08-08'),
(13, 'zhaoliu', '444444', 'zhaoliu@gmail.com', '12345678903', '2017-08-08'),
(14, 'zhuqi', '555555', 'zhuqi@gmail.com', '12345678904', '2017-08-08'),
(15, 'liba', '666666', 'liba@gmail.com', '12345678905', '2017-08-08'),
(16, 'wangjiu', '777777', 'wangjiu@gmail.com', '12345678906', '2017-08-08'),
(17, 'zhuda', '888888', 'zhuda@gmail.com', '12345678907', '2017-08-08'),
(18, 'suner', '999999', 'suner@gmail.com', '12345678908', '2017-08-08'),
(19, 'zhousan', '123456', 'zhousan@gmail.com', '12345678909', '2017-08-08'),
(20, 'tom', '234567', 'tom@gmail.com', '12345678910', '2017-08-08'),
(21, 'kobe', '345678', 'kobe@gmail.com', '12345678911', '2017-08-08'),
(22, 'jerry', '456789', 'jerry@gmail.com', '12345678912', '2017-08-08'),
(23, 'james', '567890', 'james@gmail.com', '12345678913', '2017-08-08'),
(24, 'wade', '012345', 'wade@gmail.com', '12345678914', '2017-08-08'),
(25, 'rose', '000000', 'rose@gmail.com', '12345678915', '2017-08-08'),
(26, 'bosh', '111222', 'bosh@gmail.com', '12345678916', '2017-08-08'),
(27, 'jack', '222333', 'jack@gmail.com', '12345678917', '2017-08-08'),
(28, 'jordan', '333444', 'jordan@gmail.com', '12345678918', '2017-08-08'),
(29, 'julie', '444555', 'julie@gmail.com', '12345678919', '2017-08-08');
```

After data initialization, we can directly connect to the MySQL database through `MySQL -u root -h 127.0.0.1 -P 3306 -p c-A` to check the data stored in the table `t_user` of the underlying database.

It can be seen that MySQL stores encrypted data, and sensitive data is effectively protected in the database storage layer.

```mysql
mysql> SELECT * FROM t_user;
+---------+--------------------------+--------------------------+----------------------------------------------+--------------------------+---------------+
| user_id | user_name_cipher         | password_cipher          | email_cipher                                 | telephone_cipher         | creation_date |
+---------+--------------------------+--------------------------+----------------------------------------------+--------------------------+---------------+
|      10 | sVq8Lmm+j6bZE5EKSilJEQ== | aQol0b6th65d0aXe+zFPsQ== | WM0fHOH91JNWnHTkiqBdyNmzk4uJ7CCz4mB1va9Ya1M= | kLjLJIMnfyHT2nA+viaoaQ== | 2017-08-08    |
|      11 | fQ7IzBxKVuNHtUF6h6WSBg== | wuhmEKgdgrWQYt+Ev0hgGA== | svATu3uWv9KfiloWJeWx3A==                     | 0kDFxndQdzauFwL/wyCsNQ== | 2017-08-08    |
|      12 | AQRWSlufQPog/b64YRhu6Q== | x7A+2jq9B6DSOSFtSOibdA== | nHJv9e6NiClIuGHOjHLvCAq2ZLhWcqfQ8/EQnIqMx+g= | a/SzSJLapt5iBXvF2c9ycw== | 2017-08-08    |
|      13 | 5NqS4YvpT+mHBFqZOZ3QDA== | zi6b4xYRjjV+bBk2R4wB+w== | MLBZczLjriUXvg3aM5QPTxMJbLjNh8yeNrSNBek/VTw= | b6VVhG+F6ujG8IMUZJAIFg== | 2017-08-08    |
|      14 | qeIY9od3u1KwhjihzLQUTQ== | 51UmlLAC+tUvdOAj8CjWfQ== | JCmeNdPyrKO5BW5zvhAA+g==                     | f995xinpZdKMVU5J5/yv3w== | 2017-08-08    |
|      15 | VbNUtguwtpeGhHGnPJ3aXg== | +3/5CVbqoKhg3sqznKTFFQ== | T+X+e3Q3+ZNIXXmg/80uxg==                     | GETj+S6DrO042E7NuBXLBQ== | 2017-08-08    |
|      16 | U0/Ao/w1u7L5avR3fAH2Og== | jFfFMYxv02DjaFRuAoCDGw== | RNW/KRq5HeL2YTfAdXSyARMJbLjNh8yeNrSNBek/VTw= | +lbvjJwO7VO4HUKc0Mw0NA== | 2017-08-08    |
|      17 | zb1sgBigoMi7JPSoY4bAVw== | VFIjocgjujJCJc6waWXqJA== | 1vF/ET3nBxt7T7vVfAndZQ==                     | wFvs5BH6OikgveBeTEBwsQ== | 2017-08-08    |
|      18 | rJzNIrFEnx296kW+N1YmMw== | LaODSKGyR7vZ1IvmBOe9vA== | 5u4GIQkJsWRmnJHWaHNSjg==                     | uwqm2O1Lv2tNTraJX1ym7Q== | 2017-08-08    |
|      19 | qHwpQ9kteL8VX6iTUhNdbQ== | MyOShk4kjRnds7CZfU5NCw== | HmYCo7QBfJ2E0EvaGHBCOBMJbLjNh8yeNrSNBek/VTw= | YLNQuuUPMGA21nhKWPzzsg== | 2017-08-08    |
|      20 | qCCmvf7OWRxbVbtLb0az1g== | fzdTMkzpBvgNYmKSQAp8Fg== | gOoP4Mf0P4ISOJp6A4sRmg==                     | l4xa4HwOfs/jusoJon9Wzw== | 2017-08-08    |
|      21 | IYJ1COaRQ0gSjWMC/UAeMg== | 1uEDMeYh2jstbOf6kx/cqw== | tikMAFiQ37u2VgWqUT38Eg==                     | rGpr30UXfczXjCjdvPN+BA== | 2017-08-08    |
|      22 | 7wvZZ7NVHgk6m1vB/sTC1Q== | OirN3gvz9uBnrq88nfa1wQ== | T7K/Uz1O2m+3xvB0+c4nGQ==                     | 7+fCU+VbQZKgLJXZPTTegA== | 2017-08-08    |
|      23 | SbVQWl8JbnxflCfGJ7KZdA== | hWVVYdkdTUTgm08haeq+tw== | Uk3ju6GteCD1qEHns5ZhKA==                     | DpnV86FZefwBRmIAVBh2gg== | 2017-08-08    |
|      24 | fx7OfSAYqVpjNa7LoKhXvw== | N2W9ijAXNkBxhkvJiIwp0A== | lAAGItVLmb1H69++1MDrIA==                     | QrE62wAb8B+2cEPcs4Lm1Q== | 2017-08-08    |
|      25 | wH3/LdWShD9aCb8eCIm3Tg== | GDixtt6NzPOVv6H0dmov5g== | T1yfJSyVxumZUfkDnmUQxA==                     | iU+AsGczboCRfU+Zr7mcpw== | 2017-08-08    |
|      26 | GgJQTndbxyBZ2tECS8SmqQ== | gLgVFLFIyyKwdQCXaw78Ag== | O+JIn9XZ3yq6RnKElHuqlA==                     | kwYlbu9aF7ndvMTcj8QBSg== | 2017-08-08    |
|      27 | lv8w8g32kuTXNvSUUypOig== | 8i0YH2mn6kXSyvBjM5p+Yg== | gqRoJF5S66SvBalc2RCo1A==                     | 2ob/3UYqRsZA5VdScnaWxQ== | 2017-08-08    |
|      28 | P9YCbFvWCIhcS99KyKH2zA== | PRrI4z4FrWwLvcHPx9g4og== | y8q31Jj4PFSyZHiLVIxKEQq2ZLhWcqfQ8/EQnIqMx+g= | kDF2za26uOerlNYWYHRT2Q== | 2017-08-08    |
|      29 | 5wu9XvlJAVtjKijhxt6SQQ== | O4pgkLgz34N+C4bIUOQVnA== | UH7ihg16J61Np/EYMQnXIA==                     | z2hbJQD4dRkVVITNxAac5Q== | 2017-08-08    |
+---------+--------------------------+--------------------------+----------------------------------------------+--------------------------+---------------+
20 rows in set (0.00 sec)
```

After confirming the effect of data encryption, we conduct a simple test on the data masking function.

The following `CASE` includes `simple SELECT query`, `join query`, `subquery` and other daily used operation statements. It can be seen that the password is masked with MD5 hash, email is masked with `MASK_BEFORE_SPECIAL_CHARS` and telephone is masked with `KEEP_FIRST_N_LAST_M`.

```mysql
-- Simple SELECT query
mysql> SELECT * FROM t_user WHERE user_id = 10;
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
| user_id | user_name | password                         | email              | telephone   | creation_date |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
|      10 | zhangsan  | 96e79218965eb72c92a549dd5a330112 | ********@gmail.com | 123****8900 | 2017-08-08    |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
1 row in set (0.01 sec)

-- Join query
mysql> SELECT u1.* FROM t_user u1 INNER JOIN t_user u2 ON u1.user_id = u2.user_id WHERE u1.user_id = 10;
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
| user_id | user_name | password                         | email              | telephone   | creation_date |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
|      10 | zhangsan  | 96e79218965eb72c92a549dd5a330112 | ********@gmail.com | 123****8900 | 2017-08-08    |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
1 row in set (0.05 sec)

-- Subquery
mysql> SELECT * FROM (SELECT * FROM t_user) temp WHERE temp.user_id = 10;
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
| user_id | user_name | password                         | email              | telephone   | creation_date |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
|      10 | zhangsan  | 96e79218965eb72c92a549dd5a330112 | ********@gmail.com | 123****8900 | 2017-08-08    |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
1 row in set (0.03 sec)

-- Subquery with join
mysql> SELECT * FROM (SELECT u1.* FROM t_user u1 INNER JOIN t_user u2 ON u1.user_id = u2.user_id) temp WHERE temp.user_id < 15;
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
| user_id | user_name | password                         | email              | telephone   | creation_date |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
|      10 | zhangsan  | 96e79218965eb72c92a549dd5a330112 | ********@gmail.com | 123****8900 | 2017-08-08    |
|      11 | lisi      | e3ceb5881a0a1fdaad01296d7554868d | ****@gmail.com     | 123****8901 | 2017-08-08    |
|      12 | wangwu    | 1a100d2c0dab19c4430e7d73762b3423 | ******@gmail.com   | 123****8902 | 2017-08-08    |
|      13 | zhaoliu   | 73882ab1fa529d7273da0db6b49cc4f3 | *******@gmail.com  | 123****8903 | 2017-08-08    |
|      14 | zhuqi     | 5b1b68a9abf4d2cd155c81a9225fd158 | *****@gmail.com    | 123****8904 | 2017-08-08    |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
5 rows in set (0.03 sec)
```

We modify the masking rules using DistSQL, adding the optional parameter salt to the MD5 hash masking used for the password, and changing the algorithm for the telephone field to the `TELEPHONE_RANDOM_REPLACE` data masking algorithm.

```sql
ALTER MASK RULE t_user (
    COLUMNS(
        (NAME=password, TYPE(NAME='MD5', PROPERTIES("salt"="123abc"))),
        (NAME=email, TYPE(NAME='MASK_BEFORE_SPECIAL_CHARS', PROPERTIES("special-chars"="@", "replace-char"="*"))),
        (NAME=telephone, TYPE(NAME='TELEPHONE_RANDOM_REPLACE', PROPERTIES("network-numbers"="123,180")))
    )
);
```

After the modification, we conduct the query again. We can see that due to the change of password salting, the result of the MD5 hash masking of the password field was changed.

While the telephone random replace data masking algorithm is used in the telephone field, the masking result is also randomly generated after the number segment.

```mysql
mysql> SELECT * FROM t_user WHERE user_id = 10;
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
| user_id | user_name | password                         | email              | telephone   | creation_date |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
|      10 | zhangsan  | 554555c0eaca7aeecada758122efd640 | ********@gmail.com | 12383015546 | 2017-08-08    |
+---------+-----------+----------------------------------+--------------------+-------------+---------------+
1 row in set (0.01 sec)
```

Finally, we run `DROP MASK RULE t_user;` to delete the masking rule. The original plaintext can be seen if we query again.

```mysql
mysql> SELECT * FROM t_user WHERE user_id = 10;
+---------+-----------+----------+--------------------+-------------+---------------+
| user_id | user_name | password | email              | telephone   | creation_date |
+---------+-----------+----------+--------------------+-------------+---------------+
|      10 | zhangsan  | 111111   | zhangsan@gmail.com | 12345678900 | 2017-08-08    |
+---------+-----------+----------+--------------------+-------------+---------------+
1 row in set (0.00 sec)
```

# Conclusion

The newly added dynamic data masking function of Apache ShardingSphere 5.3.1 is a further enhancement to ShardingSphere's data security approach.

In the future, our community data masking is considering combining data masking with user rights, SQL audit, and other functions.

We welcome interested readers to join our community to contribute to improving the data masking function of Apache ShardingSphere.

## Author

Duan Zhengqiang, is a senior middleware engineer at SphereEx & Apache ShardingSphere PMC.

Zhengqiang started to contribute to Apache ShardingSphere middleware in 2018 and played a leading role in sharding practices dealing with massive amounts data.

With rich practical experience, he loves open-source and is willing to contribute to a variety of projects. He currently focuses on the development of Apache ShardingSphere kernel module.

## Relevant Links

🔗 [ShardingSphere Official Website](https://shardingsphere.apache.org/)

🔗 [ShardingSphere Official Project Repo](https://github.com/apache/shardingsphere)

🔗 [ShardingSphere Twitter](https://twitter.com/ShardingSphere)

🔗 [ShardingSphere Slack](https://join.slack.com/t/apacheshardingsphere/shared_invite/zt-sbdde7ie-SjDqo9~I4rYcR18bq0SYTg)