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
	;

partitionOptions:
	;
