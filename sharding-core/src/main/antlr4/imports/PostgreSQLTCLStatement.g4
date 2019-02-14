grammar PostgreSQLTCLStatement;

import PostgreSQLKeyword, Keyword, Symbol, BaseRule, DataType;

setTransaction
    : SET TRANSACTION (transactionMode (COMMA_ transactionMode)* | SNAPSHOT ID) | SET SESSION CHARACTERISTICS AS TRANSACTION transactionMode (COMMA_ transactionMode)*
    ;

transactionMode
    : ISOLATION LEVEL (SERIALIZABLE | REPEATABLE READ | READ COMMITTED | READ UNCOMMITTED) | READ (WRITE | ONLY) | NOT? DEFERRABLE
    ;

commit
    : COMMIT workOrTransaction? | COMMIT PREPARED ID
    ;

rollback
    : ROLLBACK (workOrTransaction? | PREPARED ID | workOrTransaction? TO SAVEPOINT? ID)
    ;

savepoint
    : SAVEPOINT ID 
    ;

beginTransaction
    : BEGIN workOrTransaction? (transactionMode (COMMA_ transactionMode)*)?
    ;

startTransaction
    : START TRANSACTION workOrTransaction? (transactionMode (COMMA_ transactionMode)*)?
    ;

workOrTransaction
    : WORK | TRANSACTION
    ;
