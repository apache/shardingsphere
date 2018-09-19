grammar OracleDropTable;

import OracleKeyword, DataType, Keyword,BaseRule,Symbol;

dropTable:
	DROP TABLE tableName
  	(CASCADE CONSTRAINTS)?  PURGE? 
  	;