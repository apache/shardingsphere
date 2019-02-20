grammar SQLServerTCLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, DataType;

setTransaction
    : SET TRANSACTION
    ;

setAutoCommit
    : (IF AT_ AT_ TRANCOUNT GT_ NUMBER_ COMMIT TRAN)? SET IMPLICIT_TRANSACTIONS autoCommitValue
    ;

autoCommitValue
    : ON | OFF
    ;

beginTransaction
    : BEGIN (TRAN | TRANSACTION)
    ;

commit
    : COMMIT 
    ;

rollback
    : ROLLBACK
    ;

savepoint
    : SAVE (TRAN | TRANSACTION)
    ;
