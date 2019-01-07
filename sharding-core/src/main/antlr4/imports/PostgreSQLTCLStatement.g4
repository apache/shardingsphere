grammar PostgreSQLTCLStatement;

import PostgreSQLKeyword, Keyword, BaseRule, DataType, Symbol;

setTransaction
    : SET TRANSACTION (transactionMode (COMMA transactionMode)* | SNAPSHOT ID)
    | SET SESSION CHARACTERISTICS AS TRANSACTION transactionMode (COMMA transactionMode)*
    ;
    
transactionMode
    : ISOLATION LEVEL (SERIALIZABLE | REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED)
    | READ (WRITE | ONLY)
    | NOT? DEFERRABLE
    ;
    
commit
    : COMMIT workOrTransaction?
    | COMMIT PREPARED ID
    ;
    
rollback
    : ROLLBACK (workOrTransaction? | PREPARED ID | workOrTransaction? TO SAVEPOINT? ID)
    ;
    
savepoint
    : SAVEPOINT ID 
    ;
    
beginTransaction
    : BEGIN workOrTransaction? (transactionMode (COMMA transactionMode)*)?
    ;
    
startTransaction
    : START TRANSACTION workOrTransaction? (transactionMode (COMMA transactionMode)*)?
    ;
    
workOrTransaction
    : WORK | TRANSACTION
    ;
