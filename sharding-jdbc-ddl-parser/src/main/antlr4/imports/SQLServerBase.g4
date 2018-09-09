grammar SQLServerBase;

import SQLServerKeyword,Keyword,Symbol,BaseRule,DataType;

dataType: 
	typeName   
    (
    	LEFT_PAREN  
	    	(
	    		(NUMBER ( COMMA NUMBER )?)
	    		| MAX 
	    		|((CONTENT | DOCUMENT)? xmlSchemaCollection) 
	    	)
    	RIGHT_PAREN 
    )?   
	;
	
	privateExprOfDb:
	windowedFunction
	|atTimeZoneExpr
	|castExpr
	|convertExpr
	;

atTimeZoneExpr:
	ID (WITH TIME ZONE)? STRING
	;
	
castExpr:
	CAST LEFT_PAREN expr AS dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? RIGHT_PAREN  
	;
	
convertExpr:
	CONVERT ( dataType (LEFT_PAREN  NUMBER RIGHT_PAREN )? COMMA expr (COMMA NUMBER)?)
	;
	
windowedFunction:
 	functionCall overClause
 	;
 	
 overClause:
	OVER 
		LEFT_PAREN     
	      partitionByClause?
	      orderByClause?  
	      rowRangeClause? 
	    RIGHT_PAREN 
	;
	
partitionByClause:  
	PARTITION BY expr (COMMA expr)*  
	;
	
orderByClause:   
	ORDER BY orderByExpr (COMMA orderByExpr)*
   ;
  
orderByExpr:
  	expr (COLLATE collationName)? (ASC | DESC)? 
	;
	
rowRangeClause:  
 	(ROWS | RANGE) windowFrameExtent
 	;
 
 windowFrameExtent: 
	windowFramePreceding
  	| windowFrameBetween 
	; 
	
windowFrameBetween:
  	BETWEEN windowFrameBound AND windowFrameBound  
	;
	
windowFrameBound:  
	windowFramePreceding 
  	| windowFrameFollowing 
	;
	
windowFramePreceding:  
    (UNBOUNDED PRECEDING)  
  | NUMBER PRECEDING  
  | CURRENT ROW  
; 

windowFrameFollowing:
    UNBOUNDED FOLLOWING  
  | NUMBER FOLLOWING  
  | CURRENT ROW  
;