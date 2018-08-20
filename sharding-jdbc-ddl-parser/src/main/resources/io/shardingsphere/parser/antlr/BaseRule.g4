//rule in this file does not allow override

grammar BaseRule;

import DataType,Keyword,Symbol;

schemaName: ID;
tableName: ID;
columnName: ID; 
tablespaceName: ID;
collationName: ID;
indexName: ID;
alias: ID;
cteName:ID;



idList:
    LEFT_PAREN ID (COMMA  ID)* RIGHT_PAREN
    ;

rangeClause:
	NUMBER (COMMA  NUMBER)* 
	| NUMBER OFFSET NUMBER
	;

columnList:
	LEFT_PAREN columnName (COMMA columnName)* RIGHT_PAREN
	;

