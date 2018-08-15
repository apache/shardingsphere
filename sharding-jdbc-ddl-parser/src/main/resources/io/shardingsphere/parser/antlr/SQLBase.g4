grammar SQLBase;

import Keyword,Symbol;


expr:
	;

itemList:
    LEFT_PAREN item (COMMA  item)* RIGHT_PAREN
    ;
    
item:
	;

schemaName: ID;
tableName: ID;
columnName: ID; 

STRING: 
	DOUBLE_QUOTA ('\\"'|.)*? DOUBLE_QUOTA
    |SINGLE_QUOTA (SINGLE_QUOTA |.)*? SINGLE_QUOTA
	;
    
 INT :
   '0' | [1-9] [0-9]*
   ;


NUMBER:
     MINUS? INT DOT [0-9]+ EXP?
     |MINUS? INT | EXP
     |MINUS? INT
     ;
       
EXP :
    E [+\-]? INT
    ;
    
fragment HEX : 
	[0-9a-fA-F] 
	;
	
HEX_DIGIT:
	'0x' HEX+
	|'X' SINGLE_QUOTA HEX+ SINGLE_QUOTA
	;

BIT_NUM:
	('0b' ('0'|'1')+)
	|
	(B SINGLE_QUOTA ('0'|'1')+ SINGLE_QUOTA) 
	;
	
WS:  
	[ \t\r\n] + ->skip
	;
