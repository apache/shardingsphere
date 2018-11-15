grammar DMLBase;

import Keyword, MySQLBase, DQLBase, BaseRule, DataType, Symbol;

execute
    : select | insert | update | delete
    ;

fromClause 
    : FROM tableReferences
    ;

whereClause
    : WHERE expr
    ;

havingClause
    : HAVING  expr
    ;

orderByClause
    : ORDER BY groupByItem (COMMA groupByItem)*
    ;

groupByItem:
    (columnName | NUMBER |expr) (ASC|DESC)?
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

insert:
    ;

deleteClause:
    ;

updateClause:
    ;

updateSpec: 
    ;
