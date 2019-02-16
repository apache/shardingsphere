grammar OracleDCLStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, BaseRule, DataType;

grant
    : GRANT (systemPrivileges_ | (objectPrivileges_ ON onObjectClause_) | rolesToPrograms_) TO
    ;

revoke
    : REVOKE (systemPrivileges_ | (objectPrivileges_ ON onObjectClause_) | rolesToPrograms_) FROM
    ;

systemPrivileges_
    : privilege_ (COMMA_ privilege_)*
    ;

objectPrivileges_
    : privilege_ columnList? (COMMA_ privilege_ columnList?)*
    ;

privilege_
    : ALL PRIVILEGES? | ID*?
    ;

onObjectClause_
    : USER | DIRECTORY | EDITION | MINING MODEL | SQL TRANSLATION PROFILE
    | JAVA (SOURCE | RESOURCE) tableName
    | tableName
    ;

rolesToPrograms_
    : ALL | ID*?
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
