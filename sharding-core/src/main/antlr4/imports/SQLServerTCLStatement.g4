grammar SQLServerTCLStatement;

import SQLServerKeyword, Keyword, Symbol, SQLServerBase, DataType;

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
    : BEGIN (TRAN | TRANSACTION) (ID (WITH MARK STRING_)?)?
    ;

setAutoCommit
    : (IF AT_ AT_ TRANCOUNT GT_ NUMBER_ COMMIT TRAN)? SET IMPLICIT_TRANSACTIONS autoCommitValue
    ;

autoCommitValue
    : ON | OFF
    ;
