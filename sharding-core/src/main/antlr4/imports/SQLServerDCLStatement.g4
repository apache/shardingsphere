grammar SQLServerDCLStatement;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

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

