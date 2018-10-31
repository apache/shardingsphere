grammar OracleTCLStatement;

import OracleKeyword, Keyword, OracleBase, DataType, Symbol;

/**
 * each statement has a url, 
 * each base url : https://docs.oracle.com/database/121/SQLRF/.
 * no begin statement in oracle
 */
 //statements_10005.htm#SQLRF01705
setTransaction
    : SET TRANSACTION
    ( 
     	 ( 
     		 READ (ONLY | WRITE)
           | ISOLATION LEVEL ( SERIALIZABLE | READ COMMITTED )
           | USE ROLLBACK SEGMENT ID
         )(NAME STRING)?
        | NAME STRING
    ) 
    ;

///statements_4011.htm#SQLRF01110
commit
    : COMMIT WORK?
    ( 
    	  (COMMENT STRING)?
        | ( WRITE (WAIT | NOWAIT)? (IMMEDIATE | BATCH)?)?
        | FORCE STRING (COMMA NUMBER)?
    )? 
    ;

//statements_9023.htm#SQLRF01610 
rollback
    : ROLLBACK WORK?
    ( 
    	  TO SAVEPOINT? ID//ID is savepoint name
        | FORCE STRING
    )? 
    ;

//statements_10001.htm#SQLRF01701 
savepoint
    : SAVEPOINT ID 
    ;
 