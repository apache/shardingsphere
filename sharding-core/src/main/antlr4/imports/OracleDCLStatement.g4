grammar OracleDCLStatement;

import OracleKeyword, Keyword, OracleBase, BaseRule, DataType, Symbol;

programUnit
    : (FUNCTION | PROCEDURE | PACKAGE) schemaName? ID
    ;