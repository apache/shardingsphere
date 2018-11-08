grammar PostgreSQLDCLStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

createUser
    : CREATE USER userName (WITH? createUserOption)?
    ;

createUserOption
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

createUserOptions
    : createUserOption (COMMA createUserOption)*
    ;
