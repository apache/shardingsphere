grammar MySQLTCLStatement;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

setTransaction
    : SET (GLOBAL | SESSION) TRANSACTION setTransactionCharacteristic (COMMA setTransactionCharacteristic)*
    ;

setTransactionCharacteristic
    : ISOLATION LEVEL level| accessMode
    ;

level
    : REPEATABLE READ | READ (COMMITTED | UNCOMMITTED) | SERIALIZABLE
    ;

accessMode
    : READ (WRITE | ONLY)
    ;

commit
    : COMMIT WORK? (AND NO? CHAIN)? (NO? RELEASE)?
    ;

beginWork
    : BEGIN WORK? | startTransaction
    ;

startTransaction
    : START TRANSACTION startTransactionCharacteristic (COMMA startTransactionCharacteristic)*
    ;

startTransactionCharacteristic
    : WITH CONSISTENT SNAPSHOT | READ (WRITE | ONLY)
    ;

rollback
    : ROLLBACK WORK? 
    (
        (AND NO? CHAIN)? (NO? RELEASE)? 
       | TO SAVEPOINT? ID
    )
    ;

savepoint
    : SAVEPOINT ID 
    ;

setVariable
    : SET assignment (COMMA assignment)*
    ;

assignment
    : variable EQ_ expr
    ;

variable
    : (AT_ AT_)? (GLOBAL | PERSIST  | PERSIST_ONLY | SESSION)? DOT? ID
    ;
