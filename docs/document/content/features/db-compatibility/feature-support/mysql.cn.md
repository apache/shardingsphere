+++
title = "MySQL"
weight = 1
+++

MySQL 不支持的 SQL 清单如下：

## 用户和角色
| SQL                                                          |
|--------------------------------------------------------------|
| CREATE USER 'finley'@'localhost' IDENTIFIED BY 'password'    |
| ALTER USER 'finley'@'localhost' IDENTIFIED BY 'new_password' |
| DROP USER 'finley'@'localhost';                              |
| CREATE ROLE 'app_read'                                       |
| DROP ROLE 'app_read'                                         |
| SHOW CREATE USER finley                                      |
| SET PASSWORD = 'auth_string'                                 |
| SET ROLE DEFAULT;                                            |

## 授权
| SQL                                                   |
|-------------------------------------------------------|
| GRANT ALL ON db1.* TO 'jeffrey'@'localhost'           |
| GRANT SELECT ON world.* TO 'role3';                   |
| GRANT 'role1', 'role2' TO 'user1'@'localhost'         |
| REVOKE INSERT ON *.* FROM 'jeffrey'@'localhost'       |
| REVOKE 'role1', 'role2' FROM 'user1'@'localhost'      |
| REVOKE ALL PRIVILEGES, GRANT OPTION FROM user_or_role |
| SHOW GRANTS FOR 'jeffrey'@'localhost'                 |
| SHOW GRANTS FOR CURRENT_USER                          |
| FLUSH PRIVILEGES                                      |
