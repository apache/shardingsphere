grammar DMLBase;

import Keyword, MySQLBase, DQLBase, BaseRule, DataType, Symbol;

execute
    : insert | update | delete
    ;
    
fromClause 
    : FROM tableReferences
    ;
    
whereClause
    : WHERE expr
    ;
    
havingClause
    : HAVING expr
    ;
    
orderByClause
    : ORDER BY orderByItem (COMMA orderByItem)*
    ;
    
limitClause
    : LIMIT rangeClause
    ;
    
delete: 
    deleteClause whereClause? orderByClause? limitClause?
    ;
    
partitionClause 
    : PARTITION idList
    ;
    
update: 
    updateClause setClause whereClause? orderByClause? limitClause?
    ;
    
setClause 
    : SET assignmentList
    ;
    
assignmentList
    : assignment (COMMA assignment)*
    ;
    
assignment
    : columnName EQ_ value
    ;
    
insert
    :
    ;
    
deleteClause
    :
    ;

updateClause
    :
    ;

updateSpec 
    :
    ;
