+++
pre = "<b>7.5. </b>"
title = "Encryption"
weight = 5
chapter = true
+++

## Scenarios
Whether a new business needs to use data encryption capabilities, or an existing business needs to be transformed to meet encryption needs, Apache ShardingSphere provides a complete set of data encryption solutions.
## Prerequisites
Assuming that the new service goes online, it is now expected that the user's password field will be encrypted and stored in the database, but at the same time, the system should be able to obtain plaintext data.
## Data Planning
Since the user expects the password field to be stored encrypted, but the system needs to be able to obtain the plaintext field, we can encrypt the data using the reversible algorithm AES.
So we can encrypt the `t_user` table with the logical column name `pwd`, which is the field name in Business SQL, which maps to the 'pwd_cipher' field in the database, corresponding to AES, using the configuration shown in the configuration example.
## Procedure
1. Download ShardingSphere-proxy.
2. Use the encryption configuration shown in the configuration example.
3. After the proxy is connected, create the `t_user` table.
``` sql
## You can view the real creation table syntax through the PREVIEW syntax.
encrypt_db=> PREVIEW CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd VARCHAR(200) NOT NULL, PRIMARY KEY (user_id));
 data_source_name |                                                         actual_sql
------------------+----------------------------------------------------------------------------------------------------------------------------
 ds_0             | CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd_cipher VARCHAR(200) NOT NULL, PRIMARY KEY (user_id))
(1 row)
CREATE TABLE t_user (user_id INT NOT NULL, username VARCHAR(200), pwd VARCHAR(200) NOT NULL, PRIMARY KEY (user_id));
``` 
4. Insert data into the `t_user` table.
``` sql
encrypt_db=> PREVIEW INSERT INTO t_user values (1,'ZHANGSAN','123456');
 data_source_name |                                              actual_sql
------------------+------------------------------------------------------------------------------------------------------
 ds_0             | INSERT INTO t_user(user_id, username, pwd_cipher) values (1, 'ZHANGSAN', 'MyOShk4kjRnds7CZfU5NCw==')
(1 row)
INSERT INTO t_user values (1,'ZHANGSAN','123456');
```
5. Query `t_user` table.
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

## Sample
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
## Related References
[YAML Configuration: Encryption](/en/user-manual/shardingsphere-jdbc/yaml-config/rules/encrypt/)