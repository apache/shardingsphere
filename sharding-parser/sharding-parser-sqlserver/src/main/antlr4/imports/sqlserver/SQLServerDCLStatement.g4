grammar SQLServerDCLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, BaseRule, DataType;

grant
    : GRANT (classPrivilegesClause_ | classTypePrivilegesClause_) 
    ;

revoke
    : REVOKE ((GRANT OPTION FOR)? classPrivilegesClause_ | classTypePrivilegesClause_)
    ;

deny
    : DENY classPrivilegesClause_
    ;

classPrivilegesClause_
    : classPrivileges_ (ON onClassClause_)?
    ;

classPrivileges_
    : (ALL PRIVILEGES? | (privilegeType_ columnList? (COMMA_ privilegeType_ columnList?)*))
    ;

onClassClause_
    : class_? tableName
    ;

classTypePrivilegesClause_
    : classTypePrivileges_ (ON onClassTypeClause_)?
    ;

classTypePrivileges_
    : privilegeType_ (COMMA_ privilegeType_)*
    ;

onClassTypeClause_
    : classType_? tableName
    ;

privilegeType_
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
