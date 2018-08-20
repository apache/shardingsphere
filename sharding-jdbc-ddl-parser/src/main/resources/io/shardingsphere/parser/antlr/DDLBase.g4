grammar DDLBase;

import Keyword, BaseRule,Symbol;

execute:
	dropIndex
	|createIndex
	|dropTable
	|truncateTable
	|alterTable
	|createTable
	;

dropIndex:
	;

createIndex:
	;
		
dropTable:
	;

truncateTable:
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

