grammar SQLServerDCLStatement;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

grant
    : grantGeneral
    | grantDW
    ;
    
grantGeneral
    : GRANT (ALL PRIVILEGES? | permissionOnColumns ( COMMA permissionOnColumns)*)
    (ON (ID COLONCOLON)? ID )? TO ids   
    (WITH GRANT OPTION)? (AS ID)? 
    ;

permissionOnColumns
    : permission columnList?
    ;

permission
    : ID *?
    ;

grantDW
    : GRANT permission (COMMA permission)*
    (ON (classType COLONCOLON)? ID)?   
    TO ids (WITH GRANT OPTION)?
    ;

classType
    : LOGIN  
    | DATABASE  
    | OBJECT  
    | ROLE  
    | SCHEMA  
    | USER  
    ;
