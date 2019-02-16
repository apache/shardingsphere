grammar OracleDCLStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, BaseRule, DataType;

grant
    : GRANT (objectPrivileges_ (ON onObjectClause_)? | otherPrivileges_)
    ;

revoke
    : REVOKE (objectPrivileges_ (ON onObjectClause_)? | otherPrivileges_)
    ;

objectPrivileges_
    : objectPrivilegeType_ columnList? (COMMA_ objectPrivilegeType_ columnList?)*
    ;

objectPrivilegeType_
    : (ALL PRIVILEGES?)
    | SELECT
    | INSERT
    | DELETE
    | UPDATE
    | ALTER
    | READ
    | WRITE
    | EXECUTE
    | USE
    | INDEX
    | REFERENCES
    | DEBUG
    | UNDER
    | (FLASHBACK ARCHIVE)
    | (ON COMMIT REFRESH)
    | (QUERY REWRITE)
    | (KEEP SEQUENCE)
    | (INHERIT PRIVILEGES)
    | (TRANSLATE SQL)
    | (MERGE VIEW)
    ;

onObjectClause_
    : USER | DIRECTORY | EDITION | (MINING MODEL) | (SQL TRANSLATION PROFILE)
    | JAVA (SOURCE | RESOURCE) tableName
    | tableName
    ;

otherPrivileges_
    : STRING_+ | ID+
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
