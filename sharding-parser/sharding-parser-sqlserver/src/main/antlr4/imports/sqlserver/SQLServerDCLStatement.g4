grammar SQLServerDCLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, BaseRule, DataType;

grant
    : GRANT (byClass_ | byClassType_)
    ;

revoke
    : REVOKE ((GRANT OPTION FOR)? byClass_ | byClassType_)
    ;

deny
    : DENY byClass_
    ;

byClass_
    : (ALL PRIVILEGES? | permission_ columnList? (COMMA_ permission_ columnList?)*) (ON class_? tableName)?
    ;

byClassType_
    : permission_ (COMMA_ permission_)* (ON (classType_)? tableName)?
    ;

permission_
    : ID+?
    ;

class_
    : ID COLON_ COLON_
    ;
    
classType_
    : (LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER) COLON_ COLON_
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

createLogin
    : CREATE LOGIN
    ;

dropLogin
    : DROP LOGIN
    ;

alterLogin
    : ALTER LOGIN
    ;
