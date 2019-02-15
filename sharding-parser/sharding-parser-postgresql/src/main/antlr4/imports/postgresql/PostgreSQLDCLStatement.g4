grammar PostgreSQLDCLStatement;

import PostgreSQLKeyword, Keyword, Symbol, PostgreSQLBase, BaseRule, DataType;

grant
    : GRANT privType_ columnList? (COMMA_ privType_ columnList?)* ON privLevel
    ;

grantRole
    : GRANT roleNames
    ;

revoke
    : REVOKE (GRANT OPTION FOR)? privType_ columnList? (COMMA_ privType_ columnList?)* ON privLevel
    ;

revokeRole
    : REVOKE (ADMIN OPTION FOR)? roleNames
    ;

privType_
    : ALL PRIVILEGES?
    | SELECT
    | INSERT
    | UPDATE
    | DELETE
    | TRUNCATE
    | REFERENCES
    | TRIGGER
    | CREATE
    | CONNECT
    | TEMPORARY
    | TEMP
    | EXECUTE
    | USAGE
    ;

privLevel
    : SEQUENCE
    | DATABASE
    | DOMAIN
    | FOREIGN
    | FUNCTION 
    | PROCEDURE
    | ROUTINE 
    | ALL
    | LANGUAGE
    | LARGE OBJECT
    | SCHEMA
    | TABLESPACE
    | TYPE
    | TABLE? tableNames
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

createRole
    : CREATE ROLE
    ;

dropRole
    : DROP ROLE
    ;

alterRole
    : ALTER ROLE
    ;
