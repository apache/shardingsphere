grammar MySQLTCLStatement;

import MySQLKeyword, Keyword, Symbol, BaseRule, DataType;

setTransaction
    : SET (GLOBAL | SESSION)? TRANSACTION
    ;

setAutoCommit
    : SET AUTOCOMMIT EQ_ autoCommitValue
    ;

autoCommitValue
    : NUMBER_
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
