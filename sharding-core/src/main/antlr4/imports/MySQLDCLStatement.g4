grammar MySQLDCLStatement;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

/**
 * each statement has a url, 
 * each base url : https://dev.mysql.com/doc/refman/8.0/en/.
 */

//grant.html
grantPriveleges
    : GRANT
    privType columnList? (COMMA privType columnList?)*
    ON objectType? privLevel
    TO userOrRoles
    (WITH GRANT OPTION)?
    ;

privType
    : ALL PRIVILEGES?
    | ALTER ROUTINE?
    | CREATE (ROUTINE | TEMPORARY TABLES? | USER | VIEW)?
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

objectType
    : TABLE
    | FUNCTION
    | PROCEDURE
    ;

privLevel
    : ASTERISK
    | ASTERISK DOT ASTERISK
    | schemaName DOT ASTERISK
    | schemaName DOT tableName
    | tableName
    | schemaName DOT routineName
    ;

userOrRole
    : userName
    | roleName
    ;

userOrRoles
    : userOrRole (COMMA userOrRole)*
    ;

//grant.html
grantProxy
    : GRANT PROXY ON userOrRole
    TO userOrRoles
    (WITH GRANT OPTION)?
    ;

//grant.html
grantRoles
    : GRANT roleNames
    TO userOrRoles
    (WITH ADMIN OPTION)?
    ;