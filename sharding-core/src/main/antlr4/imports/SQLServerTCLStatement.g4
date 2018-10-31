grammar SQLServerTCLStatement;

import SQLServerKeyword, Keyword,SQLServerBase, DataType, Symbol;

/**
 * each statement has a url, 
 * each base url : https://docs.microsoft.com/en-us/sql/t-sql/language-elements/.
 */
//begin-transaction-transact-sql?view=sql-server-2017    
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

//commit-transaction-transact-sql?view=sql-server-2017
//commit-work-transact-sql?view=sql-server-2017
commit
    : COMMIT 
    (
    	((TRAN | TRANSACTION) ID?)? (WITH LEFT_PAREN DELAYED_DURABILITY EQ_OR_ASSIGN (OFF | ON) RIGHT_PAREN)?
    	| WORK?
    )
    ;

//rollback-transaction-transact-sql?view=sql-server-2017
rollback
    : ROLLBACK  
    (
    	(TRAN | TRANSACTION) ID?
       | WORK?
    )
    ;

//save-transaction-transact-sql?view=sql-server-2017
savepoint
    : SAVE (TRAN | TRANSACTION) ID
    ;
    
//begin-transaction-transact-sql?view=sql-server-2017
beginWork
    : BEGIN (TRAN | TRANSACTION) (ID ( WITH MARK STRING)?)?
    ;
