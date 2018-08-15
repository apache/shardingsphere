grammar DDLBase;

import SQLBase, Keyword, Symbol;

execute:
	alterTable
	;
	
alterTable:
    ALTER TABLE prefixTableName tableName alterSpecifications? partitionOptions?
	;

prefixTableName:
    ; 	

alterSpecifications:
	NONE
	;

partitionOptions:
	NONE
	;

createTable:
	CREATE 	TEMPORARY? TABLE (IF NOT EXISTS)? tableName createTableOptions
	; 
	
createTableOptions:
	NONE;