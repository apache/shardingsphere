grammar OracleDCLStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, BaseRule, DataType;

grant
    : GRANT (systemPrivileges_ | (objectPrivileges_ ON onObjectClause_) | rolesToPrograms_) TO
    ;

revoke
    : REVOKE (systemPrivileges_ | (objectPrivileges_ ON onObjectClause_) | rolesToPrograms_) FROM
    ;

systemPrivileges_
    : privilegeType_ (COMMA_ privilegeType_)*
    ;

objectPrivileges_
    : privilegeType_ columnList? (COMMA_ privilegeType_ columnList?)*
    ;

privilegeType_
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
