grammar SQLServerDCLStatement;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

grant
    : grantGeneral | grantDW
    ;
    
grantGeneral
    : GRANT (ALL PRIVILEGES? | permissionOnColumns ( COMMA permissionOnColumns)*)
    (ON (ID COLON COLON)? ID)? TO ids
    (WITH GRANT OPTION)? (AS ID)? 
    ;

permissionOnColumns
    : permission columnList?
    ;

permission
    : ID *?
    ;

grantDW
    : GRANT permission (COMMA permission)* (ON (classType COLON COLON)? ID)?   
    TO ids (WITH GRANT OPTION)?
    ;

classType
    : LOGIN | DATABASE | OBJECT | ROLE | SCHEMA | USER  
    ;

revoke
    : revokeGeneral | revokeDW
    ;

revokeGeneral
    : REVOKE (GRANT OPTION FOR)?
    ((ALL PRIVILEGES?)? | permissionOnColumns)
    (ON (ID COLON COLON)? ID)?
    (TO | FROM) ids
    (CASCADE)? ( AS ID)?
    ;

revokeDW
    : REVOKE permission (COMMA permission)* (ON (classType COLON COLON)? ID)?
    (FROM | TO)? ids CASCADE?
    ;


