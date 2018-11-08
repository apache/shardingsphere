grammar PostgreSQLDCLStatement;

import PostgreSQLKeyword, Keyword, PostgreSQLBase, BaseRule, DataType, Symbol;

grant
    : GRANT privType columnList? (COMMA privType columnList?)*
    ON (
        TABLE? tableNames
        | ALL TABLES IN SCHEMA schemaNames
        | SEQUENCE sequenceNames
        | ALL SEQUENCES IN SCHEMA schemaNames
        | DATABASE databaseNames
        | DOMAIN domainNames
        | FOREIGN DATA WRAPPER ID (COMMA ID)*
        | FOREIGN SERVER ID (COMMA ID)*
        | (FUNCTION | PROCEDURE | ROUTINE) routineName (LP_ (argMode? ID? dataType (COMMA argMode? ID? dataType)*)? RP_)?
            (COMMA (FUNCTION | PROCEDURE | ROUTINE) routineName (LP_ (argMode? ID? dataType (COMMA argMode? ID? dataType)*)? RP_)?)*
        | ALL (FUNCTION | PROCEDURE | ROUTINE) IN SCHEMA schemaNames
        | LANGUAGE  ID (COMMA ID)*
        | LARGE OBJECT ID (COMMA ID)*
        | SCHEMA schemaNames
        | TABLESPACE tablespaceNames
        | TYPE typeNames
    )
    TO roleSpecifications
    (WITH GRANT OPTION)?
    ;

privType
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

roleSpecification
    : GROUP? roleName | PUBLIC | CURRENT_USER | SESSION_USER
    ;

argMode
    : IN | OUT | INOUT | VARIADIC
    ;

roleSpecifications
    : roleSpecification (COMMA roleSpecification)*
    ;

grantRole
    : GRANT roleNames TO roleNames (WITH ADMIN OPTION)?
    ;

revoke
    : REVOKE (GRANT OPTION FOR)?
    privType columnList? (COMMA privType columnList?)*
    ON (
        TABLE? tableNames
        | ALL TABLES IN SCHEMA schemaNames
        | SEQUENCE sequenceNames
        | ALL SEQUENCES IN SCHEMA schemaNames
        | DATABASE databaseNames
        | DOMAIN domainNames
        | FOREIGN DATA WRAPPER ID (COMMA ID)*
        | FOREIGN SERVER ID (COMMA ID)*
        | (FUNCTION | PROCEDURE | ROUTINE) routineName (LP_ (argMode? ID? dataType (COMMA argMode? ID? dataType)*)? RP_)?
            (COMMA (FUNCTION | PROCEDURE | ROUTINE) routineName (LP_ (argMode? ID? dataType (COMMA argMode? ID? dataType)*)? RP_)?)*
        | ALL (FUNCTION | PROCEDURE | ROUTINE) IN SCHEMA schemaNames
        | LANGUAGE  ID (COMMA ID)*
        | LARGE OBJECT ID (COMMA ID)*
        | SCHEMA schemaNames
        | TABLESPACE tablespaceNames
        | TYPE typeNames
    )
    FROM roleSpecifications
    (CASCADE | RESTRICT)
    ;

revokeRole
    : REVOKE (ADMIN OPTION FOR)?
    roleNames FROM roleNames
    (CASCADE | RESTRICT)
    ;

createUser
    : CREATE USER roleName (WITH? roleOptions)?
    ;

roleOption
    : SUPERUSER
    | NOSUPERUSER
    | CREATEDB
    | NOCREATEDB
    | CREATEROLE
    | NOCREATEROLE
    | INHERIT
    | NOINHERIT
    | LOGIN
    | NOLOGIN
    | REPLICATION
    | NOREPLICATION
    | BYPASSRLS
    | NOBYPASSRLS
    | CONNECTION LIMIT NUMBER
    | ENCRYPTED? PASSWORD STRING
    | VALID UNTIL STRING
    | IN ROLE roleNames
    | IN GROUP roleNames
    | ROLE roleNames
    | ADMIN roleNames
    | USER roleNames
    | SYSID STRING
    ;

roleOptions
    : roleOption (COMMA roleOption)*
    ;

alterUser
    : ALTER USER roleSpecification WITH roleOptions
    ;

renameUser
    : ALTER USER roleName RENAME TO roleName
    ;

alterUserSetConfig
    : ALTER USER (roleSpecification | ALL) (IN DATABASE databaseName)? SET STRING ((TO | EQ) (STRING | ID | NUMBER | DEFAULT) | FROM CURRENT)
    ;

alterUserResetConfig
    : ALTER USER (roleSpecification | ALL) (IN DATABASE databaseName)? RESET (STRING | ALL)
    ;

dropUser
    : DROP USER (IF EXISTS)? roleNames
    ;

createRole
    : CREATE ROLE roleName (WITH? roleOptions)?
    ;

alterRole
    : ALTER ROLE roleSpecification WITH roleOptions
    ;

renameRole
    : ALTER ROLE roleName RENAME TO roleName
    ;

alterRoleSetConfig
    : ALTER ROLE (roleSpecification | ALL) (IN DATABASE databaseName)? SET STRING ((TO | EQ) (STRING | ID | NUMBER | DEFAULT) | FROM CURRENT)
    ;

alterRoleResetConfig
    : ALTER ROLE (roleSpecification | ALL) (IN DATABASE databaseName)? RESET (STRING | ALL)
    ;

dropRole
    : DROP ROLE (IF EXISTS)? roleNames
    ;
