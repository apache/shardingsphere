grammar SQLServerTCLStatement;

import SQLServerKeyword, Keyword, SQLServerBase, DataType, Symbol;
    
setTransaction
    : SET TRANSACTION ISOLATION LEVEL
    (
        READ (UNCOMMITTED | COMMITTED)
        | REPEATABLE READ
        | SNAPSHOT
        | SERIALIZABLE
        | ISOLATION LEVEL READ UNCOMMITTED
    )
    ;

commit
    : COMMIT 
    (
        ((TRAN | TRANSACTION) ID?)? (WITH LP_ DELAYED_DURABILITY EQ_ (OFF | ON) RP_)?
        | WORK?
    )
    ;

rollback
    : ROLLBACK  
    (
        (TRAN | TRANSACTION) ID?
        | WORK?
    )
    ;

savepoint
    : SAVE (TRAN | TRANSACTION) ID
    ;

beginWork
    : BEGIN (TRAN | TRANSACTION) (ID (WITH MARK STRING)?)?
    ;
