grammar MySQLUpdateStatement;

import MySQLKeyword, Keyword, BaseRule, MySQLDQL, DQLBase, MySQLBase, DataType, Symbol;

update
    : updateClause setClause whereClause? orderByClause? limitClause?
    ;

updateClause
    : UPDATE LOW_PRIORITY? IGNORE? tableReferences
    ;

setClause
    : SET assignmentList
    ;

