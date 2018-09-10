grammar SQLServerCreateIndex;
import SQLServerKeyword, DataType, Keyword, SQLServerBase,BaseRule,Symbol;

createIndex:
	CREATE ( UNIQUE )? ( CLUSTERED | NONCLUSTERED )? INDEX indexName   
    ON objectName columnNameWithSortsWithParen  
    ( INCLUDE columnList )?  
    ( WHERE predicate )?  
    ( WITH LEFT_PAREN indexOption ( COMMA indexOption )* RIGHT_PAREN )?  
    ( ON ( schemaName LEFT_PAREN columnName RIGHT_PAREN   
         | groupName   
         | DEFAULT   
         )  
    )?  
    ( FILESTREAM_ON ( groupName | schemaName | STRING ) )?  

; 

objectName : ID; 
 





