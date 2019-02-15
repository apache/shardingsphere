grammar MySQLDCLStatement;

import MySQLKeyword, Keyword, Symbol, MySQLBase, BaseRule, DataType;

grant
    : GRANT privType_ columnList? (COMMA_ privType_ columnList?)* ON objectType_? privLevel
    ;

grantProxy
    : GRANT PROXY ON
    ;

grantRole
    : GRANT roleNames
    ;

revoke
    : REVOKE privType_ columnList? (COMMA_ privType_ columnList?)* ON objectType_? privLevel
    ;

revokeAll
    : REVOKE ALL PRIVILEGES? COMMA_ GRANT OPTION
    ;

revokeProxy
    : REVOKE PROXY ON
    ;

revokeRole
    : REVOKE roleNames
    ;

privType_
    : ALL PRIVILEGES?
    | ALTER ROUTINE?
    | CREATE
    | CREATE ROUTINE
    | CREATE TABLESPACE
    | CREATE TEMPORARY TABLES
    | CREATE USER
    | CREATE VIEW
    | DELETE
    | DROP
    | EVENT
    | EXECUTE
    | FILE
    | GRANT OPTION
    | INDEX
    | INSERT
    | LOCK TABLES
    | PROCESS
    | PROXY
    | REFERENCES
    | RELOAD
    | REPLICATION CLIENT
    | REPLICATION SLAVE
    | SELECT
    | SHOW DATABASES
    | SHOW VIEW
    | SHUTDOWN
    | SUPER
    | TRIGGER
    | UPDATE
    | USAGE
    | AUDIT_ADMIN
    | BINLOG_ADMIN
    | CONNECTION_ADMIN
    | ENCRYPTION_KEY_ADMIN
    | FIREWALL_ADMIN
    | FIREWALL_USER
    | GROUP_REPLICATION_ADMIN
    | REPLICATION_SLAVE_ADMIN
    | ROLE_ADMIN
    | SET_USER_ID
    | SYSTEM_VARIABLES_ADMIN
    | VERSION_TOKEN_ADMIN
    ;

objectType_
    : TABLE | FUNCTION | PROCEDURE
    ;

privLevel
    : ASTERISK_ | ASTERISK_ DOT_ASTERISK_ | ID DOT_ASTERISK_ | ID DOT_ tableName | tableName
    ;

createUser
    : CREATE USER
    ;

dropUser
    : DROP USER
    ;

alterUser
    : ALTER USER
    ;

renameUser
    : RENAME USER
    ;

createRole
    : CREATE ROLE
    ;

dropRole
    : DROP ROLE
    ;

setRole
    : SET DEFAULT? ROLE
    ;

setPassword
    : SET PASSWORD
    ;
