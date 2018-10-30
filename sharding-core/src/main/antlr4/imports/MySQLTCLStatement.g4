grammar MySQLTCLStatement;

import MySQLKeyword, Keyword, BaseRule, DataType, Symbol;

/**
 * each statement has a url, 
 * each base url : https://dev.mysql.com/doc/refman/8.0/en/.
 */

//set-transaction.html
setTransaction
    : SET (GLOBAL | SESSION) TRANSACTION
    setTransactionCharacteristic (COMMA setTransactionCharacteristic)*
    ;
    
setTransactionCharacteristic
    : ISOLATION LEVEL level
    | accessMode
    ;

level
    : REPEATABLE READ
    | READ (COMMITTED | UNCOMMITTED)
    | SERIALIZABLE
    ;

accessMode
    : READ (WRITE | ONLY)
    ;
    
//commit.html
commit
    : COMMIT WORK? (AND NO? CHAIN)? (NO? RELEASE)?
    ;

//commit.html
beginWork
    : BEGIN WORK?
    | startTransaction
    ;
    
//commit.html
startTransaction
    : START TRANSACTION startTransactionCharacteristic (COMMA startTransactionCharacteristic)*
    ;

startTransactionCharacteristic
    : WITH CONSISTENT SNAPSHOT
    | READ (WRITE | ONLY)
    ;
    
//savepoint.html
rollback
    : ROLLBACK WORK? 
    (
    	(AND NO? CHAIN)? (NO? RELEASE)? 
       |TO SAVEPOINT? ID
    )
    ;
    
//savepoint.html 
savepoint
    : SAVEPOINT ID 
    ;

//set-variable.html
setVariable
    : SET assignment (COMMA assignment)*
    ;

assignment
    : variable EQ_OR_ASSIGN expr
    ;

variable
    : (AT_ AT_)? (GLOBAL | PERSIST  | PERSIST_ONLY | SESSION)? DOT? ID
    ;   