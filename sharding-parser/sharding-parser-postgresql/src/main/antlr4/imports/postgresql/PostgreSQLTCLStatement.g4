grammar PostgreSQLTCLStatement;

import PostgreSQLKeyword, Keyword, Symbol, BaseRule, DataType;

setTransaction
    : SET (SESSION CHARACTERISTICS AS)? TRANSACTION
    ;

beginTransaction
    : BEGIN | (START TRANSACTION)
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
