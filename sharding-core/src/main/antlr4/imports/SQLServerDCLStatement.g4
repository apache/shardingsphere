grammar SQLServerDCLStatement;

import SQLServerKeyword, DataType, Keyword, SQLServerBase, BaseRule, Symbol;

permissionOnColumns
    : permission columnList?
    ;

permission
    : ID *?
    ;

