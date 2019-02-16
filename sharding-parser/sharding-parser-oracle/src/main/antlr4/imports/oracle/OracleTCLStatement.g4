grammar OracleTCLStatement;

import OracleKeyword, Keyword, Symbol, OracleBase, DataType;

setTransaction
    : SET TRANSACTION
    ;

commit
    : COMMIT
    ;

rollback
    : ROLLBACK
    ;

savepoint
    : SAVEPOINT 
    ;
