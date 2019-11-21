grammar OracleTCLStatement;

import OracleKeyword, Keyword, OracleBase, DataType, Symbol;

setTransaction
    : SET TRANSACTION
    ( 
        READ (ONLY | WRITE)
        | ISOLATION LEVEL (SERIALIZABLE | READ COMMITTED)
        | USE ROLLBACK SEGMENT ID
    )?
    | NAME STRING
    ;
    
commit
    : COMMIT WORK?
    ( 
        (COMMENT STRING)?
        | (WRITE (WAIT | NOWAIT)? (IMMEDIATE | BATCH)?)?
        | FORCE STRING (COMMA NUMBER)?
    )? 
    ;
    
rollback
    : ROLLBACK WORK?
    ( 
        TO SAVEPOINT? ID
        | FORCE STRING
    )? 
    ;
    
savepoint
    : SAVEPOINT ID 
    ;
