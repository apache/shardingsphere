grammar SQLServerTCLStatement;

import SQLServerKeyword, Keyword, SQLServerBase, DataType, Symbol;

setTransaction
    : SET TRANSACTION ISOLATION LEVEL
    (
        READ (UNCOMMITTED | COMMITTED) | REPEATABLE READ | SNAPSHOT | SERIALIZABLE
    )
    ;
    
commit
    : COMMIT 
    (
        ((TRAN | TRANSACTION) ID?)? (WITH LP_ DELAYED_DURABILITY EQ_ (OFF | ON) RP_)? | WORK?
    )
    ;
    
rollback
    : ROLLBACK
    (
        (TRAN | TRANSACTION) ID? | WORK?
    )
    ;
    
savepoint
    : SAVE (TRAN | TRANSACTION) ID
    ;
    
beginTransaction
    : BEGIN (TRAN | TRANSACTION) (ID (WITH MARK STRING)?)?
    ;
    
setAutoCommit
    : (IF AT_ AT_ TRANCOUNT GT NUMBER COMMIT TRAN)? SET IMPLICIT_TRANSACTIONS autoCommitValue
    ;
    
autoCommitValue
    : ON | OFF
    ;
