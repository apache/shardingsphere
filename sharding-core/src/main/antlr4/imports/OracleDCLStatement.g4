grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

grantRolesToPrograms
    : roleName (COMMA roleName)* TO programUnit ( COMMA programUnit )*
    ;
    
programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
    ;