grammar PostgreTCLStatement;

import PostgreKeyword, Keyword, BaseRule, DataType, Symbol;

/**
 * each statement has a url, 
 * each base url : https://www.postgresql.org/docs/current/static/.
 * current version is 11
 */
//sql-set-transaction.html    
setTransaction
    : SET TRANSACTION (transactionMode (COMMA transactionMode)* | SNAPSHOT ID)
    | SET SESSION CHARACTERISTICS AS TRANSACTION transactionMode (COMMA transactionMode)*
    ;
    
transactionMode
    : ISOLATION LEVEL (SERIALIZABLE | REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED)
    | READ (WRITE | ONLY)
    | NOT? DEFERRABLE
    ;
    
//sql-commit.html,sql-commit-prepared.html
commit
    : COMMIT workOrTransaction?
    | COMMIT PREPARED ID
    ;

//sql-rollback.html,sql-rollback-prepared.html,sql-rollback-to.html
rollback
    : ROLLBACK workOrTransaction?
    | ROLLBACK PREPARED ID
    | ROLLBACK workOrTransaction? TO SAVEPOINT? ID
    ;

//sql-savepoint.html
savepoint
    : SAVEPOINT ID 
    ;

//sql-begin.html 
beginWork
    : BEGIN workOrTransaction? (transactionMode (COMMA transactionMode)*)?
    ;

//sql-start-transaction.html
startTransaction
    : START TRANSACTION workOrTransaction? (transactionMode (COMMA transactionMode)*)?
    ;

workOrTransaction
    : WORK 
    | TRANSACTION
    ;