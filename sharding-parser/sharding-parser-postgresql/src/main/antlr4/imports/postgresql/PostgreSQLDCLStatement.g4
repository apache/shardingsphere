grammar PostgreSQLDCLStatement;

import PostgreSQLKeyword, Keyword, Symbol, PostgreSQLBase, BaseRule, DataType;

grant
    : GRANT ((privileges_ ON onObjectClause_) | roleNames)
    ;

revoke
    : REVOKE (GRANT OPTION FOR)? ((privileges_ ON onObjectClause_) | roleNames)
    ;

privileges_
    : privilegeType_ columnList? (COMMA_ privilegeType_ columnList?)*
    ;

privilegeType_
    : (ALL PRIVILEGES?)
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

onObjectClause_
    : SEQUENCE
    | DATABASE
    | DOMAIN
    | FOREIGN
    | FUNCTION
    | PROCEDURE
    | ROUTINE
    | ALL
    | LANGUAGE
    | (LARGE OBJECT)
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
