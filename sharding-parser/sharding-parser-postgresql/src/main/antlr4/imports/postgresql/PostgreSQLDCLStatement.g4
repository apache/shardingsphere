grammar PostgreSQLDCLStatement;

import PostgreSQLKeyword, Keyword, Symbol, PostgreSQLBase, BaseRule, DataType;

grant
    : GRANT (privileges_ ON onObjectClause_ | ignoredIdentifiers_)
    ;

revoke
    : REVOKE (GRANT OPTION FOR)? (privileges_ ON onObjectClause_ | ignoredIdentifiers_)
    ;

privileges_
    : privilegeType_ columnNames? (COMMA_ privilegeType_ columnNames?)*
    ;

privilegeType_
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
    | LARGE OBJECT
    | SCHEMA
    | TABLESPACE
    | TYPE
    | TABLE? tableName (COMMA_ tableName)*
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
